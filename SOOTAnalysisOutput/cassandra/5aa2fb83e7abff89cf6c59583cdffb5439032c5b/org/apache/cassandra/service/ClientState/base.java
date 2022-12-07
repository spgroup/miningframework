package org.apache.cassandra.service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.*;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.config.SchemaConstants;
import org.apache.cassandra.cql3.QueryHandler;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.functions.Function;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.schema.SchemaKeyspace;
import org.apache.cassandra.thrift.ThriftValidation;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.JVMStabilityInspector;
import org.apache.cassandra.utils.CassandraVersion;

public class ClientState {

    private static final Logger logger = LoggerFactory.getLogger(ClientState.class);

    public static final CassandraVersion DEFAULT_CQL_VERSION = org.apache.cassandra.cql3.QueryProcessor.CQL_VERSION;

    private static final Set<IResource> READABLE_SYSTEM_RESOURCES = new HashSet<>();

    private static final Set<IResource> PROTECTED_AUTH_RESOURCES = new HashSet<>();

    private static final Set<IResource> DROPPABLE_SYSTEM_AUTH_TABLES = new HashSet<>();

    static {
        for (String cf : Arrays.asList(SystemKeyspace.LOCAL, SystemKeyspace.PEERS)) READABLE_SYSTEM_RESOURCES.add(DataResource.table(SchemaConstants.SYSTEM_KEYSPACE_NAME, cf));
        SchemaKeyspace.ALL.forEach(table -> READABLE_SYSTEM_RESOURCES.add(DataResource.table(SchemaConstants.SCHEMA_KEYSPACE_NAME, table)));
        if (DatabaseDescriptor.isDaemonInitialized()) {
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthenticator().protectedResources());
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthorizer().protectedResources());
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getRoleManager().protectedResources());
        }
        DROPPABLE_SYSTEM_AUTH_TABLES.add(DataResource.table(SchemaConstants.AUTH_KEYSPACE_NAME, PasswordAuthenticator.LEGACY_CREDENTIALS_TABLE));
        DROPPABLE_SYSTEM_AUTH_TABLES.add(DataResource.table(SchemaConstants.AUTH_KEYSPACE_NAME, CassandraRoleManager.LEGACY_USERS_TABLE));
        DROPPABLE_SYSTEM_AUTH_TABLES.add(DataResource.table(SchemaConstants.AUTH_KEYSPACE_NAME, CassandraAuthorizer.USER_PERMISSIONS));
    }

    private volatile AuthenticatedUser user;

    private volatile String keyspace;

    private volatile boolean noCompactMode;

    private static final QueryHandler cqlQueryHandler;

    static {
        QueryHandler handler = QueryProcessor.instance;
        String customHandlerClass = System.getProperty("cassandra.custom_query_handler_class");
        if (customHandlerClass != null) {
            try {
                handler = FBUtilities.construct(customHandlerClass, "QueryHandler");
                logger.info("Using {} as query handler for native protocol queries (as requested with -Dcassandra.custom_query_handler_class)", customHandlerClass);
            } catch (Exception e) {
                logger.error("Cannot use class {} as query handler", customHandlerClass, e);
                JVMStabilityInspector.killCurrentJVM(e, true);
            }
        }
        cqlQueryHandler = handler;
    }

    public final boolean isInternal;

    private final InetSocketAddress remoteAddress;

    private static final AtomicLong lastTimestampMicros = new AtomicLong(0);

    private ClientState() {
        this.isInternal = true;
        this.remoteAddress = null;
    }

    protected ClientState(InetSocketAddress remoteAddress) {
        this.isInternal = false;
        this.remoteAddress = remoteAddress;
        if (!DatabaseDescriptor.getAuthenticator().requireAuthentication())
            this.user = AuthenticatedUser.ANONYMOUS_USER;
    }

    public static ClientState forInternalCalls() {
        return new ClientState();
    }

    public static ClientState forExternalCalls(SocketAddress remoteAddress) {
        return new ClientState((InetSocketAddress) remoteAddress);
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

    public long getTimestampForPaxos(long minTimestampToUse) {
        while (true) {
            long current = Math.max(System.currentTimeMillis() * 1000, minTimestampToUse);
            long last = lastTimestampMicros.get();
            long tstamp = last >= current ? last + 1 : current;
            if (tstamp == minTimestampToUse || lastTimestampMicros.compareAndSet(last, tstamp))
                return tstamp;
        }
    }

    public static QueryHandler getCQLQueryHandler() {
        return cqlQueryHandler;
    }

    public InetSocketAddress getRemoteAddress() {
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

    public void setNoCompactMode() {
        this.noCompactMode = true;
    }

    public boolean isNoCompactMode() {
        return noCompactMode;
    }

    public void login(AuthenticatedUser user) throws AuthenticationException {
        if (user.isAnonymous() || canLogin(user))
            this.user = user;
        else
            throw new AuthenticationException(String.format("%s is not permitted to log in", user.getName()));
    }

    private boolean canLogin(AuthenticatedUser user) {
        try {
            return DatabaseDescriptor.getRoleManager().canLogin(user.getPrimaryRole());
        } catch (RequestExecutionException e) {
            throw new AuthenticationException("Unable to perform authentication: " + e.getMessage(), e);
        }
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
        hasAccess(keyspace, perm, DataResource.table(keyspace, columnFamily));
    }

    public void hasColumnFamilyAccess(CFMetaData cfm, Permission perm) throws UnauthorizedException, InvalidRequestException {
        hasAccess(cfm.ksName, perm, cfm.resource);
    }

    private void hasAccess(String keyspace, Permission perm, DataResource resource) throws UnauthorizedException, InvalidRequestException {
        validateKeyspace(keyspace);
        if (isInternal)
            return;
        validateLogin();
        preventSystemKSSchemaModification(keyspace, resource, perm);
        if ((perm == Permission.SELECT) && READABLE_SYSTEM_RESOURCES.contains(resource))
            return;
        if (PROTECTED_AUTH_RESOURCES.contains(resource))
            if ((perm == Permission.CREATE) || (perm == Permission.ALTER) || (perm == Permission.DROP))
                throw new UnauthorizedException(String.format("%s schema is protected", resource));
        ensureHasPermission(perm, resource);
    }

    public void ensureHasPermission(Permission perm, IResource resource) throws UnauthorizedException {
        if (!DatabaseDescriptor.getAuthorizer().requireAuthorization())
            return;
        if (resource instanceof FunctionResource && resource.hasParent())
            if (((FunctionResource) resource).getKeyspace().equals(SchemaConstants.SYSTEM_KEYSPACE_NAME))
                return;
        checkPermissionOnResourceChain(perm, resource);
    }

    public void ensureHasPermission(Permission permission, Function function) {
        if (!DatabaseDescriptor.getAuthorizer().requireAuthorization())
            return;
        if (function.isNative())
            return;
        checkPermissionOnResourceChain(permission, FunctionResource.function(function.name().keyspace, function.name().name, function.argTypes()));
    }

    private void checkPermissionOnResourceChain(Permission perm, IResource resource) {
        for (IResource r : Resources.chain(resource)) if (authorize(r).contains(perm))
            return;
        throw new UnauthorizedException(String.format("User %s has no %s permission on %s or any of its parents", user.getName(), perm, resource));
    }

    private void preventSystemKSSchemaModification(String keyspace, DataResource resource, Permission perm) throws UnauthorizedException {
        if (perm != Permission.ALTER && perm != Permission.DROP && perm != Permission.CREATE)
            return;
        if (SchemaConstants.isLocalSystemKeyspace(keyspace))
            throw new UnauthorizedException(keyspace + " keyspace is not user-modifiable.");
        if (SchemaConstants.isReplicatedSystemKeyspace(keyspace)) {
            if (perm == Permission.ALTER && resource.isKeyspaceLevel())
                return;
            if (perm == Permission.DROP && DROPPABLE_SYSTEM_AUTH_TABLES.contains(resource))
                return;
            throw new UnauthorizedException(String.format("Cannot %s %s", perm, resource));
        }
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

    public AuthenticatedUser getUser() {
        return user;
    }

    public static CassandraVersion[] getCQLSupportedVersion() {
        return new CassandraVersion[] { QueryProcessor.CQL_VERSION };
    }

    private Set<Permission> authorize(IResource resource) {
        return user.getPermissions(resource);
    }
}
