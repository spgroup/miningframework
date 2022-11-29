package com.orientechnologies.orient.core.db.document;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.listener.OListenerManger;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.cache.OCacheLevelOneLocatorImpl;
import com.orientechnologies.orient.core.cache.OLocalRecordCache;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandRequestInternal;
import com.orientechnologies.orient.core.config.OContextConfiguration;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.config.OStorageEntryConfiguration;
import com.orientechnologies.orient.core.conflict.ORecordConflictStrategy;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.db.ODatabaseListener;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.OHookReplacedRecordThreadLocal;
import com.orientechnologies.orient.core.db.OScenarioThreadLocal;
import com.orientechnologies.orient.core.db.record.OClassTrigger;
import com.orientechnologies.orient.core.db.record.OCurrentStorageComponentsFactory;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.db.record.ridbag.sbtree.ORidBagDeleteHook;
import com.orientechnologies.orient.core.db.record.ridbag.sbtree.OSBTreeCollectionManager;
import com.orientechnologies.orient.core.db.record.ridbag.sbtree.OSBTreeCollectionManagerProxy;
import com.orientechnologies.orient.core.dictionary.ODictionary;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.exception.OTransactionBlockedException;
import com.orientechnologies.orient.core.exception.OTransactionException;
import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.fetch.OFetchHelper;
import com.orientechnologies.orient.core.hook.OHookThreadLocal;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OClassIndexManager;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexAbstract;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.intent.OIntent;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.iterator.ORecordIteratorCluster;
import com.orientechnologies.orient.core.metadata.OMetadata;
import com.orientechnologies.orient.core.metadata.OMetadataDefault;
import com.orientechnologies.orient.core.metadata.function.OFunctionTrigger;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OImmutableUser;
import com.orientechnologies.orient.core.metadata.security.ORestrictedAccessHook;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityTrackerHook;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.metadata.security.OUserTrigger;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.record.impl.ODocumentInternal;
import com.orientechnologies.orient.core.schedule.OSchedulerTrigger;
import com.orientechnologies.orient.core.serialization.serializer.OStringSerializerHelper;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.OSerializationSetThreadLocal;
import com.orientechnologies.orient.core.serialization.serializer.record.string.ORecordSerializerSchemaAware2CSV;
import com.orientechnologies.orient.core.storage.OPhysicalPosition;
import com.orientechnologies.orient.core.storage.ORawBuffer;
import com.orientechnologies.orient.core.storage.ORecordCallback;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.OStorageEmbedded;
import com.orientechnologies.orient.core.storage.OStorageOperationResult;
import com.orientechnologies.orient.core.storage.OStorageProxy;
import com.orientechnologies.orient.core.storage.impl.local.OFreezableStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPaginatedStorage;
import com.orientechnologies.orient.core.storage.impl.local.paginated.OOfflineClusterException;
import com.orientechnologies.orient.core.storage.impl.local.paginated.ORecordSerializationContext;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.orientechnologies.orient.core.tx.OTransactionOptimistic;
import com.orientechnologies.orient.core.tx.OTransactionRealAbstract;
import com.orientechnologies.orient.core.type.tree.provider.OMVRBTreeRIDProvider;
import com.orientechnologies.orient.core.version.ORecordVersion;
import com.orientechnologies.orient.core.version.OVersionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings("unchecked")
public class ODatabaseDocumentTx extends OListenerManger<ODatabaseListener> implements ODatabaseDocumentInternal {

    @Deprecated
    private static final String DEF_RECORD_FORMAT = "csv";

    protected static ORecordSerializer defaultSerializer;

    static {
        defaultSerializer = ORecordSerializerFactory.instance().getFormat(OGlobalConfiguration.DB_DOCUMENT_SERIALIZER.getValueAsString());
        if (defaultSerializer == null)
            throw new ODatabaseException("Impossible to find serializer with name " + OGlobalConfiguration.DB_DOCUMENT_SERIALIZER.getValueAsString());
    }

    private final Map<String, Object> properties = new HashMap<String, Object>();

    private final Map<ORecordHook, ORecordHook.HOOK_POSITION> unmodifiableHooks;

    protected ORecordSerializer serializer;

    private String url;

    private OStorage storage;

    private STATUS status;

    private OIntent currentIntent;

    private ODatabaseInternal<?> databaseOwner;

    private OSBTreeCollectionManager sbTreeCollectionManager;

    private OMetadataDefault metadata;

    private OImmutableUser user;

    private byte recordType;

    @Deprecated
    private String recordFormat;

    private Map<ORecordHook, ORecordHook.HOOK_POSITION> hooks = new LinkedHashMap<ORecordHook, ORecordHook.HOOK_POSITION>();

    private boolean retainRecords = true;

    private OLocalRecordCache localCache;

    private boolean mvcc;

    private boolean validation;

    private OCurrentStorageComponentsFactory componentsFactory;

    private boolean initialized = false;

    private OTransaction currentTx;

    private boolean keepStorageOpen = false;

    public ODatabaseDocumentTx(final String iURL) {
        this(iURL, true);
    }

    public ODatabaseDocumentTx(final String iURL, boolean keepStorageOpen) {
        super(false);
        if (iURL == null)
            throw new IllegalArgumentException("URL parameter is null");
        try {
            this.keepStorageOpen = keepStorageOpen;
            url = iURL.replace('\\', '/');
            status = STATUS.CLOSED;
            setProperty("fetch-max", 50);
            storage = Orient.instance().loadStorage(url);
            unmodifiableHooks = Collections.unmodifiableMap(hooks);
            recordType = ODocument.RECORD_TYPE;
            localCache = new OLocalRecordCache(new OCacheLevelOneLocatorImpl());
            mvcc = OGlobalConfiguration.DB_MVCC.getValueAsBoolean();
            validation = OGlobalConfiguration.DB_VALIDATION.getValueAsBoolean();
            init();
            databaseOwner = this;
        } catch (Throwable t) {
            if (storage != null)
                Orient.instance().unregisterStorage(storage);
            throw new ODatabaseException("Error on opening database '" + iURL + "'", t);
        }
        setSerializer(defaultSerializer);
    }

    public static ORecordSerializer getDefaultSerializer() {
        return defaultSerializer;
    }

    public static void setDefaultSerializer(ORecordSerializer iDefaultSerializer) {
        defaultSerializer = iDefaultSerializer;
    }

    @Override
    public <DB extends ODatabase> DB open(final String iUserName, final String iUserPassword) {
        setCurrentDatabaseInThreadLocal();
        try {
            if (status == STATUS.OPEN)
                throw new IllegalStateException("Database " + getName() + " is already open");
            if (user != null && !user.getName().equals(iUserName))
                initialized = false;
            if (storage.isClosed()) {
                storage.open(iUserName, iUserPassword, properties);
            } else if (storage instanceof OStorageProxy) {
                final String name = ((OStorageProxy) storage).getUserName();
                if (!name.equals(iUserName)) {
                    storage.close();
                    storage.open(iUserName, iUserPassword, properties);
                }
            }
            status = STATUS.OPEN;
            initAtFirstOpen(iUserName, iUserPassword);
            if (!(getStorage() instanceof OStorageProxy)) {
                final OSecurity security = metadata.getSecurity();
                if (user == null || user.getVersion() != security.getVersion() || !user.getName().equalsIgnoreCase(iUserName)) {
                    final OUser usr = metadata.getSecurity().authenticate(iUserName, iUserPassword);
                    if (usr != null)
                        user = new OImmutableUser(security.getVersion(), usr);
                    else
                        user = null;
                    checkSecurity(ORule.ResourceGeneric.DATABASE, ORole.PERMISSION_READ);
                }
            }
            callOnOpenListeners();
        } catch (OException e) {
            close();
            throw e;
        } catch (Exception e) {
            close();
            throw new ODatabaseException("Cannot open database", e);
        }
        return (DB) this;
    }

