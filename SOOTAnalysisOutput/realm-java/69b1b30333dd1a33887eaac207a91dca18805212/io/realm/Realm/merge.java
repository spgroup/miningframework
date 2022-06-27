package io.realm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import io.realm.exceptions.RealmEncryptionNotSupportedException;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.log.RealmLog;

public final class Realm extends BaseRealm {

    public static final String DEFAULT_REALM_NAME = RealmConfiguration.DEFAULT_REALM_NAME;

    protected static final ThreadLocal<Map<RealmConfiguration, Realm>> realmsCache = new ThreadLocal<Map<RealmConfiguration, Realm>>() {

        @Override
        protected Map<RealmConfiguration, Realm> initialValue() {
            return new HashMap<RealmConfiguration, Realm>();
        }
    };

    private static final ThreadLocal<Map<RealmConfiguration, Integer>> referenceCount = new ThreadLocal<Map<RealmConfiguration, Integer>>() {

        @Override
        protected Map<RealmConfiguration, Integer> initialValue() {
            return new HashMap<RealmConfiguration, Integer>();
        }
    };

    private final Map<Class<? extends RealmObject>, Table> classToTable = new HashMap<Class<? extends RealmObject>, Table>();

    private static RealmConfiguration defaultConfiguration;

    protected ColumnIndices columnIndices = new ColumnIndices();

