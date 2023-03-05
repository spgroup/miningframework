package org.apache.cassandra.service;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.*;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.QueryHandler;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.JVMStabilityInspector;
import org.apache.cassandra.utils.SemanticVersion;

public class ClientState {

    private static final Logger logger = LoggerFactory.getLogger(ClientState.class);

    public static final SemanticVersion DEFAULT_CQL_VERSION = org.apache.cassandra.cql3.QueryProcessor.CQL_VERSION;

    private static final Set<IResource> READABLE_SYSTEM_RESOURCES = new HashSet<>();

    private static final Set<IResource> PROTECTED_AUTH_RESOURCES = new HashSet<>();

    static {
        for (String cf : Iterables.concat(Arrays.asList(SystemKeyspace.LOCAL_CF, SystemKeyspace.PEERS_CF), SystemKeyspace.allSchemaCfs)) READABLE_SYSTEM_RESOURCES.add(DataResource.columnFamily(Keyspace.SYSTEM_KS, cf));
        PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthenticator().protectedResources());
        PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthorizer().protectedResources());
    }

    private volatile AuthenticatedUser user;

    private volatile String keyspace;

    private SemanticVersion cqlVersion;

    private static final QueryHandler cqlQueryHandler;

    static {
        QueryHandler handler = QueryProcessor.instance;
        String customHandlerClass = System.getProperty("cassandra.custom_query_handler_class");
        if (customHandlerClass != null) {
            try {
                handler = (QueryHandler) FBUtilities.construct(customHandlerClass, "QueryHandler");
                logger.info("Using {} as query handler for native protocol queries (as requested with -Dcassandra.custom_query_handler_class)", customHandlerClass);
            } catch (Exception e) {
                JVMStabilityInspector.inspectThrowable(e);
                logger.info("Cannot use class {} as query handler ({}), ignoring by defaulting on normal query handling", customHandlerClass, e.getMessage());
            }
        }
        cqlQueryHandler = handler;
    }

    public final boolean isInternal;

    private final SocketAddress remoteAddress;

    private static final AtomicLong lastTimestampMicros = new AtomicLong(0);

    private ClientState() {
        this.isInternal = true;
        this.remoteAddress = null;
    }

    protected ClientState(SocketAddress remoteAddress) {
        this.isInternal = false;
        this.remoteAddress = remoteAddress;
        if (!DatabaseDescriptor.getAuthenticator().requireAuthentication())
            this.user = AuthenticatedUser.ANONYMOUS_USER;
    }

    public static ClientState forInternalCalls() {
        return new ClientState();
    }

    public static ClientState forExternalCalls(SocketAddress remoteAddress) {
        return new ClientState(remoteAddress);
    }

    public long getTimestamp() {
        while (true) {
            long current = System.currentTimeMillis() * 1000;
            long last = lastTimestampMicros.get();
            long tstamp = last >= current ? last + 1 : current;
            if (lastTimestampMicros.compareAndSet(last, tstamp))
                return tstamp;
        }
    }

    public long getTimestamp(long minTimestampToUse) {
        while (true) {
            long current = Math.max(System.currentTimeMillis() * 1000, minTimestampToUse);
            long last = lastTimestampMicros.get();
            long tstamp = last >= current ? last + 1 : current;
            if (lastTimestampMicros.compareAndSet(last, tstamp))
                return tstamp;
        }
    }

    public static QueryHandler getCQLQueryHandler() {
        return cqlQueryHandler;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getRawKeyspace() {
        return keyspace;
    }

    public String getKeyspace() throws InvalidRequestException {
        if (keyspace == null)
            throw new InvalidRequestException("No keyspace has been specified. USE a keyspace, or explicitly specify keyspace.tablename");
        return keyspace;
    }

    public void setKeyspace(String ks) throws InvalidRequestException {
        if (user != null && Schema.instance.getKSMetaData(ks) == null)
            throw new InvalidRequestException("Keyspace '" + ks + "' does not exist");
        keyspace = ks;
    }

    public void login(AuthenticatedUser user) throws AuthenticationException {
        if (!user.isAnonymous() && !Auth.isExistingUser(user.getName()))
            throw new AuthenticationException(String.format("User %s doesn't exist - create it with CREATE USER query first", user.getName()));
        this.user = user;
    }

    public void hasAllKeyspacesAccess(Permission perm) throws UnauthorizedException {
        if (isInternal)
            return;
        validateLogin();
        ensureHasPermission(perm, DataResource.root());
    }

    public void hasKeyspaceAccess(String keyspace, Permission perm) throws UnauthorizedException, InvalidRequestException {
        hasAccess(keyspace, perm, DataResource.keyspace(keyspace));
    }

    public void hasColumnFamilyAccess(String keyspace, String columnFamily, Permission perm) throws UnauthorizedException, InvalidRequestException {
        ThriftValidation.validateColumnFamily(keyspace, columnFamily);
        hasAccess(keyspace, perm, DataResource.columnFamily(keyspace, columnFamily));
    }

    private void hasAccess(String keyspace, Permission perm, DataResource resource) throws UnauthorizedException, InvalidRequestException {
        validateKeyspace(keyspace);
        if (isInternal)
            return;
        validateLogin();
        preventSystemKSSchemaModification(keyspace, resource, perm);
        if (perm.equals(Permission.SELECT) && READABLE_SYSTEM_RESOURCES.contains(resource))
            return;
        if (PROTECTED_AUTH_RESOURCES.contains(resource))
            if (perm.equals(Permission.CREATE) || perm.equals(Permission.ALTER) || perm.equals(Permission.DROP))
                throw new UnauthorizedException(String.format("%s schema is protected", resource));
        ensureHasPermission(perm, resource);
    }

    public void ensureHasPermission(Permission perm, IResource resource) throws UnauthorizedException {
        for (IResource r : Resources.chain(resource)) if (authorize(r).contains(perm))
            return;
        throw new UnauthorizedException(String.format("User %s has no %s permission on %s or any of its parents", user.getName(), perm, resource));
    }

    private void preventSystemKSSchemaModification(String keyspace, DataResource resource, Permission perm) throws UnauthorizedException {
        if (!(perm.equals(Permission.ALTER) || perm.equals(Permission.DROP) || perm.equals(Permission.CREATE)))
            return;
        if (Keyspace.SYSTEM_KS.equalsIgnoreCase(keyspace))
            throw new UnauthorizedException(keyspace + " keyspace is not user-modifiable.");
        Set<String> allowAlter = Sets.newHashSet(Auth.AUTH_KS, Tracing.TRACE_KS);
        if (allowAlter.contains(keyspace.toLowerCase()) && !(resource.isKeyspaceLevel() && perm.equals(Permission.ALTER)))
            throw new UnauthorizedException(String.format("Cannot %s %s", perm, resource));
    }

    public void validateLogin() throws UnauthorizedException {
        if (user == null)
            throw new UnauthorizedException("You have not logged in");
    }

    public void ensureNotAnonymous() throws UnauthorizedException {
        validateLogin();
        if (user.isAnonymous())
            throw new UnauthorizedException("You have to be logged in and not anonymous to perform this request");
    }

    public void ensureIsSuper(String message) throws UnauthorizedException {
        if (DatabaseDescriptor.getAuthenticator().requireAuthentication() && (user == null || !user.isSuper()))
            throw new UnauthorizedException(message);
    }

    private static void validateKeyspace(String keyspace) throws InvalidRequestException {
        if (keyspace == null)
            throw new InvalidRequestException("You have not set a keyspace for this session");
    }

    public void setCQLVersion(String str) throws InvalidRequestException {
        SemanticVersion version;
        try {
            version = new SemanticVersion(str);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
        SemanticVersion cql = org.apache.cassandra.cql.QueryProcessor.CQL_VERSION;
        SemanticVersion cql3 = org.apache.cassandra.cql3.QueryProcessor.CQL_VERSION;
        SemanticVersion cql3Beta = new SemanticVersion("3.0.0-beta1");
        if (version.equals(cql3Beta))
            throw new InvalidRequestException(String.format("There has been a few syntax breaking changes between 3.0.0-beta1 and 3.0.0 " + "(mainly the syntax for options of CREATE KEYSPACE and CREATE TABLE). 3.0.0-beta1 " + " is not supported; please upgrade to 3.0.0"));
        if (version.isSupportedBy(cql))
            cqlVersion = cql;
        else if (version.isSupportedBy(cql3))
            cqlVersion = cql3;
        else
            throw new InvalidRequestException(String.format("Provided version %s is not supported by this server (supported: %s)", version, StringUtils.join(getCQLSupportedVersion(), ", ")));
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public SemanticVersion getCQLVersion() {
        return cqlVersion;
    }

    public static SemanticVersion[] getCQLSupportedVersion() {
        SemanticVersion cql = org.apache.cassandra.cql.QueryProcessor.CQL_VERSION;
        SemanticVersion cql3 = org.apache.cassandra.cql3.QueryProcessor.CQL_VERSION;
        return new SemanticVersion[] { cql, cql3 };
    }

    private Set<Permission> authorize(IResource resource) {
        return Auth.getPermissions(user, resource);
    }
}