    public void callOnOpenListeners() {
        for (Iterator<ODatabaseLifecycleListener> it = Orient.instance().getDbLifecycleListeners(); it.hasNext(); ) it.next().onOpen(getDatabaseOwner());
        for (ODatabaseListener listener : getListenersCopy()) try {
            listener.onOpen(getDatabaseOwner());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public <DB extends ODatabase> DB create() {
        return create(null);
    }

    @Override
    public <DB extends ODatabase> DB create(final Map<OGlobalConfiguration, Object> iInitialSettings) {
        setCurrentDatabaseInThreadLocal();
        try {
            if (status == STATUS.OPEN)
                throw new IllegalStateException("Database " + getName() + " is already open");
            if (storage == null)
                storage = Orient.instance().loadStorage(url);
            if (iInitialSettings != null) {
                final OContextConfiguration ctxCfg = storage.getConfiguration().getContextConfiguration();
                for (Map.Entry<OGlobalConfiguration, Object> e : iInitialSettings.entrySet()) {
                    ctxCfg.setValue(e.getKey(), e.getValue());
                }
            }
            storage.create(properties);
            status = STATUS.OPEN;
            componentsFactory = getStorage().getComponentsFactory();
            sbTreeCollectionManager = new OSBTreeCollectionManagerProxy(this, getStorage().getResource(OSBTreeCollectionManager.class.getSimpleName(), new Callable<OSBTreeCollectionManager>() {

                @Override
                public OSBTreeCollectionManager call() throws Exception {
                    Class<? extends OSBTreeCollectionManager> managerClass = getStorage().getCollectionManagerClass();
                    if (managerClass == null) {
                        OLogManager.instance().warn(this, "Current implementation of storage does not support sbtree collections");
                        return null;
                    } else {
                        return managerClass.newInstance();
                    }
                }
            }));
            localCache.startup();
            getStorage().getConfiguration().setRecordSerializer(getSerializer().toString());
            getStorage().getConfiguration().setRecordSerializerVersion(getSerializer().getCurrentVersion());
            getStorage().getConfiguration().update();
            if (!(getStorage() instanceof OStorageProxy))
                installHooks();
            metadata = new OMetadataDefault();
            metadata.create();
            registerHook(new OSecurityTrackerHook(metadata.getSecurity()), ORecordHook.HOOK_POSITION.LAST);
            final OUser usr = getMetadata().getSecurity().getUser(OUser.ADMIN);
            if (usr == null)
                user = null;
            else
                user = new OImmutableUser(getMetadata().getSecurity().getVersion(), usr);
            if (!metadata.getSchema().existsClass(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME))
                metadata.getSchema().createClass(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME);
            for (Iterator<ODatabaseLifecycleListener> it = Orient.instance().getDbLifecycleListeners(); it.hasNext(); ) it.next().onCreate(getDatabaseOwner());
            for (ODatabaseListener listener : browseListeners()) try {
                listener.onCreate(this);
            } catch (Throwable ignore) {
            }
        } catch (Exception e) {
            throw new ODatabaseException("Cannot create database '" + getName() + "'", e);
        }
        return (DB) this;
    }

    @Override
    public void drop() {
        checkOpeness();
        checkSecurity(ORule.ResourceGeneric.DATABASE, ORole.PERMISSION_DELETE);
        setCurrentDatabaseInThreadLocal();
        callOnCloseListeners();
        if (metadata != null) {
            metadata.close();
            metadata = null;
        }
        final Iterable<ODatabaseListener> tmpListeners = getListenersCopy();
        closeOnDelete();
        try {
            if (storage == null)
                storage = Orient.instance().loadStorage(url);
            storage.delete();
            storage = null;
            for (ODatabaseListener listener : tmpListeners) try {
                listener.onDelete(this);
            } catch (Throwable t) {
            }
            status = STATUS.CLOSED;
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        } catch (OException e) {
            throw e;
        } catch (Exception e) {
            throw new ODatabaseException("Cannot delete database", e);
        }
    }

    public void callOnCloseListeners() {
        for (Iterator<ODatabaseLifecycleListener> it = Orient.instance().getDbLifecycleListeners(); it.hasNext(); ) it.next().onClose(getDatabaseOwner());
        for (ODatabaseListener listener : getListenersCopy()) try {
            listener.onClose(getDatabaseOwner());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public <RET extends ORecord> RET getRecord(final OIdentifiable iIdentifiable) {
        if (iIdentifiable instanceof ORecord)
            return (RET) iIdentifiable;
        return (RET) load(iIdentifiable.getIdentity());
    }

    @Override
    public void reload() {
        metadata.reload();
        storage.reload();
    }

    public <RET extends ORecord> RET load(final ORID iRecordId, final String iFetchPlan, final boolean iIgnoreCache) {
        return (RET) executeReadRecord((ORecordId) iRecordId, null, iFetchPlan, iIgnoreCache, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    public ODatabase<ORecord> delete(final ORID iRecord, final ORecordVersion iVersion) {
        executeDeleteRecord(iRecord, iVersion, true, true, OPERATION_MODE.SYNCHRONOUS, false);
        return this;
    }

    public ODatabase<ORecord> cleanOutRecord(final ORID iRecord, final ORecordVersion iVersion) {
        executeDeleteRecord(iRecord, iVersion, true, true, OPERATION_MODE.SYNCHRONOUS, true);
        return this;
    }

    public ORecord getRecordByUserObject(final Object iUserObject, final boolean iCreateIfNotAvailable) {
        return (ORecord) iUserObject;
    }

    public void registerUserObject(final Object iObject, final ORecord iRecord) {
    }

    public void registerUserObjectAfterLinkSave(ORecord iRecord) {
    }

    public Object getUserObjectByRecord(final OIdentifiable record, final String iFetchPlan) {
        return record;
    }

    public boolean existsUserObjectByRID(final ORID iRID) {
        return true;
    }

    public String getType() {
        return TYPE;
    }

    public ODatabaseDocument delete(final ORID iRecord, final OPERATION_MODE iMode) {
        ORecord record = iRecord.getRecord();
        if (record == null)
            return this;
        delete(record, iMode);
        return this;
    }

    public ODatabaseDocument delete(final ORecord iRecord, final OPERATION_MODE iMode) {
        currentTx.deleteRecord(iRecord, iMode);
        return this;
    }

    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(final String iClusterName, final Class<REC> iClass) {
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, iClusterName);
        setCurrentDatabaseInThreadLocal();
        final int clusterId = getClusterIdByName(iClusterName);
        return new ORecordIteratorCluster<REC>(this, this, clusterId, true);
    }

    @Override
    public <REC extends ORecord> ORecordIteratorCluster<REC> browseCluster(final String iClusterName, final Class<REC> iRecordClass, final long startClusterPosition, final long endClusterPosition, final boolean loadTombstones) {
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, iClusterName);
        setCurrentDatabaseInThreadLocal();
        final int clusterId = getClusterIdByName(iClusterName);
        return new ORecordIteratorCluster<REC>(this, this, clusterId, startClusterPosition, endClusterPosition, true, loadTombstones, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    public OCommandRequest command(final OCommandRequest iCommand) {
        checkSecurity(ORule.ResourceGeneric.COMMAND, ORole.PERMISSION_READ);
        setCurrentDatabaseInThreadLocal();
        final OCommandRequestInternal command = (OCommandRequestInternal) iCommand;
        try {
            command.reset();
            return command;
        } catch (Exception e) {
            throw new ODatabaseException("Error on command execution", e);
        }
    }

    public <RET extends List<?>> RET query(final OQuery<?> iCommand, final Object... iArgs) {
        setCurrentDatabaseInThreadLocal();
        iCommand.reset();
        return (RET) iCommand.execute(iArgs);
    }

    public byte getRecordType() {
        return recordType;
    }

    @Override
    public long countClusterElements(final int[] iClusterIds) {
        return countClusterElements(iClusterIds, false);
    }

    @Override
    public long countClusterElements(final int iClusterId) {
        return countClusterElements(iClusterId, false);
    }

    @Override
    public long countClusterElements(int iClusterId, boolean countTombstones) {
        final String name = getClusterNameById(iClusterId);
        if (name == null)
            return 0;
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, name);
        setCurrentDatabaseInThreadLocal();
        return storage.count(iClusterId, countTombstones);
    }

    @Override
    public long countClusterElements(int[] iClusterIds, boolean countTombstones) {
        String name;
        for (int iClusterId : iClusterIds) {
            name = getClusterNameById(iClusterId);
            checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, name);
        }
        return storage.count(iClusterIds, countTombstones);
    }

    @Override
    public long countClusterElements(final String iClusterName) {
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, iClusterName);
        setCurrentDatabaseInThreadLocal();
        final int clusterId = getClusterIdByName(iClusterName);
        if (clusterId < 0)
            throw new IllegalArgumentException("Cluster '" + iClusterName + "' was not found");
        return storage.count(clusterId);
    }

    public OMetadataDefault getMetadata() {
        checkOpeness();
        return metadata;
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(ORule.ResourceGeneric resourceGeneric, String resourceSpecific, final int iOperation) {
        if (user != null) {
            try {
                user.allow(resourceGeneric, resourceSpecific, iOperation);
            } catch (OSecurityAccessException e) {
                if (OLogManager.instance().isDebugEnabled())
                    OLogManager.instance().debug(this, "[checkSecurity] User '%s' tried to access to the reserved resource '%s', operation '%s'", getUser(), resourceGeneric + "." + resourceSpecific, iOperation);
                throw e;
            }
        }
        return (DB) this;
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(final ORule.ResourceGeneric iResourceGeneric, final int iOperation, final Object... iResourcesSpecific) {
        if (user != null) {
            try {
                for (Object target : iResourcesSpecific) {
                    if (target != null) {
                        user.allow(iResourceGeneric, target.toString(), iOperation);
                    } else
                        user.allow(iResourceGeneric, null, iOperation);
                }
            } catch (OSecurityAccessException e) {
                if (OLogManager.instance().isDebugEnabled())
                    OLogManager.instance().debug(this, "[checkSecurity] User '%s' tried to access to the reserved resource '%s', target(s) '%s', operation '%s'", getUser(), iResourceGeneric, Arrays.toString(iResourcesSpecific), iOperation);
                throw e;
            }
        }
        return (DB) this;
    }

    public <DB extends ODatabaseDocument> DB checkSecurity(final ORule.ResourceGeneric iResourceGeneric, final int iOperation, final Object iResourceSpecific) {
        checkOpeness();
        if (user != null) {
            try {
                if (iResourceSpecific != null)
                    user.allow(iResourceGeneric, iResourceSpecific.toString(), iOperation);
                else
                    user.allow(iResourceGeneric, null, iOperation);
            } catch (OSecurityAccessException e) {
                if (OLogManager.instance().isDebugEnabled())
                    OLogManager.instance().debug(this, "[checkSecurity] User '%s' tried to access to the reserved resource '%s', target '%s', operation '%s'", getUser(), iResourceGeneric, iResourceSpecific, iOperation);
                throw e;
            }
        }
        return (DB) this;
    }

    @Override
    public ODatabaseInternal<?> getDatabaseOwner() {
        ODatabaseInternal<?> current = databaseOwner;
        while (current != null && current != this && current.getDatabaseOwner() != current) current = current.getDatabaseOwner();
        return current;
    }

    @Override
    public ODatabaseInternal<ORecord> setDatabaseOwner(ODatabaseInternal<?> iOwner) {
        databaseOwner = iOwner;
        return this;
    }

    public boolean isRetainRecords() {
        return retainRecords;
    }

    public ODatabaseDocument setRetainRecords(boolean retainRecords) {
        this.retainRecords = retainRecords;
        return this;
    }

    public <DB extends ODatabase> DB setStatus(final STATUS status) {
        setStatusInternal(status);
        return (DB) this;
    }

    public void setStatusInternal(final STATUS status) {
        this.status = status;
    }

    public void setDefaultClusterIdInternal(final int iDefClusterId) {
        getStorage().setDefaultClusterId(iDefClusterId);
    }

    public void setInternal(final ATTRIBUTES iAttribute, final Object iValue) {
        set(iAttribute, iValue);
    }

    public OSecurityUser getUser() {
        return user;
    }

    public void setUser(OSecurityUser user) {
        if (user instanceof OUser) {
            OMetadata metadata = getMetadata();
            if (metadata != null) {
                final OSecurity security = metadata.getSecurity();
                this.user = new OImmutableUser(security.getVersion(), (OUser) user);
            } else
                this.user = new OImmutableUser(-1, (OUser) user);
        } else
            this.user = (OImmutableUser) user;
    }

    public boolean isMVCC() {
        return mvcc;
    }

    public <DB extends ODatabase<?>> DB setMVCC(boolean mvcc) {
        this.mvcc = mvcc;
        return (DB) this;
    }

    public ODictionary<ORecord> getDictionary() {
        checkOpeness();
        return metadata.getIndexManager().getDictionary();
    }

    public <DB extends ODatabase<?>> DB registerHook(final ORecordHook iHookImpl, final ORecordHook.HOOK_POSITION iPosition) {
        final Map<ORecordHook, ORecordHook.HOOK_POSITION> tmp = new LinkedHashMap<ORecordHook, ORecordHook.HOOK_POSITION>(hooks);
        tmp.put(iHookImpl, iPosition);
        hooks.clear();
        for (ORecordHook.HOOK_POSITION p : ORecordHook.HOOK_POSITION.values()) {
            for (Map.Entry<ORecordHook, ORecordHook.HOOK_POSITION> e : tmp.entrySet()) {
                if (e.getValue() == p)
                    hooks.put(e.getKey(), e.getValue());
            }
        }
        return (DB) this;
    }

    public <DB extends ODatabase<?>> DB registerHook(final ORecordHook iHookImpl) {
        return (DB) registerHook(iHookImpl, ORecordHook.HOOK_POSITION.REGULAR);
    }

    public <DB extends ODatabase<?>> DB unregisterHook(final ORecordHook iHookImpl) {
        if (iHookImpl != null) {
            iHookImpl.onUnregister();
            hooks.remove(iHookImpl);
        }
        return (DB) this;
    }

    @Override
    public OLocalRecordCache getLocalCache() {
        return localCache;
    }

    public Map<ORecordHook, ORecordHook.HOOK_POSITION> getHooks() {
        return unmodifiableHooks;
    }

    public ORecordHook.RESULT callbackHooks(final ORecordHook.TYPE iType, final OIdentifiable id) {
        if (id == null || !OHookThreadLocal.INSTANCE.push(id))
            return ORecordHook.RESULT.RECORD_NOT_CHANGED;
        try {
            final ORecord rec = id.getRecord();
            if (rec == null)
                return ORecordHook.RESULT.RECORD_NOT_CHANGED;
            OScenarioThreadLocal.RUN_MODE runMode = OScenarioThreadLocal.INSTANCE.get();
            boolean recordChanged = false;
            for (ORecordHook hook : hooks.keySet()) {
                switch(runMode) {
                    case DEFAULT:
                        if (getStorage().isDistributed() && hook.getDistributedExecutionMode() == ORecordHook.DISTRIBUTED_EXECUTION_MODE.TARGET_NODE)
                            continue;
                        break;
                    case RUNNING_DISTRIBUTED:
                        if (hook.getDistributedExecutionMode() == ORecordHook.DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE)
                            continue;
                }
                final ORecordHook.RESULT res = hook.onTrigger(iType, rec);
                if (res == ORecordHook.RESULT.RECORD_CHANGED)
                    recordChanged = true;
                else if (res == ORecordHook.RESULT.SKIP_IO)
                    return res;
                else if (res == ORecordHook.RESULT.SKIP)
                    return res;
                else if (res == ORecordHook.RESULT.RECORD_REPLACED)
                    return res;
            }
            return recordChanged ? ORecordHook.RESULT.RECORD_CHANGED : ORecordHook.RESULT.RECORD_NOT_CHANGED;
        } finally {
            OHookThreadLocal.INSTANCE.pop(id);
        }
    }

    public boolean isValidationEnabled() {
        return !getStatus().equals(STATUS.IMPORTING) && validation;
    }

    public <DB extends ODatabaseDocument> DB setValidationEnabled(final boolean iEnabled) {
        validation = iEnabled;
        return (DB) this;
    }

    public ORecordConflictStrategy getConflictStrategy() {
        return getStorage().getConflictStrategy();
    }

    public ODatabaseDocumentTx setConflictStrategy(final ORecordConflictStrategy iResolver) {
        getStorage().setConflictStrategy(iResolver);
        return this;
    }

    public ODatabaseDocumentTx setConflictStrategy(final String iStrategyName) {
        getStorage().setConflictStrategy(Orient.instance().getRecordConflictStrategy().getStrategy(iStrategyName));
        return this;
    }

    @Override
    public OContextConfiguration getConfiguration() {
        if (storage != null)
            return storage.getConfiguration().getContextConfiguration();
        return null;
    }

    @Override
    public boolean declareIntent(OIntent iIntent) {
        if (currentIntent != null) {
            if (iIntent != null && iIntent.getClass().equals(currentIntent.getClass()))
                return false;
            currentIntent.end(this);
        }
        currentIntent = iIntent;
        if (iIntent != null)
            iIntent.begin(this);
        return true;
    }

    @Override
    public boolean exists() {
        if (status == STATUS.OPEN)
            return true;
        if (storage == null)
            storage = Orient.instance().loadStorage(url);
        return storage.exists();
    }

    @Override
    public void close() {
        if (isClosed())
            return;
        setCurrentDatabaseInThreadLocal();
        try {
            commit(true);
        } catch (Exception e) {
            OLogManager.instance().error(this, "Exception during commit of active transaction.", e);
        }
        if (status != STATUS.OPEN)
            return;
        callOnCloseListeners();
        if (currentIntent != null) {
            currentIntent.end(this);
            currentIntent = null;
        }
        status = STATUS.CLOSED;
        localCache.clear();
        if (!keepStorageOpen && storage != null)
            storage.close();
        ODatabaseRecordThreadLocal.INSTANCE.remove();
    }

    @Override
    public STATUS getStatus() {
        return status;
    }

    @Override
    public long getSize() {
        return storage.getSize();
    }

    @Override
    public String getName() {
        return storage != null ? storage.getName() : url;
    }

    @Override
    public String getURL() {
        return url != null ? url : storage.getURL();
    }

    @Override
    public int getDefaultClusterId() {
        return storage.getDefaultClusterId();
    }

    @Override
    public int getClusters() {
        return storage.getClusters();
    }

    @Override
    public boolean existsCluster(String iClusterName) {
        return storage.getClusterNames().contains(iClusterName.toLowerCase());
    }

    @Override
    public Collection<String> getClusterNames() {
        return storage.getClusterNames();
    }

    @Override
    public int getClusterIdByName(String iClusterName) {
        if (iClusterName == null)
            return -1;
        return storage.getClusterIdByName(iClusterName.toLowerCase());
    }

    @Override
    public String getClusterNameById(int iClusterId) {
        if (iClusterId == -1)
            return null;
        return storage.getPhysicalClusterNameById(iClusterId);
    }

    @Override
    public long getClusterRecordSizeByName(String clusterName) {
        try {
            return storage.getClusterById(getClusterIdByName(clusterName)).getRecordsSize();
        } catch (Exception e) {
            throw new ODatabaseException("Error on reading records size for cluster '" + clusterName + "'", e);
        }
    }

    @Override
    public long getClusterRecordSizeById(int clusterId) {
        try {
            return storage.getClusterById(clusterId).getRecordsSize();
        } catch (Exception e) {
            throw new ODatabaseException("Error on reading records size for cluster with id '" + clusterId + "'", e);
        }
    }

    @Override
    public boolean isClosed() {
        return status == STATUS.CLOSED || storage.isClosed();
    }

    @Override
    public int addCluster(String iClusterName, Object... iParameters) {
        return storage.addCluster(iClusterName, false, iParameters);
    }

    @Override
    public int addCluster(String iClusterName, int iRequestedId, Object... iParameters) {
        return storage.addCluster(iClusterName, iRequestedId, false, iParameters);
    }

    @Override
    public boolean dropCluster(String iClusterName, boolean iTruncate) {
        return storage.dropCluster(iClusterName, iTruncate);
    }

    @Override
    public boolean dropCluster(int iClusterId, boolean iTruncate) {
        return storage.dropCluster(iClusterId, iTruncate);
    }

    @Override
    public Object setProperty(String iName, Object iValue) {
        if (iValue == null)
            return properties.remove(iName.toLowerCase());
        else
            return properties.put(iName.toLowerCase(), iValue);
    }

    @Override
    public Object getProperty(String iName) {
        return properties.get(iName.toLowerCase());
    }

    @Override
    public Iterator<Map.Entry<String, Object>> getProperties() {
        return properties.entrySet().iterator();
    }

    @Override
    public Object get(ATTRIBUTES iAttribute) {
        if (iAttribute == null)
            throw new IllegalArgumentException("attribute is null");
        switch(iAttribute) {
            case STATUS:
                return getStatus();
            case DEFAULTCLUSTERID:
                return getDefaultClusterId();
            case TYPE:
                return getMetadata().getImmutableSchemaSnapshot().existsClass("V") ? "graph" : "document";
            case DATEFORMAT:
                return storage.getConfiguration().dateFormat;
            case DATETIMEFORMAT:
                return storage.getConfiguration().dateTimeFormat;
            case TIMEZONE:
                return storage.getConfiguration().getTimeZone().getID();
            case LOCALECOUNTRY:
                return storage.getConfiguration().getLocaleCountry();
            case LOCALELANGUAGE:
                return storage.getConfiguration().getLocaleLanguage();
            case CHARSET:
                return storage.getConfiguration().getCharset();
            case CUSTOM:
                return storage.getConfiguration().properties;
            case CLUSTERSELECTION:
                return storage.getConfiguration().getClusterSelection();
            case MINIMUMCLUSTERS:
                return storage.getConfiguration().getMinimumClusters();
            case CONFLICTSTRATEGY:
                return storage.getConfiguration().getConflictStrategy();
        }
        return null;
    }

    @Override
    public <DB extends ODatabase> DB set(ATTRIBUTES iAttribute, Object iValue) {
        if (iAttribute == null)
            throw new IllegalArgumentException("attribute is null");
        final String stringValue = OStringSerializerHelper.getStringContent(iValue != null ? iValue.toString() : null);
        switch(iAttribute) {
            case STATUS:
                if (stringValue == null)
                    throw new IllegalArgumentException("DB status can't be null");
                setStatus(STATUS.valueOf(stringValue.toUpperCase(Locale.ENGLISH)));
                break;
            case DEFAULTCLUSTERID:
                if (iValue != null) {
                    if (iValue instanceof Number)
                        storage.setDefaultClusterId(((Number) iValue).intValue());
                    else
                        storage.setDefaultClusterId(storage.getClusterIdByName(iValue.toString()));
                }
                break;
            case TYPE:
                throw new IllegalArgumentException("Database type property is not supported");
            case DATEFORMAT:
                new SimpleDateFormat(stringValue).format(new Date());
                storage.getConfiguration().dateFormat = stringValue;
                storage.getConfiguration().update();
                break;
            case DATETIMEFORMAT:
                new SimpleDateFormat(stringValue).format(new Date());
                storage.getConfiguration().dateTimeFormat = stringValue;
                storage.getConfiguration().update();
                break;
            case TIMEZONE:
                if (stringValue == null)
                    throw new IllegalArgumentException("Timezone can't be null");
                storage.getConfiguration().setTimeZone(TimeZone.getTimeZone(stringValue.toUpperCase()));
                storage.getConfiguration().update();
                break;
            case LOCALECOUNTRY:
                storage.getConfiguration().setLocaleCountry(stringValue);
                storage.getConfiguration().update();
                break;
            case LOCALELANGUAGE:
                storage.getConfiguration().setLocaleLanguage(stringValue);
                storage.getConfiguration().update();
                break;
            case CHARSET:
                storage.getConfiguration().setCharset(stringValue);
                storage.getConfiguration().update();
                break;
            case CUSTOM:
                int indx = stringValue != null ? stringValue.indexOf('=') : -1;
                if (indx < 0) {
                    if ("clear".equalsIgnoreCase(stringValue)) {
                        clearCustomInternal();
                    } else
                        throw new IllegalArgumentException("Syntax error: expected <name> = <value> or clear, instead found: " + iValue);
                } else {
                    String customName = stringValue.substring(0, indx).trim();
                    String customValue = stringValue.substring(indx + 1).trim();
                    if (customValue.isEmpty())
                        removeCustomInternal(customName);
                    else
                        setCustomInternal(customName, customValue);
                }
                break;
            case CLUSTERSELECTION:
                storage.getConfiguration().setClusterSelection(stringValue);
                storage.getConfiguration().update();
                break;
            case MINIMUMCLUSTERS:
                if (iValue != null) {
                    if (iValue instanceof Number)
                        storage.getConfiguration().setMinimumClusters(((Number) iValue).intValue());
                    else
                        storage.getConfiguration().setMinimumClusters(Integer.parseInt(stringValue));
                } else
                    storage.getConfiguration().setMinimumClusters(1);
                storage.getConfiguration().update();
                break;
            case CONFLICTSTRATEGY:
                storage.getConfiguration().setConflictStrategy(stringValue);
                storage.getConfiguration().update();
                break;
            default:
                throw new IllegalArgumentException("Option '" + iAttribute + "' not supported on alter database");
        }
        return (DB) this;
    }

    @Override
    public ORecordMetadata getRecordMetadata(ORID rid) {
        return storage.getRecordMetadata(rid);
    }

    @Override
    public void freezeCluster(int iClusterId) {
        freezeCluster(iClusterId, false);
    }

    @Override
    public void releaseCluster(int iClusterId) {
        final OLocalPaginatedStorage storage;
        if (getStorage() instanceof OLocalPaginatedStorage)
            storage = ((OLocalPaginatedStorage) getStorage());
        else {
            OLogManager.instance().error(this, "We can not freeze non local storage.");
            return;
        }
        storage.release(iClusterId);
    }

    @Override
    public void freezeCluster(int iClusterId, boolean throwException) {
        if (getStorage() instanceof OLocalPaginatedStorage) {
            final OLocalPaginatedStorage paginatedStorage = ((OLocalPaginatedStorage) getStorage());
            paginatedStorage.freeze(throwException, iClusterId);
        } else {
            OLogManager.instance().error(this, "Only local paginated storage supports cluster freeze.");
        }
    }

    public OTransaction getTransaction() {
        return currentTx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(final ORecord iRecord, final String iFetchPlan) {
        return (RET) currentTx.loadRecord(iRecord.getIdentity(), iRecord, iFetchPlan, false, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(ORecord iRecord, String iFetchPlan, boolean iIgnoreCache, boolean loadTombstone, OStorage.LOCKING_STRATEGY iLockingStrategy) {
        return (RET) currentTx.loadRecord(iRecord.getIdentity(), iRecord, iFetchPlan, iIgnoreCache, loadTombstone, iLockingStrategy);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(final ORecord iRecord) {
        return (RET) currentTx.loadRecord(iRecord.getIdentity(), iRecord, null, false, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(final ORID recordId) {
        return (RET) currentTx.loadRecord(recordId, null, null, false, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(final ORID iRecordId, final String iFetchPlan) {
        return (RET) currentTx.loadRecord(iRecordId, null, iFetchPlan, false, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET load(final ORID iRecordId, String iFetchPlan, final boolean iIgnoreCache, final boolean loadTombstone, OStorage.LOCKING_STRATEGY iLockingStrategy) {
        return (RET) currentTx.loadRecord(iRecordId, null, iFetchPlan, iIgnoreCache, loadTombstone, iLockingStrategy);
    }

    @SuppressWarnings("unchecked")
    public <RET extends ORecord> RET reload(final ORecord iRecord) {
        return reload(iRecord, null, false);
    }

    @SuppressWarnings("unchecked")
    public <RET extends ORecord> RET reload(final ORecord iRecord, final String iFetchPlan) {
        return reload(iRecord, iFetchPlan, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RET extends ORecord> RET reload(final ORecord iRecord, final String iFetchPlan, final boolean iIgnoreCache) {
        ORecord record = currentTx.loadRecord(iRecord.getIdentity(), iRecord, iFetchPlan, iIgnoreCache, false, OStorage.LOCKING_STRATEGY.DEFAULT);
        if (record != null && iRecord != record) {
            iRecord.fromStream(record.toStream());
            iRecord.getRecordVersion().copyFrom(record.getRecordVersion());
        } else if (record == null)
            throw new ORecordNotFoundException("Record with rid " + iRecord.getIdentity() + " was not found in database");
        return (RET) record;
    }

    public ODatabaseDocument delete(final ORID iRecord) {
        checkOpeness();
        final ORecord rec = iRecord.getRecord();
        if (rec != null)
            rec.delete();
        return this;
    }

    @Override
    public boolean hide(ORID rid) {
        checkOpeness();
        if (currentTx.isActive())
            throw new ODatabaseException("This operation can be executed only in non tx mode");
        return executeHideRecord(rid, OPERATION_MODE.SYNCHRONOUS);
    }

    @Override
    public OBinarySerializerFactory getSerializerFactory() {
        return componentsFactory.binarySerializerFactory;
    }

    public ODatabaseDocument begin(final OTransaction iTx) {
        checkOpeness();
        if (currentTx.isActive() && iTx.equals(currentTx)) {
            currentTx.begin();
            return this;
        }
        currentTx.rollback(true, 0);
        for (ODatabaseListener listener : browseListeners()) try {
            listener.onBeforeTxBegin(this);
        } catch (Throwable t) {
            OLogManager.instance().error(this, "Error before the transaction begin", t, OTransactionBlockedException.class);
        }
        currentTx = iTx;
        currentTx.begin();
        return this;
    }

    public <RET extends ORecord> RET load(final ORecord iRecord, final String iFetchPlan, final boolean iIgnoreCache) {
        return (RET) executeReadRecord((ORecordId) iRecord.getIdentity(), iRecord, iFetchPlan, iIgnoreCache, false, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    public <RET extends ORecord> RET executeReadRecord(final ORecordId rid, ORecord iRecord, final String iFetchPlan, final boolean iIgnoreCache, final boolean loadTombstones, final OStorage.LOCKING_STRATEGY iLockingStrategy) {
        checkOpeness();
        getMetadata().makeThreadLocalSchemaSnapshot();
        ORecordSerializationContext.pushContext();
        try {
            checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, getClusterNameById(rid.getClusterId()));
            ORecord record = getTransaction().getRecord(rid);
            if (record == OTransactionRealAbstract.DELETED_RECORD)
                return null;
            if (record == null && !iIgnoreCache)
                record = getLocalCache().findRecord(rid);
            if (record != null) {
                if (iRecord != null) {
                    iRecord.fromStream(record.toStream());
                    iRecord.getRecordVersion().copyFrom(record.getRecordVersion());
                    record = iRecord;
                }
                OFetchHelper.checkFetchPlanValid(iFetchPlan);
                if (callbackHooks(ORecordHook.TYPE.BEFORE_READ, record) == ORecordHook.RESULT.SKIP)
                    return null;
                if (record.getInternalStatus() == ORecordElement.STATUS.NOT_LOADED)
                    record.reload();
                if (iLockingStrategy == OStorage.LOCKING_STRATEGY.KEEP_SHARED_LOCK)
                    record.lock(false);
                else if (iLockingStrategy == OStorage.LOCKING_STRATEGY.KEEP_EXCLUSIVE_LOCK)
                    record.lock(true);
                callbackHooks(ORecordHook.TYPE.AFTER_READ, record);
                return (RET) record;
            }
            final ORawBuffer recordBuffer;
            if (!rid.isValid())
                recordBuffer = null;
            else {
                OFetchHelper.checkFetchPlanValid(iFetchPlan);
                recordBuffer = storage.readRecord(rid, iFetchPlan, iIgnoreCache, null, loadTombstones, iLockingStrategy).getResult();
            }
            if (recordBuffer == null)
                return null;
            if (iRecord == null || ORecordInternal.getRecordType(iRecord) != recordBuffer.recordType)
                iRecord = Orient.instance().getRecordFactoryManager().newInstance(recordBuffer.recordType);
            ORecordInternal.fill(iRecord, rid, recordBuffer.version, recordBuffer.buffer, false);
            if (iRecord.getRecordVersion().isTombstone())
                return (RET) iRecord;
            if (callbackHooks(ORecordHook.TYPE.BEFORE_READ, iRecord) == ORecordHook.RESULT.SKIP)
                return null;
            iRecord.fromStream(recordBuffer.buffer);
            callbackHooks(ORecordHook.TYPE.AFTER_READ, iRecord);
            if (!iIgnoreCache)
                getLocalCache().updateRecord(iRecord);
            return (RET) iRecord;
        } catch (OOfflineClusterException t) {
            throw t;
        } catch (Throwable t) {
            if (rid.isTemporary())
                throw new ODatabaseException("Error on retrieving record using temporary RecordId: " + rid, t);
            else
                throw new ODatabaseException("Error on retrieving record " + rid + " (cluster: " + storage.getPhysicalClusterNameById(rid.clusterId) + ")", t);
        } finally {
            ORecordSerializationContext.pullContext();
            getMetadata().clearThreadLocalSchemaSnapshot();
        }
    }

    public <RET extends ORecord> RET executeSaveRecord(final ORecord record, String iClusterName, final ORecordVersion iVersion, boolean iCallTriggers, final OPERATION_MODE iMode, boolean iForceCreate, final ORecordCallback<? extends Number> iRecordCreatedCallback, ORecordCallback<ORecordVersion> iRecordUpdatedCallback) {
        checkOpeness();
        setCurrentDatabaseInThreadLocal();
        if (!record.isDirty())
            return (RET) record;
        final ORecordId rid = (ORecordId) record.getIdentity();
        if (rid == null)
            throw new ODatabaseException("Cannot create record because it has no identity. Probably is not a regular record or contains projections of fields rather than a full record");
        final Set<OIndex<?>> lockedIndexes = new HashSet<OIndex<?>>();
        record.setInternalStatus(ORecordElement.STATUS.MARSHALLING);
        try {
            if (record instanceof ODocument)
                acquireIndexModificationLock((ODocument) record, lockedIndexes);
            final boolean wasNew = iForceCreate || rid.isNew();
            if (wasNew && rid.clusterId == -1)
                rid.clusterId = iClusterName != null ? getClusterIdByName(iClusterName) : getDefaultClusterId();
            byte[] stream;
            final OStorageOperationResult<ORecordVersion> operationResult;
            getMetadata().makeThreadLocalSchemaSnapshot();
            ORecordSerializationContext.pushContext();
            try {
                stream = record.toStream();
                final boolean isNew = iForceCreate || rid.isNew();
                if (isNew)
                    ORecordInternal.onBeforeIdentityChanged(record);
                else if (stream == null || stream.length == 0)
                    return (RET) record;
                if (isNew && rid.clusterId < 0)
                    rid.clusterId = iClusterName != null ? getClusterIdByName(iClusterName) : getDefaultClusterId();
                if (rid.clusterId > -1 && iClusterName == null)
                    iClusterName = getClusterNameById(rid.clusterId);
                checkRecordClass(record, iClusterName, rid, isNew);
                checkSecurity(ORule.ResourceGeneric.CLUSTER, wasNew ? ORole.PERMISSION_CREATE : ORole.PERMISSION_UPDATE, iClusterName);
                final boolean partialMarshalling = record instanceof ODocument && OSerializationSetThreadLocal.INSTANCE.checkIfPartial((ODocument) record);
                if (stream != null && stream.length > 0 && !partialMarshalling) {
                    if (iCallTriggers) {
                        final ORecordHook.TYPE triggerType = wasNew ? ORecordHook.TYPE.BEFORE_CREATE : ORecordHook.TYPE.BEFORE_UPDATE;
                        final ORecordHook.RESULT hookResult = callbackHooks(triggerType, record);
                        if (hookResult == ORecordHook.RESULT.RECORD_CHANGED)
                            stream = updateStream(record);
                        else if (hookResult == ORecordHook.RESULT.SKIP_IO)
                            return (RET) record;
                        else if (hookResult == ORecordHook.RESULT.RECORD_REPLACED)
                            return (RET) OHookReplacedRecordThreadLocal.INSTANCE.get();
                    }
                }
                if (wasNew && !isNew)
                    record.setDirty();
                else if (!record.isDirty())
                    return (RET) record;
                final ORecordVersion realVersion = !mvcc || iVersion.isUntracked() ? OVersionFactory.instance().createUntrackedVersion() : record.getRecordVersion();
                try {
                    boolean updateContent = ORecordInternal.isContentChanged(record);
                    byte[] content = (stream == null) ? new byte[0] : stream;
                    byte recordType = ORecordInternal.getRecordType(record);
                    int mode = iMode.ordinal();
                    Orient.instance().getRecordFactoryManager().getRecordTypeClass(recordType);
                    if (iForceCreate || ORecordId.isNew(rid.clusterPosition)) {
                        final OStorageOperationResult<OPhysicalPosition> ppos = storage.createRecord(rid, content, iVersion, recordType, mode, (ORecordCallback<Long>) iRecordCreatedCallback);
                        operationResult = new OStorageOperationResult<ORecordVersion>(ppos.getResult().recordVersion, ppos.isMoved());
                    } else {
                        operationResult = storage.updateRecord(rid, updateContent, content, iVersion, recordType, mode, iRecordUpdatedCallback);
                    }
                    final ORecordVersion version = operationResult.getResult();
                    if (isNew) {
                        ((ORecordId) record.getIdentity()).copyFrom(rid);
                        ORecordInternal.onAfterIdentityChanged(record);
                    }
                    if (operationResult.getModifiedRecordContent() != null)
                        stream = operationResult.getModifiedRecordContent();
                    ORecordInternal.fill(record, rid, version, stream, partialMarshalling);
                    callbackHookSuccess(record, iCallTriggers, wasNew, stream, operationResult);
                } catch (Throwable t) {
                    callbackHookFailure(record, iCallTriggers, wasNew, stream);
                    throw t;
                }
            } finally {
                ORecordSerializationContext.pullContext();
                getMetadata().clearThreadLocalSchemaSnapshot();
            }
            if (stream != null && stream.length > 0 && !operationResult.isMoved())
                getLocalCache().updateRecord(record);
        } catch (OException e) {
            throw e;
        } catch (Throwable t) {
            if (!ORecordId.isValid(record.getIdentity().getClusterPosition()))
                throw new ODatabaseException("Error on saving record in cluster #" + record.getIdentity().getClusterId(), t);
            else
                throw new ODatabaseException("Error on saving record " + record.getIdentity(), t);
        } finally {
            releaseIndexModificationLock(lockedIndexes);
            record.setInternalStatus(ORecordElement.STATUS.LOADED);
        }
        return (RET) record;
    }

    public void executeDeleteRecord(OIdentifiable record, final ORecordVersion iVersion, final boolean iRequired, boolean iCallTriggers, final OPERATION_MODE iMode, boolean prohibitTombstones) {
        checkOpeness();
        final ORecordId rid = (ORecordId) record.getIdentity();
        if (rid == null)
            throw new ODatabaseException("Cannot delete record because it has no identity. Probably was created from scratch or contains projections of fields rather than a full record");
        if (!rid.isValid())
            return;
        record = record.getRecord();
        if (record == null)
            return;
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_DELETE, getClusterNameById(rid.clusterId));
        final Set<OIndex<?>> lockedIndexes = new HashSet<OIndex<?>>();
        setCurrentDatabaseInThreadLocal();
        ORecordSerializationContext.pushContext();
        getMetadata().makeThreadLocalSchemaSnapshot();
        try {
            if (record instanceof ODocument)
                acquireIndexModificationLock((ODocument) record, lockedIndexes);
            try {
                ORecord rec = record.getRecord();
                if (iCallTriggers && rec != null)
                    callbackHooks(ORecordHook.TYPE.BEFORE_DELETE, rec);
                final ORecordVersion realVersion = mvcc ? iVersion : OVersionFactory.instance().createUntrackedVersion();
                final OStorageOperationResult<Boolean> operationResult;
                try {
                    if (prohibitTombstones) {
                        final boolean result = storage.cleanOutRecord(rid, iVersion, iMode.ordinal(), null);
                        if (!result && iRequired)
                            throw new ORecordNotFoundException("The record with id " + rid + " was not found");
                        operationResult = new OStorageOperationResult<Boolean>(result);
                    } else {
                        final OStorageOperationResult<Boolean> result = storage.deleteRecord(rid, iVersion, iMode.ordinal(), null);
                        if (!result.getResult() && iRequired)
                            throw new ORecordNotFoundException("The record with id " + rid + " was not found");
                        operationResult = new OStorageOperationResult<Boolean>(result.getResult());
                    }
                    if (iCallTriggers) {
                        if (!operationResult.isMoved() && rec != null)
                            callbackHooks(ORecordHook.TYPE.AFTER_DELETE, rec);
                        else if (rec != null)
                            callbackHooks(ORecordHook.TYPE.DELETE_REPLICATED, rec);
                    }
                } catch (Throwable t) {
                    if (iCallTriggers)
                        callbackHooks(ORecordHook.TYPE.DELETE_FAILED, rec);
                    throw t;
                }
                clearDocumentTracking(rec);
                if (!operationResult.isMoved()) {
                    getLocalCache().deleteRecord(rid);
                }
            } catch (OException e) {
                throw e;
            } catch (Throwable t) {
                throw new ODatabaseException("Error on deleting record in cluster #" + record.getIdentity().getClusterId(), t);
            }
        } finally {
            releaseIndexModificationLock(lockedIndexes);
            ORecordSerializationContext.pullContext();
            getMetadata().clearThreadLocalSchemaSnapshot();
        }
    }

    public boolean executeHideRecord(OIdentifiable record, final OPERATION_MODE iMode) {
        checkOpeness();
        final ORecordId rid = (ORecordId) record.getIdentity();
        if (rid == null)
            throw new ODatabaseException("Cannot hide record because it has no identity. Probably was created from scratch or contains projections of fields rather than a full record");
        if (!rid.isValid())
            return false;
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_DELETE, getClusterNameById(rid.clusterId));
        setCurrentDatabaseInThreadLocal();
        getMetadata().makeThreadLocalSchemaSnapshot();
        ORecordSerializationContext.pushContext();
        try {
            final OStorageOperationResult<Boolean> operationResult;
            operationResult = storage.hideRecord(rid, iMode.ordinal(), null);
            if (!operationResult.isMoved())
                getLocalCache().deleteRecord(rid);
            return operationResult.getResult();
        } finally {
            ORecordSerializationContext.pullContext();
            getMetadata().clearThreadLocalSchemaSnapshot();
        }
    }

    public ODatabaseDocumentTx begin() {
        return begin(OTransaction.TXTYPE.OPTIMISTIC);
    }

    public ODatabaseDocumentTx begin(final OTransaction.TXTYPE iType) {
        checkOpeness();
        setCurrentDatabaseInThreadLocal();
        if (currentTx.isActive()) {
            if (iType == OTransaction.TXTYPE.OPTIMISTIC && currentTx instanceof OTransactionOptimistic) {
                currentTx.begin();
                return this;
            }
            currentTx.rollback(true, 0);
        }
        for (ODatabaseListener listener : browseListeners()) try {
            listener.onBeforeTxBegin(this);
        } catch (Throwable t) {
            OLogManager.instance().error(this, "Error before tx begin", t);
        }
        switch(iType) {
            case NOTX:
                setDefaultTransactionMode();
                break;
            case OPTIMISTIC:
                currentTx = new OTransactionOptimistic(this);
                break;
            case PESSIMISTIC:
                throw new UnsupportedOperationException("Pessimistic transaction");
        }
        currentTx.begin();
        return this;
    }

    public void setDefaultTransactionMode() {
        if (!(currentTx instanceof OTransactionNoTx))
            currentTx = new OTransactionNoTx(this);
    }

    @Override
    public void freeze(final boolean throwException) {
        checkOpeness();
        if (!(getStorage() instanceof OFreezableStorage)) {
            OLogManager.instance().error(this, "We can not freeze non local storage. " + "If you use remote client please use OServerAdmin instead.");
            return;
        }
        final long startTime = Orient.instance().getProfiler().startChrono();
        final Collection<? extends OIndex<?>> indexes = getMetadata().getIndexManager().getIndexes();
        final List<OIndexAbstract<?>> indexesToLock = prepareIndexesToFreeze(indexes);
        freezeIndexes(indexesToLock, true);
        flushIndexes(indexesToLock);
        final OFreezableStorage storage = getFreezableStorage();
        if (storage != null) {
            storage.freeze(throwException);
        }
        Orient.instance().getProfiler().stopChrono("db." + getName() + ".freeze", "Time to freeze the database", startTime, "db.*.freeze");
    }

    @Override
    public void freeze() {
        checkOpeness();
        if (!(getStorage() instanceof OFreezableStorage)) {
            OLogManager.instance().error(this, "We can not freeze non local storage. " + "If you use remote client please use OServerAdmin instead.");
            return;
        }
        final long startTime = Orient.instance().getProfiler().startChrono();
        final Collection<? extends OIndex<?>> indexes = getMetadata().getIndexManager().getIndexes();
        final List<OIndexAbstract<?>> indexesToLock = prepareIndexesToFreeze(indexes);
        freezeIndexes(indexesToLock, false);
        flushIndexes(indexesToLock);
        final OFreezableStorage storage = getFreezableStorage();
        if (storage != null) {
            storage.freeze(false);
        }
        Orient.instance().getProfiler().stopChrono("db." + getName() + ".freeze", "Time to freeze the database", startTime, "db.*.freeze");
    }

    @Override
    public void release() {
        checkOpeness();
        if (!(getStorage() instanceof OFreezableStorage)) {
            OLogManager.instance().error(this, "We can not release non local storage. " + "If you use remote client please use OServerAdmin instead.");
            return;
        }
        final long startTime = Orient.instance().getProfiler().startChrono();
        final OFreezableStorage storage = getFreezableStorage();
        if (storage != null) {
            storage.release();
        }
        Collection<? extends OIndex<?>> indexes = getMetadata().getIndexManager().getIndexes();
        releaseIndexes(indexes);
        Orient.instance().getProfiler().stopChrono("db." + getName() + ".release", "Time to release the database", startTime, "db.*.release");
    }

    public ODocument newInstance() {
        return new ODocument();
    }

    @Override
    public ODocument newInstance(final String iClassName) {
        return new ODocument(iClassName);
    }

    public ORecordIteratorClass<ODocument> browseClass(final String iClassName) {
        return browseClass(iClassName, true);
    }

    public ORecordIteratorClass<ODocument> browseClass(final String iClassName, final boolean iPolymorphic) {
        if (getMetadata().getImmutableSchemaSnapshot().getClass(iClassName) == null)
            throw new IllegalArgumentException("Class '" + iClassName + "' not found in current database");
        checkSecurity(ORule.ResourceGeneric.CLASS, ORole.PERMISSION_READ, iClassName);
        return new ORecordIteratorClass<ODocument>(this, this, iClassName, iPolymorphic, true, false);
    }

    @Override
    public ORecordIteratorCluster<ODocument> browseCluster(final String iClusterName) {
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, iClusterName);
        return new ORecordIteratorCluster<ODocument>(this, this, getClusterIdByName(iClusterName), true);
    }

    @Override
    public ORecordIteratorCluster<ODocument> browseCluster(String iClusterName, long startClusterPosition, long endClusterPosition, boolean loadTombstones) {
        checkSecurity(ORule.ResourceGeneric.CLUSTER, ORole.PERMISSION_READ, iClusterName);
        return new ORecordIteratorCluster<ODocument>(this, this, getClusterIdByName(iClusterName), startClusterPosition, endClusterPosition, true, loadTombstones, OStorage.LOCKING_STRATEGY.DEFAULT);
    }

    @Override
    public <RET extends ORecord> RET save(final ORecord iRecord) {
        return (RET) save(iRecord, null, OPERATION_MODE.SYNCHRONOUS, false, null, null);
    }

    @Override
    public <RET extends ORecord> RET save(final ORecord iRecord, final OPERATION_MODE iMode, boolean iForceCreate, final ORecordCallback<? extends Number> iRecordCreatedCallback, ORecordCallback<ORecordVersion> iRecordUpdatedCallback) {
        return save(iRecord, null, iMode, iForceCreate, iRecordCreatedCallback, iRecordUpdatedCallback);
    }

    @Override
    public <RET extends ORecord> RET save(final ORecord iRecord, final String iClusterName) {
        return (RET) save(iRecord, iClusterName, OPERATION_MODE.SYNCHRONOUS, false, null, null);
    }

    @Override
    public <RET extends ORecord> RET save(final ORecord iRecord, String iClusterName, final OPERATION_MODE iMode, boolean iForceCreate, final ORecordCallback<? extends Number> iRecordCreatedCallback, ORecordCallback<ORecordVersion> iRecordUpdatedCallback) {
        checkOpeness();
        if (!(iRecord instanceof ODocument))
            return (RET) currentTx.saveRecord(iRecord, iClusterName, iMode, iForceCreate, iRecordCreatedCallback, iRecordUpdatedCallback);
        ODocument doc = (ODocument) iRecord;
        doc.validate();
        ODocumentInternal.convertAllMultiValuesToTrackedVersions(doc);
        if (iForceCreate || !doc.getIdentity().isValid()) {
            if (doc.getClassName() != null)
                checkSecurity(ORule.ResourceGeneric.CLASS, ORole.PERMISSION_CREATE, doc.getClassName());
            final OClass schemaClass = doc.getImmutableSchemaClass();
            int clusterId = iRecord.getIdentity().getClusterId();
            if (clusterId == ORID.CLUSTER_ID_INVALID) {
                if (iClusterName == null) {
                    if (schemaClass != null) {
                        if (schemaClass.isAbstract())
                            throw new OSchemaException("Document belongs to abstract class " + schemaClass.getName() + " and can not be saved");
                        iClusterName = getClusterNameById(schemaClass.getClusterForNewInstance(doc));
                    } else {
                        iClusterName = getClusterNameById(storage.getDefaultClusterId());
                    }
                }
                clusterId = getClusterIdByName(iClusterName);
                if (clusterId == -1)
                    throw new IllegalArgumentException("Cluster name " + iClusterName + " is not configured");
            }
            final int[] clusterIds;
            if (schemaClass != null) {
                clusterIds = schemaClass.getClusterIds();
                int i = 0;
                for (; i < clusterIds.length; ++i) if (clusterIds[i] == clusterId)
                    break;
                if (i == clusterIds.length)
                    throw new IllegalArgumentException("Cluster name " + iClusterName + " (id=" + clusterId + ") is not configured to store the class " + doc.getClassName() + ", valid are " + Arrays.toString(clusterIds));
            }
        } else {
            if (doc.getClassName() != null)
                checkSecurity(ORule.ResourceGeneric.CLASS, ORole.PERMISSION_UPDATE, doc.getClassName());
        }
        doc = (ODocument) currentTx.saveRecord(iRecord, iClusterName, iMode, iForceCreate, iRecordCreatedCallback, iRecordUpdatedCallback);
        return (RET) doc;
    }

    public ODatabaseDocumentTx delete(final ORecord record) {
        checkOpeness();
        if (record == null)
            throw new ODatabaseException("Cannot delete null document");
        if (record instanceof ODocument && ((ODocument) record).getClassName() != null)
            checkSecurity(ORule.ResourceGeneric.CLASS, ORole.PERMISSION_DELETE, ((ODocument) record).getClassName());
        try {
            currentTx.deleteRecord(record, OPERATION_MODE.SYNCHRONOUS);
        } catch (OException e) {
            throw e;
        } catch (Exception e) {
            if (record instanceof ODocument)
                throw new ODatabaseException("Error on deleting record " + record.getIdentity() + " of class '" + ((ODocument) record).getClassName() + "'", e);
            else
                throw new ODatabaseException("Error on deleting record " + record.getIdentity());
        }
        return this;
    }

    public long countClass(final String iClassName) {
        return countClass(iClassName, true);
    }

    public long countClass(final String iClassName, final boolean iPolymorphic) {
        ODatabaseRecordThreadLocal.INSTANCE.set(this);
        final OClass cls = getMetadata().getImmutableSchemaSnapshot().getClass(iClassName);
        if (cls == null)
            throw new IllegalArgumentException("Class '" + iClassName + "' not found in database");
        return cls.count(iPolymorphic);
    }

    @Override
    public ODatabase<ORecord> commit() {
        return commit(false);
    }

    @Override
    public ODatabaseDocument commit(boolean force) throws OTransactionException {
        checkOpeness();
        if (!currentTx.isActive())
            return this;
        if (!force && currentTx.amountOfNestedTxs() > 1) {
            currentTx.commit();
            return this;
        }
        setCurrentDatabaseInThreadLocal();
        for (ODatabaseListener listener : browseListeners()) try {
            listener.onBeforeTxCommit(this);
        } catch (Throwable t) {
            try {
                rollback(force);
            } catch (RuntimeException e) {
                throw e;
            }
            OLogManager.instance().debug(this, "Cannot commit the transaction: caught exception on execution of %s.onBeforeTxCommit()", t, OTransactionBlockedException.class, listener.getClass());
        }
        try {
            currentTx.commit(force);
        } catch (RuntimeException e) {
            for (ODatabaseListener listener : browseListeners()) try {
                listener.onBeforeTxRollback(this);
            } catch (Throwable t) {
                OLogManager.instance().error(this, "Error before tx rollback", t);
            }
            currentTx.rollback(false, 0);
            getLocalCache().clear();
            for (ODatabaseListener listener : browseListeners()) try {
                listener.onAfterTxRollback(this);
            } catch (Throwable t) {
                OLogManager.instance().error(this, "Error after tx rollback", t);
            }
            throw e;
        }
        for (ODatabaseListener listener : browseListeners()) try {
            listener.onAfterTxCommit(this);
        } catch (Throwable t) {
            OLogManager.instance().debug(this, "Error after the transaction has been committed. The transaction remains valid. The exception caught was on execution of %s.onAfterTxCommit()", t, OTransactionBlockedException.class, listener.getClass());
        }
        return this;
    }

    @Override
    public ODatabase<ORecord> rollback() {
        return rollback(false);
    }

    @Override
    public ODatabaseDocument rollback(boolean force) throws OTransactionException {
        checkOpeness();
        if (currentTx.isActive()) {
            if (!force && currentTx.amountOfNestedTxs() > 1) {
                currentTx.rollback();
                return this;
            }
            for (ODatabaseListener listener : browseListeners()) try {
                listener.onBeforeTxRollback(this);
            } catch (Throwable t) {
                OLogManager.instance().error(this, "Error before tx rollback", t);
            }
            currentTx.rollback(force, -1);
            for (ODatabaseListener listener : browseListeners()) try {
                listener.onAfterTxRollback(this);
            } catch (Throwable t) {
                OLogManager.instance().error(this, "Error after tx rollback", t);
            }
        }
        getLocalCache().clear();
        return this;
    }

    @Override
    public <DB extends ODatabase> DB getUnderlying() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OStorage getStorage() {
        return storage;
    }

    @Override
    public void replaceStorage(OStorage iNewStorage) {
        storage = iNewStorage;
    }

    @Override
    public <V> V callInLock(Callable<V> iCallable, boolean iExclusiveLock) {
        return storage.callInLock(iCallable, iExclusiveLock);
    }

    @Override
    public void backup(OutputStream out, Map<String, Object> options, Callable<Object> callable, OCommandOutputListener iListener, int compressionLevel, int bufferSize) throws IOException {
        storage.backup(out, options, callable, iListener, compressionLevel, bufferSize);
    }

    @Override
    public void restore(InputStream in, Map<String, Object> options, Callable<Object> callable, OCommandOutputListener iListener) throws IOException {
        if (storage == null)
            storage = Orient.instance().loadStorage(url);
        getStorage().restore(in, options, callable, iListener);
    }

    public OSBTreeCollectionManager getSbTreeCollectionManager() {
        return sbTreeCollectionManager;
    }

    @Override
    public OCurrentStorageComponentsFactory getStorageVersions() {
        return componentsFactory;
    }

    public ORecordSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(ORecordSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void resetInitialization() {
        for (ORecordHook h : hooks.keySet()) h.onUnregister();
        hooks.clear();
        localCache.shutdown();
        close();
        initialized = false;
    }

    @Override
    @Deprecated
    public <DB extends ODatabaseDocument> DB checkSecurity(String iResource, int iOperation) {
        final String resourceSpecific = ORule.mapLegacyResourceToSpecificResource(iResource);
        final ORule.ResourceGeneric resourceGeneric = ORule.mapLegacyResourceToGenericResource(iResource);
        if (resourceSpecific == null || resourceSpecific.equals("*"))
            checkSecurity(resourceGeneric, null, iOperation);
        return checkSecurity(resourceGeneric, resourceSpecific, iOperation);
    }

    @Override
    @Deprecated
    public <DB extends ODatabaseDocument> DB checkSecurity(String iResourceGeneric, int iOperation, Object iResourceSpecific) {
        final ORule.ResourceGeneric resourceGeneric = ORule.mapLegacyResourceToGenericResource(iResourceGeneric);
        if (iResourceSpecific == null || iResourceSpecific.equals("*"))
            return checkSecurity(resourceGeneric, iOperation, (Object) null);
        return checkSecurity(resourceGeneric, iOperation, iResourceSpecific);
    }

    @Override
    @Deprecated
    public <DB extends ODatabaseDocument> DB checkSecurity(String iResourceGeneric, int iOperation, Object... iResourcesSpecific) {
        final ORule.ResourceGeneric resourceGeneric = ORule.mapLegacyResourceToGenericResource(iResourceGeneric);
        return checkSecurity(resourceGeneric, iOperation, iResourcesSpecific);
    }

    public void setCurrentDatabaseInThreadLocal() {
        ODatabaseRecordThreadLocal.INSTANCE.set(this);
    }

    protected void checkTransaction() {
        if (currentTx == null || currentTx.getStatus() == OTransaction.TXSTATUS.INVALID)
            throw new OTransactionException("Transaction not started");
    }

    @Deprecated
    protected ORecordSerializer resolveFormat(final Object iObject) {
        return ORecordSerializerFactory.instance().getFormatForObject(iObject, recordFormat);
    }

    protected void checkOpeness() {
        if (isClosed())
            throw new ODatabaseException("Database '" + getURL() + "' is closed");
    }

    private void initAtFirstOpen(String iUserName, String iUserPassword) {
        if (initialized)
            return;
        ORecordSerializerFactory serializerFactory = ORecordSerializerFactory.instance();
        String serializeName = getStorage().getConfiguration().getRecordSerializer();
        if (serializeName == null)
            serializeName = ORecordSerializerSchemaAware2CSV.NAME;
        serializer = serializerFactory.getFormat(serializeName);
        if (serializer == null)
            throw new ODatabaseException("RecordSerializer with name '" + serializeName + "' not found ");
        if (getStorage().getConfiguration().getRecordSerializerVersion() > serializer.getMinSupportedVersion())
            throw new ODatabaseException("Persistent record serializer version is not support by the current implementation");
        componentsFactory = getStorage().getComponentsFactory();
        final OSBTreeCollectionManager sbTreeCM = getStorage().getResource(OSBTreeCollectionManager.class.getSimpleName(), new Callable<OSBTreeCollectionManager>() {

            @Override
            public OSBTreeCollectionManager call() throws Exception {
                Class<? extends OSBTreeCollectionManager> managerClass = getStorage().getCollectionManagerClass();
                if (managerClass == null) {
                    OLogManager.instance().warn(this, "Current implementation of storage does not support sbtree collections");
                    return null;
                } else {
                    return managerClass.newInstance();
                }
            }
        });
        sbTreeCollectionManager = sbTreeCM != null ? new OSBTreeCollectionManagerProxy(this, sbTreeCM) : null;
        localCache.startup();
        metadata = new OMetadataDefault();
        metadata.load();
        recordFormat = DEF_RECORD_FORMAT;
        if (!(getStorage() instanceof OStorageProxy)) {
            if (metadata.getIndexManager().autoRecreateIndexesAfterCrash()) {
                metadata.getIndexManager().recreateIndexes();
                setCurrentDatabaseInThreadLocal();
                user = null;
            }
            installHooks();
            registerHook(new OSecurityTrackerHook(metadata.getSecurity()), ORecordHook.HOOK_POSITION.LAST);
            user = null;
        } else
            user = new OImmutableUser(-1, new OUser(iUserName, OUser.encryptPassword(iUserPassword)).addRole(new ORole("passthrough", null, ORole.ALLOW_MODES.ALLOW_ALL_BUT)));
        if (!metadata.getSchema().existsClass(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME))
            metadata.getSchema().createClass(OMVRBTreeRIDProvider.PERSISTENT_CLASS_NAME);
        initialized = true;
    }

    private void installHooks() {
        registerHook(new OClassTrigger(), ORecordHook.HOOK_POSITION.FIRST);
        registerHook(new ORestrictedAccessHook(), ORecordHook.HOOK_POSITION.FIRST);
        registerHook(new OUserTrigger(), ORecordHook.HOOK_POSITION.EARLY);
        registerHook(new OFunctionTrigger(), ORecordHook.HOOK_POSITION.REGULAR);
        registerHook(new OClassIndexManager(), ORecordHook.HOOK_POSITION.LAST);
        registerHook(new OSchedulerTrigger(), ORecordHook.HOOK_POSITION.LAST);
        registerHook(new ORidBagDeleteHook(), ORecordHook.HOOK_POSITION.LAST);
    }

    private void closeOnDelete() {
        if (status != STATUS.OPEN)
            return;
        if (currentIntent != null) {
            currentIntent.end(this);
            currentIntent = null;
        }
        resetListeners();
        if (storage != null)
            storage.close(true, true);
        storage = null;
        status = STATUS.CLOSED;
    }

    private void clearCustomInternal() {
        storage.getConfiguration().properties.clear();
    }

    private void removeCustomInternal(final String iName) {
        setCustomInternal(iName, null);
    }

    private void setCustomInternal(final String iName, final String iValue) {
        if (iValue == null || "null".equalsIgnoreCase(iValue)) {
            for (Iterator<OStorageEntryConfiguration> it = storage.getConfiguration().properties.iterator(); it.hasNext(); ) {
                final OStorageEntryConfiguration e = it.next();
                if (e.name.equals(iName)) {
                    it.remove();
                    break;
                }
            }
        } else {
            boolean found = false;
            for (OStorageEntryConfiguration e : storage.getConfiguration().properties) {
                if (e.name.equals(iName)) {
                    e.value = iValue;
                    found = true;
                    break;
                }
            }
            if (!found)
                storage.getConfiguration().properties.add(new OStorageEntryConfiguration(iName, iValue));
        }
        storage.getConfiguration().update();
    }

    private void callbackHookFailure(ORecord record, boolean iCallTriggers, boolean wasNew, byte[] stream) {
        if (iCallTriggers && stream != null && stream.length > 0)
            callbackHooks(wasNew ? ORecordHook.TYPE.CREATE_FAILED : ORecordHook.TYPE.UPDATE_FAILED, record);
    }

    private void callbackHookSuccess(final ORecord record, final boolean iCallTriggers, final boolean wasNew, final byte[] stream, final OStorageOperationResult<ORecordVersion> operationResult) {
        if (iCallTriggers && stream != null && stream.length > 0) {
            final ORecordHook.TYPE hookType;
            if (!operationResult.isMoved()) {
                hookType = wasNew ? ORecordHook.TYPE.AFTER_CREATE : ORecordHook.TYPE.AFTER_UPDATE;
            } else {
                hookType = wasNew ? ORecordHook.TYPE.CREATE_REPLICATED : ORecordHook.TYPE.UPDATE_REPLICATED;
            }
            callbackHooks(hookType, record);
            clearDocumentTracking(record);
        }
    }

    private void clearDocumentTracking(final ORecord record) {
        if (record instanceof ODocument && ((ODocument) record).isTrackingChanges()) {
            ((ODocument) record).setTrackingChanges(false);
            ((ODocument) record).setTrackingChanges(true);
        }
    }

    private void checkRecordClass(ORecord record, String iClusterName, ORecordId rid, boolean isNew) {
        if (rid.clusterId > -1 && getStorageVersions().classesAreDetectedByClusterId() && isNew && record instanceof ODocument) {
            final ODocument recordSchemaAware = (ODocument) record;
            final OClass recordClass = recordSchemaAware.getImmutableSchemaClass();
            final OClass clusterIdClass = metadata.getImmutableSchemaSnapshot().getClassByClusterId(rid.clusterId);
            if (recordClass == null && clusterIdClass != null || clusterIdClass == null && recordClass != null || (recordClass != null && !recordClass.equals(clusterIdClass)))
                throw new OSchemaException("Record saved into cluster '" + iClusterName + "' should be saved with class '" + clusterIdClass + "' but has been created with class '" + recordClass + "'");
        }
    }

    private byte[] updateStream(ORecord record) {
        byte[] stream;
        ORecordInternal.unsetDirty(record);
        record.setDirty();
        ORecordSerializationContext.pullContext();
        ORecordSerializationContext.pushContext();
        stream = record.toStream();
        return stream;
    }

    private void releaseIndexModificationLock(Set<OIndex<?>> lockedIndexes) {
        if (metadata == null)
            return;
        final OIndexManager indexManager = metadata.getIndexManager();
        if (indexManager == null)
            return;
        for (OIndex<?> index : lockedIndexes) {
            index.getInternal().releaseModificationLock();
        }
    }

    private void acquireIndexModificationLock(ODocument doc, Set<OIndex<?>> lockedIndexes) {
        if (getStorage().getUnderlying() instanceof OStorageEmbedded) {
            final OClass cls = doc.getImmutableSchemaClass();
            if (cls != null) {
                final Collection<OIndex<?>> indexes = cls.getIndexes();
                if (indexes != null) {
                    final SortedSet<OIndex<?>> indexesToLock = new TreeSet<OIndex<?>>(new Comparator<OIndex<?>>() {

                        public int compare(OIndex<?> indexOne, OIndex<?> indexTwo) {
                            return indexOne.getName().compareTo(indexTwo.getName());
                        }
                    });
                    indexesToLock.addAll(indexes);
                    for (final OIndex<?> index : indexesToLock) {
                        index.getInternal().acquireModificationLock();
                        lockedIndexes.add(index);
                    }
                }
            }
        }
    }

    private void init() {
        currentTx = new OTransactionNoTx(this);
    }

    private OFreezableStorage getFreezableStorage() {
        OStorage s = getStorage();
        if (s instanceof OFreezableStorage)
            return (OFreezableStorage) s;
        else {
            OLogManager.instance().error(this, "Storage of type " + s.getType() + " does not support freeze operation.");
            return null;
        }
    }

    private void freezeIndexes(final List<OIndexAbstract<?>> indexesToFreeze, final boolean throwException) {
        if (indexesToFreeze != null) {
            for (OIndexAbstract<?> indexToLock : indexesToFreeze) {
                indexToLock.freeze(throwException);
            }
        }
    }

    private void flushIndexes(final List<OIndexAbstract<?>> indexesToFlush) {
        for (OIndexAbstract<?> index : indexesToFlush) {
            index.flush();
        }
    }

    private List<OIndexAbstract<?>> prepareIndexesToFreeze(final Collection<? extends OIndex<?>> indexes) {
        List<OIndexAbstract<?>> indexesToFreeze = null;
        if (indexes != null && !indexes.isEmpty()) {
            indexesToFreeze = new ArrayList<OIndexAbstract<?>>(indexes.size());
            for (OIndex<?> index : indexes) {
                indexesToFreeze.add((OIndexAbstract<?>) index.getInternal());
            }
            Collections.sort(indexesToFreeze, new Comparator<OIndex<?>>() {

                public int compare(OIndex<?> o1, OIndex<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        return indexesToFreeze;
    }

    private void releaseIndexes(final Collection<? extends OIndex<?>> indexesToRelease) {
        if (indexesToRelease != null) {
            Iterator<? extends OIndex<?>> it = indexesToRelease.iterator();
            while (it.hasNext()) {
                it.next().getInternal().release();
                it.remove();
            }
        }
    }
}
