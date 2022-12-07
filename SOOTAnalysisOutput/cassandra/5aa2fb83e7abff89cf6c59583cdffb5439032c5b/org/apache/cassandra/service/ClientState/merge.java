package org.apache.cassandra.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.*;
import org.apache.cassandra.db.virtual.VirtualSchemaKeyspace;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.schema.TableMetadata;
import org.apache.cassandra.schema.TableMetadataRef;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.schema.Schema;
import org.apache.cassandra.schema.SchemaConstants;
import org.apache.cassandra.schema.SchemaKeyspaceTables;
import org.apache.cassandra.cql3.QueryHandler;
import org.apache.cassandra.cql3.QueryProcessor;
import org.apache.cassandra.cql3.functions.Function;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.dht.Datacenters;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.JVMStabilityInspector;

public class ClientState {

    private static final Logger logger = LoggerFactory.getLogger(ClientState.class);

    private static final Set<IResource> READABLE_SYSTEM_RESOURCES = new HashSet<>();

    private static final Set<IResource> PROTECTED_AUTH_RESOURCES = new HashSet<>();

    static {
        for (String cf : Arrays.asList(SystemKeyspace.LOCAL, SystemKeyspace.LEGACY_PEERS, SystemKeyspace.PEERS_V2)) READABLE_SYSTEM_RESOURCES.add(DataResource.table(SchemaConstants.SYSTEM_KEYSPACE_NAME, cf));
        SchemaKeyspaceTables.ALL.forEach(table -> READABLE_SYSTEM_RESOURCES.add(DataResource.table(SchemaConstants.SCHEMA_KEYSPACE_NAME, table)));
        VirtualSchemaKeyspace.instance.tables().forEach(t -> READABLE_SYSTEM_RESOURCES.add(t.metadata().resource));
        if (DatabaseDescriptor.isDaemonInitialized()) {
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthenticator().protectedResources());
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getAuthorizer().protectedResources());
            PROTECTED_AUTH_RESOURCES.addAll(DatabaseDescriptor.getRoleManager().protectedResources());
        }
    }

    private volatile AuthenticatedUser user;

    private volatile String keyspace;

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

    private volatile String driverName;

    private volatile String driverVersion;

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

    protected ClientState(ClientState source) {
        this.isInternal = source.isInternal;
        this.remoteAddress = source.remoteAddress;
        this.user = source.user;
        this.keyspace = source.keyspace;
        this.driverName = source.driverName;
        this.driverVersion = source.driverVersion;
    }

    public static ClientState forInternalCalls() {
        return new ClientState();
    }

    public static ClientState forInternalCalls(String keyspace) {
        ClientState state = new ClientState();
        state.setKeyspace(keyspace);
        return state;
    }

    public static ClientState forExternalCalls(SocketAddress remoteAddress) {
        return new ClientState((InetSocketAddress) remoteAddress);
    }

    public ClientState cloneWithKeyspaceIfSet(String keyspace) {
        if (keyspace == null)
            return this;
        ClientState clientState = new ClientState(this);
        clientState.setKeyspace(keyspace);
        return clientState;
    }

    public static long getTimestamp() {
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

    public Optional<String> getDriverName() {
        return Optional.ofNullable(driverName);
    }

    public Optional<String> getDriverVersion() {
        return Optional.ofNullable(driverVersion);
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

    public static QueryHandler getCQLQueryHandler() {
        return cqlQueryHandler;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    InetAddress getClientAddress() {
        return isInternal ? null : remoteAddress.getAddress();
    }

    public String getRawKeyspace() {
        return keyspace;
    }

    public String getKeyspace() throws InvalidRequestException {
        if (keyspace == null)
            throw new InvalidRequestException("No keyspace has been specified. USE a keyspace, or explicitly specify keyspace.tablename");
        return keyspace;
    }

    public void setKeyspace(String ks) {
        if (user != null && Schema.instance.getKeyspaceMetadata(ks) == null)
            throw new InvalidRequestException("Keyspace '" + ks + "' does not exist");
        keyspace = ks;
    }

    public void login(AuthenticatedUser user) {
        if (user.isAnonymous() || canLogin(user))
            this.user = user;
        else
            throw new AuthenticationException(String.format("%s is not permitted to log in", user.getName()));
    }

    private boolean canLogin(AuthenticatedUser user) {
        try {
            return user.canLogin();
        } catch (RequestExecutionException | RequestValidationException e) {
            throw new AuthenticationException("Unable to perform authentication: " + e.getMessage(), e);
        }
    }

    public void ensureAllKeyspacesPermission(Permission perm) {
        if (isInternal)
            return;
        validateLogin();
        ensurePermission(perm, DataResource.root());
    }

    public void ensureKeyspacePermission(String keyspace, Permission perm) {
        ensurePermission(keyspace, perm, DataResource.keyspace(keyspace));
    }

    public void ensureTablePermission(String keyspace, String table, Permission perm) {
        ensurePermission(keyspace, perm, DataResource.table(keyspace, table));
    }

    public void ensureTablePermission(TableMetadataRef tableRef, Permission perm) {
        ensureTablePermission(tableRef.get(), perm);
    }

    public void ensureTablePermission(TableMetadata table, Permission perm) {
        ensurePermission(table.keyspace, perm, table.resource);
    }

    private void ensurePermission(String keyspace, Permission perm, DataResource resource) {
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
        ensurePermission(perm, resource);
    }

    public void ensurePermission(Permission perm, IResource resource) {
        if (!DatabaseDescriptor.getAuthorizer().requireAuthorization())
            return;
        if (resource instanceof FunctionResource && resource.hasParent())
            if (((FunctionResource) resource).getKeyspace().equals(SchemaConstants.SYSTEM_KEYSPACE_NAME))
                return;
        ensurePermissionOnResourceChain(perm, resource);
    }

    public void ensurePermission(Permission permission, Function function) {
        if (!DatabaseDescriptor.getAuthorizer().requireAuthorization())
            return;
        if (function.isNative())
            return;
        ensurePermissionOnResourceChain(permission, FunctionResource.function(function.name().keyspace, function.name().name, function.argTypes()));
    }

    private void ensurePermissionOnResourceChain(Permission perm, IResource resource) {
        for (IResource r : Resources.chain(resource)) if (authorize(r).contains(perm))
            return;
        throw new UnauthorizedException(String.format("User %s has no %s permission on %s or any of its parents", user.getName(), perm, resource));
    }

    private void preventSystemKSSchemaModification(String keyspace, DataResource resource, Permission perm) {
        if (perm != Permission.ALTER && perm != Permission.DROP && perm != Permission.CREATE)
            return;
        if (SchemaConstants.isLocalSystemKeyspace(keyspace))
            throw new UnauthorizedException(keyspace + " keyspace is not user-modifiable.");
        if (SchemaConstants.isReplicatedSystemKeyspace(keyspace)) {
            if (perm == Permission.ALTER && resource.isKeyspaceLevel())
                return;
            throw new UnauthorizedException(String.format("Cannot %s %s", perm, resource));
        }
    }

    public void validateLogin() {
        if (user == null) {
            throw new UnauthorizedException("You have not logged in");
        } else if (!user.hasLocalAccess()) {
            throw new UnauthorizedException(String.format("You do not have access to this datacenter (%s)", Datacenters.thisDatacenter()));
        }
    }

    public void ensureNotAnonymous() {
        validateLogin();
        if (user.isAnonymous())
            throw new UnauthorizedException("You have to be logged in and not anonymous to perform this request");
    }

    public void ensureIsSuperuser(String message) {
        if (DatabaseDescriptor.getAuthenticator().requireAuthentication() && (user == null || !user.isSuper()))
            throw new UnauthorizedException(message);
    }

    private static void validateKeyspace(String keyspace) {
        if (keyspace == null)
            throw new InvalidRequestException("You have not set a keyspace for this session");
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    private Set<Permission> authorize(IResource resource) {
        return user.getPermissions(resource);
    }
}