    private Realm(RealmConfiguration configuration, boolean autoRefresh) {
        super(configuration, autoRefresh);
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedGroupManager != null && sharedGroupManager.isOpen()) {
            RealmLog.w("Remember to call close() on all Realm instances. " + "Realm " + configuration.getPath() + " is being finalized without being closed, " + "this can lead to running out of native memory.");
        }
        super.finalize();
    }

    public static Realm getInstance(Context context) {
        return Realm.getInstance(new RealmConfiguration.Builder(context).name(DEFAULT_REALM_NAME).build());
    }

    public static Realm getDefaultInstance() {
        if (defaultConfiguration == null) {
            throw new NullPointerException("No default RealmConfiguration was found. Call setDefaultConfiguration() first");
        }
        return create(defaultConfiguration);
    }

    public static Realm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return create(configuration);
    }

    public static void setDefaultConfiguration(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        defaultConfiguration = configuration;
    }

    public static void removeDefaultConfiguration() {
        defaultConfiguration = null;
    }

    private static synchronized Realm create(RealmConfiguration configuration) {
        boolean autoRefresh = Looper.myLooper() != null;
        try {
            boolean validateSchema = !validatedRealmFiles.contains(configuration.getPath());
            return createAndValidate(configuration, validateSchema, autoRefresh);
        } catch (RealmMigrationNeededException e) {
            if (configuration.shouldDeleteRealmIfMigrationNeeded()) {
                deleteRealm(configuration);
            } else {
                migrateRealm(configuration);
            }
            return createAndValidate(configuration, true, autoRefresh);
        }
    }

    private static Realm createAndValidate(RealmConfiguration configuration, boolean validateSchema, boolean autoRefresh) {
        synchronized (BaseRealm.class) {
            String canonicalPath = configuration.getPath();
            Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
            Integer references = localRefCount.get(configuration);
            if (references == null) {
                references = 0;
            }
            Map<RealmConfiguration, Realm> realms = realmsCache.get();
            Realm realm = realms.get(configuration);
            if (realm != null) {
                localRefCount.put(configuration, references + 1);
                return realm;
            }
            validateAgainstExistingConfigurations(configuration);
            realm = new Realm(configuration, autoRefresh);
            List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(canonicalPath);
            if (pathConfigurationCache == null) {
                pathConfigurationCache = new CopyOnWriteArrayList<RealmConfiguration>();
                globalPathConfigurationCache.put(canonicalPath, pathConfigurationCache);
            }
            pathConfigurationCache.add(configuration);
            realms.put(configuration, realm);
            localRefCount.put(configuration, references + 1);
            realm.acquireFileReference(configuration);
            long currentVersion = realm.getVersion();
            long requiredVersion = configuration.getSchemaVersion();
            if (currentVersion != UNVERSIONED && currentVersion < requiredVersion && validateSchema) {
                realm.close();
                throw new RealmMigrationNeededException(configuration.getPath(), String.format("Realm on disk need to migrate from v%s to v%s", currentVersion, requiredVersion));
            }
            if (currentVersion != UNVERSIONED && requiredVersion < currentVersion && validateSchema) {
                realm.close();
                throw new IllegalArgumentException(String.format("Realm on disk is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
            }
            if (validateSchema) {
                try {
                    initializeRealm(realm);
                } catch (RuntimeException e) {
                    realm.close();
                    throw e;
                }
            }
            return realm;
        }
    }

    @SuppressWarnings("unchecked")
    private static void initializeRealm(Realm realm) {
        long version = realm.getVersion();
        boolean commitNeeded = false;
        try {
            realm.beginTransaction();
            if (version == UNVERSIONED) {
                commitNeeded = true;
                realm.setVersion(realm.configuration.getSchemaVersion());
            }
            RealmProxyMediator mediator = realm.configuration.getSchemaMediator();
            for (Class<? extends RealmObject> modelClass : mediator.getModelClasses()) {
                if (version == UNVERSIONED) {
                    mediator.createTable(modelClass, realm.sharedGroupManager.getTransaction());
                }
                mediator.validateTable(modelClass, realm.sharedGroupManager.getTransaction());
                realm.columnIndices.addClass(modelClass, mediator.getColumnIndices(modelClass));
            }
            validatedRealmFiles.add(realm.getPath());
        } finally {
            if (commitNeeded) {
                realm.commitTransaction();
            } else {
                realm.cancelTransaction();
            }
        }
    }

    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), false);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, JSONArray json) {
        if (clazz == null || json == null) {
            return;
        }
        checkHasPrimaryKey(clazz);
        for (int i = 0; i < json.length(); i++) {
            try {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            } catch (Exception e) {
                throw new RealmException("Could not map Json", e);
            }
        }
    }

    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }
        JSONArray arr;
        try {
            arr = new JSONArray(json);
        } catch (Exception e) {
            throw new RealmException("Could not create JSON array from string", e);
        }
        createAllFromJson(clazz, arr);
    }

    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return;
        }
        checkHasPrimaryKey(clazz);
        JSONArray arr;
        try {
            arr = new JSONArray(json);
        } catch (JSONException e) {
            throw new RealmException("Could not create JSON array from string", e);
        }
        createOrUpdateAllFromJson(clazz, arr);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> void createAllFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return;
        }
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                configuration.getSchemaMediator().createUsingJsonStream(clazz, this, reader);
            }
            reader.endArray();
        } finally {
            reader.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> void createOrUpdateAllFromJson(Class<E> clazz, InputStream in) throws IOException {
        if (clazz == null || in == null) {
            return;
        }
        checkHasPrimaryKey(clazz);
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONArray json = new JSONArray(scanner.next());
            for (int i = 0; i < json.length(); i++) {
                configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json.getJSONObject(i), true);
            }
        } catch (JSONException e) {
            throw new RealmException("Failed to read JSON", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        try {
            return configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, false);
        } catch (Exception e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, JSONObject json) {
        if (clazz == null || json == null) {
            return null;
        }
        checkHasPrimaryKey(clazz);
        try {
            return configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, true);
        } catch (JSONException e) {
            throw new RealmException("Could not map Json", e);
        }
    }

    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            throw new RealmException("Could not create Json object from string", e);
        }
        return createObjectFromJson(clazz, obj);
    }

    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, String json) {
        if (clazz == null || json == null || json.length() == 0) {
            return null;
        }
        checkHasPrimaryKey(clazz);
        JSONObject obj;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            throw new RealmException("Could not create Json object from string", e);
        }
        return createOrUpdateObjectFromJson(clazz, obj);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createObjectFromJson(Class<E> clazz, InputStream inputStream) throws IOException {
        if (clazz == null || inputStream == null) {
            return null;
        }
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return configuration.getSchemaMediator().createUsingJsonStream(clazz, this, reader);
        } finally {
            reader.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public <E extends RealmObject> E createOrUpdateObjectFromJson(Class<E> clazz, InputStream in) throws IOException {
        if (clazz == null || in == null) {
            return null;
        }
        checkHasPrimaryKey(clazz);
        Scanner scanner = null;
        try {
            scanner = getFullStringScanner(in);
            JSONObject json = new JSONObject(scanner.next());
            return configuration.getSchemaMediator().createOrUpdateUsingJsonObject(clazz, this, json, true);
        } catch (JSONException e) {
            throw new RealmException("Failed to read JSON", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private Scanner getFullStringScanner(InputStream in) {
        return new Scanner(in, "UTF-8").useDelimiter("\\A");
    }

    public <E extends RealmObject> E createObject(Class<E> clazz) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRow();
        return getByIndex(clazz, rowIndex);
    }

    <E extends RealmObject> E createObject(Class<E> clazz, Object primaryKeyValue) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        return getByIndex(clazz, rowIndex);
    }

    void remove(Class<? extends RealmObject> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    <E extends RealmObject> E getByIndex(Class<E> clazz, long rowIndex) {
        Table table = getTable(clazz);
        UncheckedRow row = table.getUncheckedRowByIndex(rowIndex);
        E result = configuration.getSchemaMediator().newInstance(clazz);
        result.row = row;
        result.realm = this;
        return result;
    }

    public <E extends RealmObject> E copyToRealm(E object) {
        checkNotNullObject(object);
        return copyOrUpdate(object, false);
    }

    public <E extends RealmObject> E copyToRealmOrUpdate(E object) {
        checkNotNullObject(object);
        checkHasPrimaryKey(object.getClass());
        return copyOrUpdate(object, true);
    }

    public <E extends RealmObject> List<E> copyToRealm(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<E>();
        }
        ArrayList<E> realmObjects = new ArrayList<E>();
        for (E object : objects) {
            realmObjects.add(copyToRealm(object));
        }
        return realmObjects;
    }

    public <E extends RealmObject> List<E> copyToRealmOrUpdate(Iterable<E> objects) {
        if (objects == null) {
            return new ArrayList<E>();
        }
        ArrayList<E> realmObjects = new ArrayList<E>();
        for (E object : objects) {
            realmObjects.add(copyToRealmOrUpdate(object));
        }
        return realmObjects;
    }

    boolean contains(Class<? extends RealmObject> clazz) {
        return configuration.getSchemaMediator().getModelClasses().contains(clazz);
    }

    public <E extends RealmObject> RealmQuery<E> where(Class<E> clazz) {
        checkIfValid();
        return new RealmQuery<E>(this, clazz);
    }

    public <E extends RealmObject> RealmResults<E> allObjects(Class<E> clazz) {
        return where(clazz).findAll();
    }

    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName, boolean sortAscending) {
        checkIfValid();
        Table table = getTable(clazz);
        TableView.Order order = sortAscending ? TableView.Order.ascending : TableView.Order.descending;
        long columnIndex = columnIndices.getColumnIndex(clazz, fieldName);
        if (columnIndex < 0) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
        }
        TableView tableView = table.getSortedView(columnIndex, order);
        return new RealmResults<E>(this, tableView, clazz);
    }

    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1, boolean sortAscending1, String fieldName2, boolean sortAscending2) {
        return allObjectsSorted(clazz, new String[] { fieldName1, fieldName2 }, new boolean[] { sortAscending1, sortAscending2 });
    }

    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String fieldName1, boolean sortAscending1, String fieldName2, boolean sortAscending2, String fieldName3, boolean sortAscending3) {
        return allObjectsSorted(clazz, new String[] { fieldName1, fieldName2, fieldName3 }, new boolean[] { sortAscending1, sortAscending2, sortAscending3 });
    }

    @SuppressWarnings("unchecked")
    public <E extends RealmObject> RealmResults<E> allObjectsSorted(Class<E> clazz, String[] fieldNames, boolean[] sortAscending) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames must be provided.");
        } else if (sortAscending == null) {
            throw new IllegalArgumentException("sortAscending must be provided.");
        }
        Table table = this.getTable(clazz);
        long[] columnIndices = new long[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            long columnIndex = table.getColumnIndex(fieldName);
            if (columnIndex == -1) {
                throw new IllegalArgumentException(String.format("Field name '%s' does not exist.", fieldName));
            }
            columnIndices[i] = columnIndex;
        }
        TableView tableView = table.getSortedView(columnIndices, sortAscending);
        return new RealmResults(this, tableView, clazz);
    }

    protected List<WeakReference<RealmChangeListener>> getChangeListeners() {
        return changeListeners;
    }

    @SuppressWarnings("UnusedDeclaration")
    boolean hasChanged() {
        return sharedGroupManager.hasChanged();
    }

    public void executeTransaction(Transaction transaction) {
        if (transaction == null)
            throw new IllegalArgumentException("transaction should not be null");
        beginTransaction();
        try {
            transaction.execute(this);
            commitTransaction();
        } catch (RuntimeException e) {
            cancelTransaction();
            throw new RealmException("Error during transaction.", e);
        } catch (Error e) {
            cancelTransaction();
            throw e;
        }
    }

    public Request executeTransaction(final Transaction transaction, final Transaction.Callback callback) {
        if (transaction == null)
            throw new IllegalArgumentException("transaction should not be null");
        if (callback == null)
            throw new IllegalArgumentException("callback should not be null");
        final Handler handler = new Handler();
        final RealmConfiguration realmConfiguration = getConfiguration();
        final Future<?> pendingQuery = asyncQueryExecutor.submit(new Runnable() {

            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    Realm bgRealm = Realm.getInstance(realmConfiguration);
                    bgRealm.beginTransaction();
                    try {
                        transaction.execute(bgRealm);
                        if (!Thread.currentThread().isInterrupted()) {
                            bgRealm.commitTransaction();
                            if (callback != null && !Thread.currentThread().isInterrupted() && handler.getLooper().getThread().isAlive()) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        callback.onSuccess();
                                    }
                                });
                            }
                        } else {
                            bgRealm.cancelTransaction();
                        }
                    } catch (final Exception e) {
                        bgRealm.cancelTransaction();
                        if (callback != null && !Thread.currentThread().isInterrupted() && handler.getLooper().getThread().isAlive()) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    callback.onError(e);
                                }
                            });
                        }
                    } finally {
                        bgRealm.close();
                    }
                }
            }
        });
        return new Request(pendingQuery);
    }

    public void clear(Class<? extends RealmObject> clazz) {
        getTable(clazz).clear();
    }

    @SuppressWarnings("unchecked")
    private <E extends RealmObject> Class<? extends RealmObject> getRealmClassFromObject(E object) {
        if (object.realm != null) {
            return (Class<? extends RealmObject>) object.getClass().getSuperclass();
        } else {
            return object.getClass();
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends RealmObject> E copyOrUpdate(E object, boolean update) {
        return configuration.getSchemaMediator().copyOrUpdate(this, object, update, new HashMap<RealmObject, RealmObjectProxy>());
    }

    private <E extends RealmObject> void checkNotNullObject(E object) {
        if (object == null) {
            throw new IllegalArgumentException("Null objects cannot be copied into Realm.");
        }
    }

    private <E extends RealmObject> void checkHasPrimaryKey(E object) {
        Class<? extends RealmObject> objectClass = object.getClass();
        if (!getTable(objectClass).hasPrimaryKey()) {
            throw new IllegalArgumentException("RealmObject has no @PrimaryKey defined: " + objectClass.getSimpleName());
        }
    }

    private void checkHasPrimaryKey(Class<? extends RealmObject> clazz) {
        if (!getTable(clazz).hasPrimaryKey()) {
            throw new IllegalArgumentException("A RealmObject with no @PrimaryKey cannot be updated: " + clazz.toString());
        }
    }

    protected void checkIfValid() {
        super.checkIfValid();
    }

    @Override
    protected Map<RealmConfiguration, Integer> getLocalReferenceCount() {
        return referenceCount.get();
    }

    @Override
    protected void lastLocalInstanceClosed() {
        validatedRealmFiles.remove(configuration.getPath());
        realmsCache.get().remove(configuration);
    }

    public static void migrateRealm(RealmConfiguration configuration) {
        migrateRealm(configuration, null);
    }

    public static void migrateRealm(RealmConfiguration configuration, RealmMigration migration) {
        BaseRealm.migrateRealm(configuration, migration, new MigrationCallback() {

            @Override
            public BaseRealm getRealm(RealmConfiguration configuration) {
                return Realm.createAndValidate(configuration, false, Looper.myLooper() != null);
            }

            @Override
            public void migrationComplete() {
                realmsCache.remove();
            }
        });
    }

    public static boolean deleteRealm(RealmConfiguration configuration) {
        return BaseRealm.deleteRealm(configuration);
    }

    public static boolean compactRealm(RealmConfiguration configuration) {
        return BaseRealm.compactRealm(configuration);
    }

    static String getCanonicalPath(File realmFile) {
        try {
            return realmFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RealmException("Could not resolve the canonical path to the Realm file: " + realmFile.getAbsolutePath());
        }
    }

    static Map<Handler, String> getHandlers() {
        return handlers;
    }

    public Table getTable(Class<? extends RealmObject> clazz) {
        Table table = classToTable.get(clazz);
        if (table == null) {
            clazz = Util.getOriginalModelClass(clazz);
            table = sharedGroupManager.getTable(configuration.getSchemaMediator().getTableName(clazz));
            classToTable.put(clazz, table);
        }
        return table;
    }

    public static Object getDefaultModule() {
        String moduleName = "io.realm.DefaultRealmModule";
        Class<?> clazz;
        try {
            clazz = Class.forName(moduleName);
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InvocationTargetException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (InstantiationException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Could not create an instance of " + moduleName, e);
        }
    }

    public interface Transaction {

        void execute(Realm realm);

        class Callback {

            public void onSuccess() {
            }

            public void onError(Exception e) {
            }
        }
    }

    public static class Request {

        private final Future<?> pendingQuery;

        private volatile boolean isCancelled = false;

        public Request(Future<?> pendingQuery) {
            this.pendingQuery = pendingQuery;
        }

        public void cancel() {
            pendingQuery.cancel(true);
            isCancelled = true;
            asyncQueryExecutor.getQueue().remove(pendingQuery);
        }

        public boolean isCancelled() {
            return isCancelled;
        }
    }
}
