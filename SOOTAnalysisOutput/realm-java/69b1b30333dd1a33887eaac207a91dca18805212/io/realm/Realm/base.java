package io.realm;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.exceptions.RealmEncryptionNotSupportedException;
import io.realm.internal.ColumnIndices;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.Table;
import io.realm.internal.TableView;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.android.DebugAndroidLogger;
import io.realm.internal.android.ReleaseAndroidLogger;
import io.realm.internal.log.RealmLog;

public final class Realm implements Closeable {

    public static final String DEFAULT_REALM_NAME = "default.realm";

    protected static final ThreadLocal<Map<RealmConfiguration, Realm>> realmsCache = new ThreadLocal<Map<RealmConfiguration, Realm>>() {

        @Override
        protected Map<RealmConfiguration, Realm> initialValue() {
            return new HashMap<RealmConfiguration, Realm>();
        }
    };

    private static final Set<String> validatedRealmFiles = new HashSet<String>();

    private static final ThreadLocal<Map<RealmConfiguration, Integer>> referenceCount = new ThreadLocal<Map<RealmConfiguration, Integer>>() {

        @Override
        protected Map<RealmConfiguration, Integer> initialValue() {
            return new HashMap<RealmConfiguration, Integer>();
        }
    };

    private static final Map<String, List<RealmConfiguration>> globalPathConfigurationCache = new HashMap<String, List<RealmConfiguration>>();

    private static final Map<String, AtomicInteger> globalOpenInstanceCounter = new ConcurrentHashMap<String, AtomicInteger>();

    protected static final Map<Handler, String> handlers = new ConcurrentHashMap<Handler, String>();

    private static final int REALM_CHANGED = 14930352;

    private static RealmConfiguration defaultConfiguration;

    private final Map<Class<? extends RealmObject>, Table> classToTable = new HashMap<Class<? extends RealmObject>, Table>();

    private static final String INCORRECT_THREAD_MESSAGE = "Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.";

    private static final String INCORRECT_THREAD_CLOSE_MESSAGE = "Realm access from incorrect thread. Realm instance can only be closed on the thread it was created.";

    private static final String CLOSED_REALM_MESSAGE = "This Realm instance has already been closed, making it unusable.";

    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";

    @SuppressWarnings("UnusedDeclaration")
    private static SharedGroup.Durability defaultDurability = SharedGroup.Durability.FULL;

    private boolean autoRefresh;

    private Handler handler;

    private long threadId;

    private RealmConfiguration configuration;

    private SharedGroup sharedGroup;

    private final ImplicitTransaction transaction;

    private final List<WeakReference<RealmChangeListener>> changeListeners = new CopyOnWriteArrayList<WeakReference<RealmChangeListener>>();

    private static final long UNVERSIONED = -1;

    final ColumnIndices columnIndices = new ColumnIndices();

    static {
        RealmLog.add(BuildConfig.DEBUG ? new DebugAndroidLogger() : new ReleaseAndroidLogger());
    }

