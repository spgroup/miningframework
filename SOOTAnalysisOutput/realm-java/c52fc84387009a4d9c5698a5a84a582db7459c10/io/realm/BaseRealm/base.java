package io.realm;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.getkeepsafe.relinker.BuildConfig;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.HandlerControllerConstants;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedGroupManager;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.log.RealmLog;
import rx.Observable;

@SuppressWarnings("WeakerAccess")
abstract class BaseRealm implements Closeable {

    protected static final long UNVERSIONED = -1;

    private static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";

    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";

    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";

    private static final String NOT_IN_TRANSACTION_MESSAGE = "Changing Realm data can only be done from inside a transaction.";

    protected static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    static final RealmThreadPoolExecutor asyncTaskExecutor = RealmThreadPoolExecutor.newDefaultExecutor();

    final long threadId;

    protected RealmConfiguration configuration;

    protected SharedGroupManager sharedGroupManager;

    RealmSchema schema;

    Handler handler;

    HandlerController handlerController;

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    protected BaseRealm(RealmConfiguration configuration) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.sharedGroupManager = new SharedGroupManager(configuration);
        this.schema = new RealmSchema(this, sharedGroupManager.getTransaction());
        this.handlerController = new HandlerController(this);
        if (handlerController.isAutoRefreshAvailable()) {
            setAutoRefresh(true);
        }
    }

    public void setAutoRefresh(boolean autoRefresh) {
        checkIfValid();
        handlerController.checkCanBeAutoRefreshed();
        if (autoRefresh && !handlerController.isAutoRefreshEnabled()) {
            handler = new Handler(handlerController);
            handlers.put(handler, configuration.getPath());
        } else if (!autoRefresh && handlerController.isAutoRefreshEnabled() && handler != null) {
            removeHandler();
        }
        handlerController.setAutoRefresh(autoRefresh);
    }

    public boolean isAutoRefresh() {
        return handlerController.isAutoRefreshEnabled();
    }

    public boolean isInTransaction() {
        checkIfValid();
        return !sharedGroupManager.isImmutable();
    }

    protected void addListener(RealmChangeListener<? extends BaseRealm> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't register a listener from a non-Looper or IntentService thread.");
        }
        handlerController.addChangeListener(listener);
    }

    public void removeChangeListener(RealmChangeListener<? extends BaseRealm> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't remove a listener from a non-Looper thread ");
        }
        handlerController.removeChangeListener(listener);
    }

    public abstract Observable asObservable();

    public void removeAllChangeListeners() {
        checkIfValid();
        if (!handlerController.isAutoRefreshEnabled()) {
            throw new IllegalStateException("You can't remove listeners from a non-Looper thread ");
        }
        handlerController.removeAllChangeListeners();
    }

    void setHandler(Handler handler) {
        handlers.remove(this.handler);
        handlers.put(handler, configuration.getPath());
        this.handler = handler;
    }

    protected void removeHandler() {
        handlers.remove(handler);
        handler.removeCallbacksAndMessages(null);
        this.handler = null;
    }

    public void writeCopyTo(File destination) throws java.io.IOException {
        writeEncryptedCopyTo(destination, null);
    }

    public void writeEncryptedCopyTo(File destination, byte[] key) throws java.io.IOException {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedGroupManager.copyToFile(destination, key);
    }

    public boolean waitForChange() {
        checkIfValid();
        if (isInTransaction()) {
            throw new IllegalStateException("Cannot wait for changes inside of a transaction.");
        }
        if (Looper.myLooper() != null) {
            throw new IllegalStateException("Cannot wait for changes inside a Looper thread. Use RealmChangeListeners instead.");
        }
        boolean hasChanged = sharedGroupManager.getSharedGroup().waitForChange();
        if (hasChanged) {
            sharedGroupManager.advanceRead();
            handlerController.refreshSynchronousTableViews();
        }
        return hasChanged;
    }

    public void stopWaitForChange() {
        RealmCache.invokeWithLock(new RealmCache.Callback0() {

            @Override
            public void onCall() {
                if (sharedGroupManager == null || !sharedGroupManager.isOpen() || sharedGroupManager.getSharedGroup().isClosed()) {
                    throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
                }
                sharedGroupManager.getSharedGroup().stopWaitForChange();
            }
        });
    }

    public void beginTransaction() {
        checkIfValid();
        sharedGroupManager.promoteToWrite();
    }

    public void commitTransaction() {
        commitTransaction(true, true);
    }

    void commitAsyncTransaction() {
        commitTransaction(false, false);
    }

    void commitTransaction(boolean notifyLocalThread, boolean notifyOtherThreads) {
        checkIfValid();
        sharedGroupManager.commitAndContinueAsRead();
        for (Map.Entry<Handler, String> handlerIntegerEntry : handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();
            if (!notifyLocalThread && handler.equals(this.handler)) {
                continue;
            }
            if (!notifyOtherThreads && !handler.equals(this.handler)) {
                continue;
            }
            Looper looper = handler.getLooper();
            if (realmPath.equals(configuration.getPath()) && looper.getThread().isAlive()) {
                boolean messageHandled = true;
                if (looper == Looper.myLooper()) {
                    Message msg = Message.obtain();
                    msg.what = HandlerControllerConstants.LOCAL_COMMIT;
                    if (!handler.hasMessages(HandlerControllerConstants.LOCAL_COMMIT)) {
                        handler.removeMessages(HandlerControllerConstants.REALM_CHANGED);
                        messageHandled = handler.sendMessageAtFrontOfQueue(msg);
                    }
                } else {
                    if (!handler.hasMessages(HandlerControllerConstants.REALM_CHANGED)) {
                        messageHandled = handler.sendEmptyMessage(HandlerControllerConstants.REALM_CHANGED);
                    }
                }
                if (!messageHandled) {
                    RealmLog.w("Cannot update Looper threads when the Looper has quit. Use realm.setAutoRefresh(false) " + "to prevent this.");
                }
            }
        }
    }

    public void cancelTransaction() {
        checkIfValid();
        sharedGroupManager.rollbackAndContinueAsRead();
    }

    protected void checkIfValid() {
        if (sharedGroupManager == null || !sharedGroupManager.isOpen()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(BaseRealm.INCORRECT_THREAD_MESSAGE);
        }
    }

    protected void checkIfValidAndInTransaction() {
        if (!isInTransaction()) {
            throw new IllegalStateException(NOT_IN_TRANSACTION_MESSAGE);
        }
    }

    public String getPath() {
        return configuration.getPath();
    }

    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    public long getVersion() {
        if (!sharedGroupManager.hasTable(Table.METADATA_TABLE_NAME)) {
            return UNVERSIONED;
        }
        Table metadataTable = sharedGroupManager.getTable(Table.METADATA_TABLE_NAME);
        return metadataTable.getLong(0, 0);
    }

    @Override
    public void close() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }
        RealmCache.release(this);
    }

    void doClose() {
        if (sharedGroupManager != null) {
            sharedGroupManager.close();
            sharedGroupManager = null;
        }
        if (handler != null) {
            removeHandler();
        }
    }

    public boolean isClosed() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }
        return sharedGroupManager == null || !sharedGroupManager.isOpen();
    }

    public boolean isEmpty() {
        checkIfValid();
        return sharedGroupManager.getTransaction().isObjectTablesEmpty();
    }

    boolean hasChanged() {
        return sharedGroupManager.hasChanged();
    }

    void setVersion(long version) {
        Table metadataTable = sharedGroupManager.getTable(Table.METADATA_TABLE_NAME);
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(RealmFieldType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
    }

    static Map<Handler, String> getHandlers() {
        return handlers;
    }

    public RealmSchema getSchema() {
        return schema;
    }

    <E extends RealmModel> E get(Class<E> clazz, long rowIndex) {
        Table table = schema.getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
        E result = configuration.getSchemaMediator().newInstance(clazz, schema.getColumnInfo(clazz));
        RealmObjectProxy proxy = (RealmObjectProxy) result;
        proxy.realmGet$proxyState().setRow$realm(row);
        proxy.realmGet$proxyState().setRealm$realm(this);
        proxy.realmGet$proxyState().setTableVersion$realm();
        return result;
    }

    <E extends RealmModel> E get(Class<E> clazz, String dynamicClassName, long rowIndex) {
        Table table;
        E result;
        if (dynamicClassName != null) {
            table = schema.getTable(dynamicClassName);
            @SuppressWarnings("unchecked")
            E dynamicObj = (E) new DynamicRealmObject();
            result = dynamicObj;
        } else {
            table = schema.getTable(clazz);
            result = configuration.getSchemaMediator().newInstance(clazz, schema.getColumnInfo(clazz));
        }
        RealmObjectProxy proxy = (RealmObjectProxy) result;
        proxy.realmGet$proxyState().setRealm$realm(this);
        if (rowIndex != Table.NO_MATCH) {
            proxy.realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
            proxy.realmGet$proxyState().setTableVersion$realm();
        } else {
            proxy.realmGet$proxyState().setRow$realm(InvalidRow.INSTANCE);
        }
        return result;
    }

    public void deleteAll() {
        checkIfValid();
        for (RealmObjectSchema objectSchema : schema.getAll()) {
            schema.getTable(objectSchema.getClassName()).clear();
        }
    }

    static private boolean deletes(String canonicalPath, File rootFolder, String realmFileName) {
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);
        List<File> filesToDelete = Arrays.asList(new File(rootFolder, realmFileName), new File(rootFolder, realmFileName + ".lock"), new File(rootFolder, realmFileName + ".log_a"), new File(rootFolder, realmFileName + ".log_b"), new File(rootFolder, realmFileName + ".log"), new File(canonicalPath));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    realmDeleted.set(false);
                    RealmLog.w("Could not delete the file " + fileToDelete);
                }
            }
        }
        return realmDeleted.get();
    }

    static boolean deleteRealm(final RealmConfiguration configuration) {
        final String management = ".management";
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);
        RealmCache.invokeWithGlobalRefCount(configuration, new RealmCache.Callback() {

            @Override
            public void onResult(int count) {
                if (count != 0) {
                    throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " + "Remember to close() all the instances of the Realm before deleting its file: " + configuration.getPath());
                }
                String canonicalPath = configuration.getPath();
                File realmFolder = configuration.getRealmFolder();
                String realmFileName = configuration.getRealmFileName();
                File managementFolder = new File(realmFolder, realmFileName + management);
                File[] files = managementFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        realmDeleted.set(realmDeleted.get() && file.delete());
                    }
                }
                realmDeleted.set(realmDeleted.get() && managementFolder.delete());
                realmDeleted.set(realmDeleted.get() && deletes(canonicalPath, realmFolder, realmFileName));
            }
        });
        return realmDeleted.get();
    }

    static boolean compactRealm(final RealmConfiguration configuration) {
        if (configuration.getEncryptionKey() != null) {
            throw new IllegalArgumentException("Cannot currently compact an encrypted Realm.");
        }
        return SharedGroupManager.compact(configuration);
    }

    protected static void migrateRealm(final RealmConfiguration configuration, final RealmMigration migration, final MigrationCallback callback) throws FileNotFoundException {
        if (configuration == null) {
            throw new IllegalArgumentException("RealmConfiguration must be provided");
        }
        if (migration == null && configuration.getMigration() == null) {
            throw new RealmMigrationNeededException(configuration.getPath(), "RealmMigration must be provided");
        }
        final AtomicBoolean fileNotFound = new AtomicBoolean(false);
        RealmCache.invokeWithGlobalRefCount(configuration, new RealmCache.Callback() {

            @Override
            public void onResult(int count) {
                if (count != 0) {
                    throw new IllegalStateException("Cannot migrate a Realm file that is already open: " + configuration.getPath());
                }
                File realmFile = new File(configuration.getPath());
                if (!realmFile.exists()) {
                    fileNotFound.set(true);
                    return;
                }
                RealmMigration realmMigration = (migration == null) ? configuration.getMigration() : migration;
                DynamicRealm realm = null;
                try {
                    realm = DynamicRealm.getInstance(configuration);
                    realm.beginTransaction();
                    long currentVersion = realm.getVersion();
                    realmMigration.migrate(realm, currentVersion, configuration.getSchemaVersion());
                    realm.setVersion(configuration.getSchemaVersion());
                    realm.commitTransaction();
                } catch (RuntimeException e) {
                    if (realm != null) {
                        realm.cancelTransaction();
                    }
                    throw e;
                } finally {
                    if (realm != null) {
                        realm.close();
                        callback.migrationComplete();
                    }
                }
            }
        });
        if (fileNotFound.get()) {
            throw new FileNotFoundException("Cannot migrate a Realm file which doesn't exist: " + configuration.getPath());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedGroupManager != null && sharedGroupManager.isOpen()) {
            RealmLog.w("Remember to call close() on all Realm instances. " + "Realm " + configuration.getPath() + " is being finalized without being closed, " + "this can lead to running out of native memory.");
        }
        super.finalize();
    }

    protected interface MigrationCallback {

        void migrationComplete();
    }
}
