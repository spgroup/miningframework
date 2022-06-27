package io.realm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedRealm;
import io.realm.internal.ColumnInfo;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.AndroidLogger;
import io.realm.log.RealmLog;
import rx.Observable;

@SuppressWarnings("WeakerAccess")
abstract class BaseRealm implements Closeable {

    protected static final long UNVERSIONED = -1;

    private static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";

    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";

    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";

    private static final String NOT_IN_TRANSACTION_MESSAGE = "Changing Realm data can only be done from inside a transaction.";

    static final RealmThreadPoolExecutor asyncTaskExecutor = RealmThreadPoolExecutor.newDefaultExecutor();

    final long threadId;

    protected RealmConfiguration configuration;

    protected SharedRealm sharedRealm;

    RealmSchema schema;

    HandlerController handlerController;

    static {
        RealmLog.add(BuildConfig.DEBUG ? new AndroidLogger(Log.DEBUG) : new AndroidLogger(Log.WARN));
    }

    protected BaseRealm(RealmConfiguration configuration) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.handlerController = new HandlerController(this);
        this.sharedRealm = SharedRealm.getInstance(configuration, new AndroidNotifier(this.handlerController), !(this instanceof Realm) ? null : new SharedRealm.SchemaVersionListener() {

            @Override
            public void onSchemaVersionChanged(long currentVersion) {
                RealmCache.updateSchemaCache((Realm) BaseRealm.this);
            }
        });
        this.schema = new RealmSchema(this);
        if (handlerController.isAutoRefreshAvailable()) {
            setAutoRefresh(true);
        }
    }

    public void setAutoRefresh(boolean autoRefresh) {
        checkIfValid();
        handlerController.checkCanBeAutoRefreshed();
        handlerController.setAutoRefresh(autoRefresh);
    }

    public boolean isAutoRefresh() {
        return handlerController.isAutoRefreshEnabled();
    }

    public boolean isInTransaction() {
        checkIfValid();
        return sharedRealm.isInTransaction();
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
        ((AndroidNotifier) sharedRealm.realmNotifier).setHandler(handler);
    }

    public void writeCopyTo(File destination) {
        writeEncryptedCopyTo(destination, null);
    }

    public void writeEncryptedCopyTo(File destination, byte[] key) {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        sharedRealm.writeCopy(destination, key);
    }

    public boolean waitForChange() {
        checkIfValid();
        if (isInTransaction()) {
            throw new IllegalStateException("Cannot wait for changes inside of a transaction.");
        }
        if (Looper.myLooper() != null) {
            throw new IllegalStateException("Cannot wait for changes inside a Looper thread. Use RealmChangeListeners instead.");
        }
        boolean hasChanged = sharedRealm.waitForChange();
        if (hasChanged) {
            sharedRealm.refresh();
            handlerController.refreshSynchronousTableViews();
        }
        return hasChanged;
    }

    public void stopWaitForChange() {
        RealmCache.invokeWithLock(new RealmCache.Callback0() {

            @Override
            public void onCall() {
                if (sharedRealm == null || sharedRealm.isClosed()) {
                    throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
                }
                sharedRealm.stopWaitForChange();
            }
        });
    }

    public void beginTransaction() {
        checkIfValid();
        sharedRealm.beginTransaction();
    }

    public void commitTransaction() {
        commitTransaction(true);
    }

    void commitTransaction(boolean notifyLocalThread) {
        checkIfValid();
        sharedRealm.commitTransaction();
        if (notifyLocalThread) {
            sharedRealm.realmNotifier.notifyCommitByLocalThread();
        }
    }

    public void cancelTransaction() {
        checkIfValid();
        sharedRealm.cancelTransaction();
    }

    protected void checkIfValid() {
        if (sharedRealm == null || sharedRealm.isClosed()) {
            throw new IllegalStateException(BaseRealm.CLOSED_REALM_MESSAGE);
        }
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(BaseRealm.INCORRECT_THREAD_MESSAGE);
        }
    }

    protected void checkIfInTransaction() {
        if (!sharedRealm.isInTransaction()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
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
        return sharedRealm.getSchemaVersion();
    }

    @Override
    public void close() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }
        RealmCache.release(this);
    }

    void doClose() {
        if (sharedRealm != null) {
            sharedRealm.close();
            sharedRealm = null;
        }
    }

    public boolean isClosed() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }
        return sharedRealm == null || sharedRealm.isClosed();
    }

    public boolean isEmpty() {
        checkIfValid();
        return sharedRealm.isEmpty();
    }

    void setVersion(long version) {
        sharedRealm.setSchemaVersion(version);
    }

    public RealmSchema getSchema() {
        return schema;
    }

    <E extends RealmModel> E get(Class<E> clazz, long rowIndex, boolean acceptDefaultValue, List<String> excludeFields) {
        Table table = schema.getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
        E result = configuration.getSchemaMediator().newInstance(clazz, this, row, schema.getColumnInfo(clazz), acceptDefaultValue, excludeFields);
        RealmObjectProxy proxy = (RealmObjectProxy) result;
        proxy.realmGet$proxyState().setTableVersion$realm();
        return result;
    }

    <E extends RealmModel> E get(Class<E> clazz, String dynamicClassName, long rowIndex) {
        final Table table = (dynamicClassName != null) ? schema.getTable(dynamicClassName) : schema.getTable(clazz);
        E result;
        if (dynamicClassName != null) {
            @SuppressWarnings("unchecked")
            E dynamicObj = (E) new DynamicRealmObject(this, (rowIndex != Table.NO_MATCH) ? table.getUncheckedRow(rowIndex) : InvalidRow.INSTANCE, false);
            result = dynamicObj;
        } else {
            result = configuration.getSchemaMediator().newInstance(clazz, this, (rowIndex != Table.NO_MATCH) ? table.getUncheckedRow(rowIndex) : InvalidRow.INSTANCE, schema.getColumnInfo(clazz), false, Collections.<String>emptyList());
        }
        RealmObjectProxy proxy = (RealmObjectProxy) result;
        if (rowIndex != Table.NO_MATCH) {
            proxy.realmGet$proxyState().setTableVersion$realm();
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
                    RealmLog.warn("Could not delete the file %s", fileToDelete);
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
                File realmFolder = configuration.getRealmDirectory();
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
        SharedRealm sharedRealm = SharedRealm.getInstance(configuration);
        Boolean result = sharedRealm.compact();
        sharedRealm.close();
        return result;
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

    boolean hasValidNotifier() {
        return sharedRealm.realmNotifier != null && sharedRealm.realmNotifier.isValid();
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedRealm != null && !sharedRealm.isClosed()) {
            RealmLog.warn("Remember to call close() on all Realm instances. " + "Realm %s is being finalized without being closed, " + "this can lead to running out of native memory.", configuration.getPath());
        }
        super.finalize();
    }

    protected interface MigrationCallback {

        void migrationComplete();
    }

    public static final class RealmObjectContext {

        private BaseRealm realm;

        private Row row;

        private ColumnInfo columnInfo;

        private boolean acceptDefaultValue;

        private List<String> excludeFields;

        public void set(BaseRealm realm, Row row, ColumnInfo columnInfo, boolean acceptDefaultValue, List<String> excludeFields) {
            this.realm = realm;
            this.row = row;
            this.columnInfo = columnInfo;
            this.acceptDefaultValue = acceptDefaultValue;
            this.excludeFields = excludeFields;
        }

        public BaseRealm getRealm() {
            return realm;
        }

        public Row getRow() {
            return row;
        }

        public ColumnInfo getColumnInfo() {
            return columnInfo;
        }

        public boolean getAcceptDefaultValue() {
            return acceptDefaultValue;
        }

        public List<String> getExcludeFields() {
            return excludeFields;
        }

        public void clear() {
            realm = null;
            row = null;
            columnInfo = null;
            acceptDefaultValue = false;
            excludeFields = null;
        }
    }

    static final class ThreadLocalRealmObjectContext extends ThreadLocal<RealmObjectContext> {

        @Override
        protected RealmObjectContext initialValue() {
            return new RealmObjectContext();
        }
    }

    public static final ThreadLocalRealmObjectContext objectContext = new ThreadLocalRealmObjectContext();
}