    protected void checkIfValid() {
        if (sharedGroup == null) {
            throw new IllegalStateException(CLOSED_REALM_MESSAGE);
        }
        if (threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_MESSAGE);
        }
    }

    private Realm(RealmConfiguration configuration, boolean autoRefresh) {
        this.threadId = Thread.currentThread().getId();
        this.configuration = configuration;
        this.sharedGroup = new SharedGroup(configuration.getPath(), true, configuration.getDurability(), configuration.getEncryptionKey());
        this.transaction = sharedGroup.beginImplicitTransaction();
        setAutoRefresh(autoRefresh);
    }

    @Override
    protected void finalize() throws Throwable {
        if (sharedGroup != null) {
            RealmLog.w("Remember to call close() on all Realm instances. " + "Realm " + configuration.getPath() + " is being finalized without being closed, " + "this can lead to running out of native memory.");
        }
        super.finalize();
    }

    @Override
    public void close() {
        if (this.threadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(INCORRECT_THREAD_CLOSE_MESSAGE);
        }
        Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
        String canonicalPath = configuration.getPath();
        Integer references = localRefCount.get(configuration);
        if (references == null) {
            references = 0;
        }
        if (sharedGroup != null && references == 1) {
            realmsCache.get().remove(configuration);
            sharedGroup.close();
            sharedGroup = null;
            synchronized (Realm.class) {
                validatedRealmFiles.remove(configuration.getPath());
                List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(canonicalPath);
                pathConfigurationCache.remove(configuration);
                if (pathConfigurationCache.isEmpty()) {
                    globalPathConfigurationCache.remove(canonicalPath);
                }
                AtomicInteger counter = globalOpenInstanceCounter.get(canonicalPath);
                if (counter.decrementAndGet() == 0) {
                    globalOpenInstanceCounter.remove(canonicalPath);
                }
            }
        }
        int refCount = references - 1;
        if (refCount < 0) {
            RealmLog.w("Calling close() on a Realm that is already closed: " + canonicalPath);
        }
        localRefCount.put(configuration, Math.max(0, refCount));
        if (handler != null && refCount <= 0) {
            removeHandler(handler);
        }
    }

    private void removeHandler(Handler handler) {
        handler.removeCallbacksAndMessages(null);
        handlers.remove(handler);
    }

    private class RealmCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (message.what == REALM_CHANGED) {
                transaction.advanceRead();
                sendNotifications();
            }
            return true;
        }
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        if (autoRefresh && Looper.myLooper() == null) {
            throw new IllegalStateException("Cannot set auto-refresh in a Thread without a Looper");
        }
        if (autoRefresh && !this.autoRefresh) {
            handler = new Handler(new RealmCallback());
            handlers.put(handler, configuration.getPath());
        } else if (!autoRefresh && this.autoRefresh && handler != null) {
            removeHandler(handler);
        }
        this.autoRefresh = autoRefresh;
    }

    public Table getTable(Class<? extends RealmObject> clazz) {
        Table table = classToTable.get(clazz);
        if (table == null) {
            clazz = Util.getOriginalModelClass(clazz);
            table = transaction.getTable(configuration.getSchemaMediator().getTableName(clazz));
            classToTable.put(clazz, table);
        }
        return table;
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
        if (references == 0) {
            AtomicInteger counter = globalOpenInstanceCounter.get(canonicalPath);
            if (counter == null) {
                globalOpenInstanceCounter.put(canonicalPath, new AtomicInteger(1));
            } else {
                counter.incrementAndGet();
            }
        }
        long currentVersion = realm.getVersion();
        long requiredVersion = configuration.getSchemaVersion();
        if (currentVersion != UNVERSIONED && currentVersion < requiredVersion && validateSchema) {
            realm.close();
            throw new RealmMigrationNeededException(canonicalPath, String.format("Realm on disc need to migrate from v%s to v%s", currentVersion, requiredVersion));
        }
        if (currentVersion != UNVERSIONED && requiredVersion < currentVersion && validateSchema) {
            realm.close();
            throw new IllegalArgumentException(String.format("Realm on disc is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
        }
        if (validateSchema) {
            try {
                initializeRealm(realm);
            } catch (RuntimeException e) {
                realm.close();
                throw e;
            }
        } else {
            loadMediatorClasses(realm);
        }
        return realm;
    }

    private static void validateAgainstExistingConfigurations(RealmConfiguration newConfiguration) {
        String realmPath = newConfiguration.getPath();
        List<RealmConfiguration> pathConfigurationCache = globalPathConfigurationCache.get(realmPath);
        if (pathConfigurationCache != null && pathConfigurationCache.size() > 0) {
            RealmConfiguration cachedConfiguration = pathConfigurationCache.get(0);
            if (!Arrays.equals(cachedConfiguration.getEncryptionKey(), newConfiguration.getEncryptionKey())) {
                throw new IllegalArgumentException(DIFFERENT_KEY_MESSAGE);
            }
            if (cachedConfiguration.getSchemaVersion() != newConfiguration.getSchemaVersion()) {
                throw new IllegalArgumentException(String.format("Configurations cannot have different schema versions " + "if used to open the same file. %d vs. %d", cachedConfiguration.getSchemaVersion(), newConfiguration.getSchemaVersion()));
            }
            RealmProxyMediator cachedSchema = cachedConfiguration.getSchemaMediator();
            RealmProxyMediator schema = newConfiguration.getSchemaMediator();
            if (!cachedSchema.equals(schema)) {
                throw new IllegalArgumentException("Two configurations with different schemas are trying to open " + "the same Realm file. Their schema must be the same: " + newConfiguration.getPath());
            }
            SharedGroup.Durability cachedDurability = cachedConfiguration.getDurability();
            SharedGroup.Durability newDurability = newConfiguration.getDurability();
            if (!cachedDurability.equals(newDurability)) {
                throw new IllegalArgumentException("A Realm cannot be both in-memory and persisted. Two conflicting " + "configurations pointing to " + newConfiguration.getPath() + " are being used.");
            }
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
                    mediator.createTable(modelClass, realm.transaction);
                }
                mediator.validateTable(modelClass, realm.transaction);
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

    private static void loadMediatorClasses(Realm realm) {
        RealmProxyMediator mediator = realm.configuration.getSchemaMediator();
        for (Class<? extends RealmObject> modelClass : mediator.getModelClasses()) {
            realm.columnIndices.addClass(modelClass, mediator.getColumnIndices(modelClass));
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

    public void writeCopyTo(File destination) throws IOException {
        writeEncryptedCopyTo(destination, null);
    }

    public void writeEncryptedCopyTo(File destination, byte[] key) throws IOException {
        if (destination == null) {
            throw new IllegalArgumentException("The destination argument cannot be null");
        }
        checkIfValid();
        transaction.writeToFile(destination, key);
    }

    public <E extends RealmObject> E createObject(Class<E> clazz) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRow();
        return get(clazz, rowIndex);
    }

    <E extends RealmObject> E createObject(Class<E> clazz, Object primaryKeyValue) {
        Table table = getTable(clazz);
        long rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue);
        return get(clazz, rowIndex);
    }

    void remove(Class<? extends RealmObject> clazz, long objectIndex) {
        getTable(clazz).moveLastOver(objectIndex);
    }

    <E extends RealmObject> E get(Class<E> clazz, long rowIndex) {
        Table table = getTable(clazz);
        UncheckedRow row = table.getUncheckedRow(rowIndex);
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

    public void addChangeListener(RealmChangeListener listener) {
        checkIfValid();
        for (WeakReference<RealmChangeListener> ref : changeListeners) {
            if (ref.get() == listener) {
                return;
            }
        }
        changeListeners.add(new WeakReference<RealmChangeListener>(listener));
    }

    public void removeChangeListener(RealmChangeListener listener) {
        checkIfValid();
        WeakReference<RealmChangeListener> weakRefToRemove = null;
        for (WeakReference<RealmChangeListener> weakRef : changeListeners) {
            if (listener == weakRef.get()) {
                weakRefToRemove = weakRef;
                break;
            }
        }
        if (weakRefToRemove != null) {
            changeListeners.remove(weakRefToRemove);
        }
    }

    public void removeAllChangeListeners() {
        checkIfValid();
        changeListeners.clear();
    }

    protected List<WeakReference<RealmChangeListener>> getChangeListeners() {
        return changeListeners;
    }

    private void sendNotifications() {
        Iterator<WeakReference<RealmChangeListener>> iterator = changeListeners.iterator();
        List<WeakReference<RealmChangeListener>> toRemoveList = null;
        while (iterator.hasNext()) {
            WeakReference<RealmChangeListener> weakRef = iterator.next();
            RealmChangeListener listener = weakRef.get();
            if (listener == null) {
                if (toRemoveList == null) {
                    toRemoveList = new ArrayList<WeakReference<RealmChangeListener>>(changeListeners.size());
                }
                toRemoveList.add(weakRef);
            } else {
                listener.onChange();
            }
        }
        if (toRemoveList != null) {
            changeListeners.removeAll(toRemoveList);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    boolean hasChanged() {
        return sharedGroup.hasChanged();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void refresh() {
        checkIfValid();
        transaction.advanceRead();
        sendNotifications();
    }

    public void beginTransaction() {
        checkIfValid();
        transaction.promoteToWrite();
    }

    public void commitTransaction() {
        checkIfValid();
        transaction.commitAndContinueAsRead();
        for (Map.Entry<Handler, String> handlerIntegerEntry : handlers.entrySet()) {
            Handler handler = handlerIntegerEntry.getKey();
            String realmPath = handlerIntegerEntry.getValue();
            if (handler.equals(this.handler)) {
                sendNotifications();
                continue;
            }
            if (realmPath.equals(configuration.getPath()) && !handler.hasMessages(REALM_CHANGED) && handler.getLooper().getThread().isAlive()) {
                handler.sendEmptyMessage(REALM_CHANGED);
            }
        }
    }

    public void cancelTransaction() {
        checkIfValid();
        transaction.rollbackAndContinueAsRead();
    }

    public void executeTransaction(Transaction transaction) {
        if (transaction == null)
            return;
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

    public void clear(Class<? extends RealmObject> clazz) {
        getTable(clazz).clear();
    }

    Handler getHandler() {
        String realmPath = configuration.getPath();
        for (Map.Entry<Handler, String> entry : handlers.entrySet()) {
            if (entry.getValue().equals(realmPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    long getVersion() {
        if (!transaction.hasTable("metadata")) {
            return UNVERSIONED;
        }
        Table metadataTable = transaction.getTable("metadata");
        return metadataTable.getLong(0, 0);
    }

    void setVersion(long version) {
        Table metadataTable = transaction.getTable("metadata");
        if (metadataTable.getColumnCount() == 0) {
            metadataTable.addColumn(ColumnType.INTEGER, "version");
            metadataTable.addEmptyRow();
        }
        metadataTable.setLong(0, 0, version);
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

    public static void migrateRealm(RealmConfiguration configuration) {
        migrateRealm(configuration, null);
    }

    public static synchronized void migrateRealm(RealmConfiguration configuration, RealmMigration migration) {
        if (configuration == null) {
            throw new IllegalArgumentException("RealmConfiguration must be provided");
        }
        if (migration == null && configuration.getMigration() == null) {
            throw new RealmMigrationNeededException(configuration.getPath(), "RealmMigration must be provided");
        }
        RealmMigration realmMigration = (migration == null) ? configuration.getMigration() : migration;
        Realm realm = null;
        try {
            realm = Realm.createAndValidate(configuration, false, Looper.myLooper() != null);
            realm.beginTransaction();
            realm.setVersion(realmMigration.execute(realm, realm.getVersion()));
            realm.commitTransaction();
            validatedRealmFiles.remove(configuration.getPath());
        } finally {
            if (realm != null) {
                realm.close();
                realmsCache.remove();
            }
        }
    }

    public static synchronized boolean deleteRealm(RealmConfiguration configuration) {
        boolean realmDeleted = true;
        String id = configuration.getPath();
        AtomicInteger counter = globalOpenInstanceCounter.get(id);
        if (counter != null && counter.get() > 0) {
            throw new IllegalStateException("It's not allowed to delete the file associated with an open Realm. " + "Remember to close() all the instances of the Realm before deleting its file.");
        }
        File realmFolder = configuration.getRealmFolder();
        String realmFileName = configuration.getRealmFileName();
        List<File> filesToDelete = Arrays.asList(new File(configuration.getPath()), new File(realmFolder, realmFileName + ".lock"), new File(realmFolder, realmFileName + ".lock_a"), new File(realmFolder, realmFileName + ".lock_b"), new File(realmFolder, realmFileName + ".log"));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    realmDeleted = false;
                    RealmLog.w("Could not delete the file " + fileToDelete);
                }
            }
        }
        return realmDeleted;
    }

    public static boolean compactRealm(RealmConfiguration configuration) {
        if (configuration.getEncryptionKey() != null) {
            throw new IllegalArgumentException("Cannot currently compact an encrypted Realm.");
        }
        String canonicalPath = configuration.getPath();
        AtomicInteger openInstances = globalOpenInstanceCounter.get(canonicalPath);
        if (openInstances != null && openInstances.get() > 0) {
            throw new IllegalStateException("Cannot compact an open Realm");
        }
        SharedGroup sharedGroup = null;
        boolean result = false;
        try {
            sharedGroup = new SharedGroup(canonicalPath, false, SharedGroup.Durability.FULL, configuration.getEncryptionKey());
            result = sharedGroup.compact();
        } finally {
            if (sharedGroup != null) {
                sharedGroup.close();
            }
        }
        return result;
    }

    public String getPath() {
        return configuration.getPath();
    }

    public RealmConfiguration getConfiguration() {
        return configuration;
    }

    static String getCanonicalPath(File realmFile) {
        try {
            return realmFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RealmException("Could not resolve the canonical path to the Realm file: " + realmFile.getAbsolutePath());
        }
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
    }
}
