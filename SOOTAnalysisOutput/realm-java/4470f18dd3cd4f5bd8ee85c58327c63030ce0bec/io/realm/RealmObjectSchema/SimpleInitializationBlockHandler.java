package io.realm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import io.realm.annotations.Required;
import io.realm.internal.Table;

public class RealmObjectSchema {

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

    static {
        SUPPORTED_SIMPLE_FIELDS = new HashMap<Class<?>, FieldMetaData>();
        SUPPORTED_SIMPLE_FIELDS.put(String.class, new FieldMetaData(RealmFieldType.STRING, true));
        SUPPORTED_SIMPLE_FIELDS.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, false));
        SUPPORTED_SIMPLE_FIELDS.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, true));
        SUPPORTED_SIMPLE_FIELDS.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, false));
        SUPPORTED_SIMPLE_FIELDS.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, true));
        SUPPORTED_SIMPLE_FIELDS.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, false));
        SUPPORTED_SIMPLE_FIELDS.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, true));
        SUPPORTED_SIMPLE_FIELDS.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        SUPPORTED_SIMPLE_FIELDS.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        SUPPORTED_SIMPLE_FIELDS.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, true));
        SUPPORTED_SIMPLE_FIELDS.put(Date.class, new FieldMetaData(RealmFieldType.DATE, true));
    }

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        SUPPORTED_LINKED_FIELDS = new HashMap<Class<?>, FieldMetaData>();
        SUPPORTED_LINKED_FIELDS.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, false));
        SUPPORTED_LINKED_FIELDS.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, false));
    }

    private final BaseRealm realm;

    final Table table;

    private final Map<String, Long> columnIndices;

    private final long nativePtr;

    RealmObjectSchema(BaseRealm realm, Table table, Map<String, Long> columnIndices) {
        this.realm = realm;
        this.table = table;
        this.columnIndices = columnIndices;
        this.nativePtr = 0;
    }

    RealmObjectSchema(String className) {
        this.realm = null;
        this.table = null;
        this.columnIndices = null;
        this.nativePtr = nativeCreateRealmObjectSchema(className);
    }

    protected RealmObjectSchema(long nativePtr) {
        this.realm = null;
        this.table = null;
        this.columnIndices = null;
        this.nativePtr = nativePtr;
    }

    public void close() {
        if (nativePtr != 0) {
            Set<Property> properties = getProperties();
            for (Property property : properties) {
                property.close();
            }
            nativeClose(nativePtr);
        }
    }

    protected long getNativePtr() {
        return nativePtr;
    }

    public String getClassName() {
        if (realm == null) {
            return nativeGetClassName(nativePtr);
        } else {
            return table.getName().substring(Table.TABLE_PREFIX.length());
        }
    }

    public RealmObjectSchema setClassName(String className) {
        realm.checkNotInSync();
        checkEmpty(className);
        String internalTableName = Table.TABLE_PREFIX + className;
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is to long. Limit is 56 characters: \'" + className + "\' (" + Integer.toString(className.length()) + ")");
        }
        if (realm.sharedRealm.hasTable(internalTableName)) {
            throw new IllegalArgumentException("Class already exists: " + className);
        }
        String oldTableName = null;
        String pkField = null;
        if (table.hasPrimaryKey()) {
            oldTableName = table.getName();
            pkField = getPrimaryKey();
            table.setPrimaryKey(null);
        }
        realm.sharedRealm.renameTable(table.getName(), internalTableName);
        if (pkField != null && !pkField.isEmpty()) {
            try {
                table.setPrimaryKey(pkField);
            } catch (Exception e) {
                realm.sharedRealm.renameTable(table.getName(), oldTableName);
                throw e;
            }
        }
        return this;
    }

    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(fieldType);
        if (metadata == null) {
            if (SUPPORTED_LINKED_FIELDS.containsKey(fieldType)) {
                throw new IllegalArgumentException("Use addRealmObjectField() instead to add fields that link to other RealmObjects: " + fieldName);
            } else {
                throw new IllegalArgumentException(String.format("Realm doesn't support this field type: %s(%s)", fieldName, fieldType));
            }
        }
        checkNewFieldName(fieldName);
        boolean nullable = metadata.defaultNullable;
        if (containsAttribute(attributes, FieldAttribute.REQUIRED)) {
            nullable = false;
        }
        long columnIndex = table.addColumn(metadata.realmType, fieldName, nullable);
        try {
            addModifiers(fieldName, attributes);
        } catch (Exception e) {
            table.removeColumn(columnIndex);
            throw e;
        }
        return this;
    }

    public RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, realm.sharedRealm.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    public RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, realm.sharedRealm.getTable(Table.TABLE_PREFIX + objectSchema.getClassName()));
        return this;
    }

    protected RealmObjectSchema add(Property property) {
        if (realm != null && nativePtr == 0) {
            throw new IllegalArgumentException("Don't use this method.");
        }
        nativeAddProperty(nativePtr, property.getNativePtr());
        return this;
    }

    private Set<Property> getProperties() {
        if (realm == null) {
            long[] ptrs = nativeGetProperties(nativePtr);
            Set<Property> properties = new LinkedHashSet<>(ptrs.length);
            for (int i = 0; i < ptrs.length; i++) {
                properties.add(new Property(ptrs[i]));
            }
            return properties;
        } else {
            throw new IllegalArgumentException("Not possible");
        }
    }

    public RealmObjectSchema removeField(String fieldName) {
        realm.checkNotInSync();
        checkLegalName(fieldName);
        if (!hasField(fieldName)) {
            throw new IllegalStateException(fieldName + " does not exist.");
        }
        long columnIndex = getColumnIndex(fieldName);
        if (table.getPrimaryKey() == columnIndex) {
            table.setPrimaryKey(null);
        }
        table.removeColumn(columnIndex);
        return this;
    }

    public RealmObjectSchema renameField(String currentFieldName, String newFieldName) {
        realm.checkNotInSync();
        checkLegalName(currentFieldName);
        checkFieldExists(currentFieldName);
        checkLegalName(newFieldName);
        checkFieldNameIsAvailable(newFieldName);
        long columnIndex = getColumnIndex(currentFieldName);
        table.renameColumn(columnIndex, newFieldName);
        return this;
    }

    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != Table.NO_MATCH;
    }

    public RealmObjectSchema addIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException(fieldName + " already has an index.");
        }
        table.addSearchIndex(columnIndex);
        return this;
    }

    public boolean hasIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        return table.hasSearchIndex(table.getColumnIndex(fieldName));
    }

    public RealmObjectSchema removeIndex(String fieldName) {
        realm.checkNotInSync();
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            throw new IllegalStateException("Field is not indexed: " + fieldName);
        }
        table.removeSearchIndex(columnIndex);
        return this;
    }

    public RealmObjectSchema addPrimaryKey(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        if (table.hasPrimaryKey()) {
            throw new IllegalStateException("A primary key is already defined");
        }
        table.setPrimaryKey(fieldName);
        long columnIndex = getColumnIndex(fieldName);
        if (!table.hasSearchIndex(columnIndex)) {
            table.addSearchIndex(columnIndex);
        }
        return this;
    }

    public RealmObjectSchema removePrimaryKey() {
        realm.checkNotInSync();
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        long columnIndex = table.getPrimaryKey();
        if (table.hasSearchIndex(columnIndex)) {
            table.removeSearchIndex(columnIndex);
        }
        table.setPrimaryKey("");
        return this;
    }

    public RealmObjectSchema setRequired(String fieldName, boolean required) {
        long columnIndex = table.getColumnIndex(fieldName);
        boolean currentColumnRequired = isRequired(fieldName);
        RealmFieldType type = table.getColumnType(columnIndex);
        if (type == RealmFieldType.OBJECT) {
            throw new IllegalArgumentException("Cannot modify the required state for RealmObject references: " + fieldName);
        }
        if (type == RealmFieldType.LIST) {
            throw new IllegalArgumentException("Cannot modify the required state for RealmList references: " + fieldName);
        }
        if (required && currentColumnRequired) {
            throw new IllegalStateException("Field is already required: " + fieldName);
        }
        if (!required && !currentColumnRequired) {
            throw new IllegalStateException("Field is already nullable: " + fieldName);
        }
        if (required) {
            table.convertColumnToNotNullable(columnIndex);
        } else {
            table.convertColumnToNullable(columnIndex);
        }
        return this;
    }

    public RealmObjectSchema setNullable(String fieldName, boolean nullable) {
        setRequired(fieldName, !nullable);
        return this;
    }

    public boolean isRequired(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return !table.isColumnNullable(columnIndex);
    }

    public boolean isNullable(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.isColumnNullable(columnIndex);
    }

    public boolean isPrimaryKey(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return columnIndex == table.getPrimaryKey();
    }

    public boolean hasPrimaryKey() {
        return table.hasPrimaryKey();
    }

    public String getPrimaryKey() {
        if (!table.hasPrimaryKey()) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        return table.getColumnName(table.getPrimaryKey());
    }

    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new LinkedHashSet<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(table.getColumnName(i));
        }
        return columnNames;
    }

    public RealmObjectSchema transform(Function function) {
        if (function != null) {
            long size = table.size();
            for (long i = 0; i < size; i++) {
                function.apply(new DynamicRealmObject(realm, table.getCheckedRow(i)));
            }
        }
        return this;
    }

    private void addModifiers(String fieldName, FieldAttribute[] attributes) {
        boolean indexAdded = false;
        try {
            if (attributes != null && attributes.length > 0) {
                if (containsAttribute(attributes, FieldAttribute.INDEXED)) {
                    addIndex(fieldName);
                    indexAdded = true;
                }
                if (containsAttribute(attributes, FieldAttribute.PRIMARY_KEY)) {
                    addPrimaryKey(fieldName);
                    indexAdded = true;
                }
            }
        } catch (Exception e) {
            long columnIndex = getColumnIndex(fieldName);
            if (indexAdded) {
                table.removeSearchIndex(columnIndex);
            }
            throw e;
        }
    }

    private boolean containsAttribute(FieldAttribute[] attributeList, FieldAttribute attribute) {
        if (attributeList == null || attributeList.length == 0) {
            return false;
        }
        for (int i = 0; i < attributeList.length; i++) {
            if (attributeList[i] == attribute) {
                return true;
            }
        }
        return false;
    }

    private void checkNewFieldName(String fieldName) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
    }

    private void checkLegalName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name can not be null or empty");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Field name can not contain '.'");
        }
    }

    private void checkFieldNameIsAvailable(String fieldName) {
        if (table.getColumnIndex(fieldName) != Table.NO_MATCH) {
            throw new IllegalArgumentException("Field already exists in '" + getClassName() + "': " + fieldName);
        }
    }

    private void checkFieldExists(String fieldName) {
        if (table.getColumnIndex(fieldName) == Table.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

    private long getColumnIndex(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format("Field name '%s' does not exist on schema for '%s", fieldName, getClassName()));
        }
        return columnIndex;
    }

    private void checkEmpty(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Null or empty class names are not allowed");
        }
    }

    long[] getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes) {
        if (fieldDescription == null || fieldDescription.equals("")) {
            throw new IllegalArgumentException("Non-empty fieldname must be provided");
        }
        if (fieldDescription.startsWith(".") || fieldDescription.endsWith(".")) {
            throw new IllegalArgumentException("Illegal field name. It cannot start or end with a '.': " + fieldDescription);
        }
        Table table = this.table;
        boolean checkColumnType = validColumnTypes != null && validColumnTypes.length > 0;
        if (fieldDescription.contains(".")) {
            String[] names = fieldDescription.split("\\.");
            long[] columnIndices = new long[names.length];
            for (int i = 0; i < names.length - 1; i++) {
                long index = table.getColumnIndex(names[i]);
                if (index < 0) {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
                RealmFieldType type = table.getColumnType(index);
                if (type == RealmFieldType.OBJECT || type == RealmFieldType.LIST) {
                    table = table.getLinkTarget(index);
                    columnIndices[i] = index;
                } else {
                    throw new IllegalArgumentException("Invalid query: " + names[i] + " does not refer to a class.");
                }
            }
            String columnName = names[names.length - 1];
            long columnIndex = table.getColumnIndex(columnName);
            columnIndices[names.length - 1] = columnIndex;
            if (columnIndex < 0) {
                throw new IllegalArgumentException(columnName + " is not a field name in class " + table.getName());
            }
            if (checkColumnType && !isValidType(table.getColumnType(columnIndex), validColumnTypes)) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch.", names[names.length - 1]));
            }
            return columnIndices;
        } else {
            Long fieldIndex = getFieldIndex(fieldDescription);
            if (fieldIndex == null) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exist.", fieldDescription));
            }
            RealmFieldType tableColumnType = table.getColumnType(fieldIndex);
            if (checkColumnType && !isValidType(tableColumnType, validColumnTypes)) {
                throw new IllegalArgumentException(String.format("Field '%s': type mismatch. Was %s, expected %s.", fieldDescription, tableColumnType, Arrays.toString(validColumnTypes)));
            }
            return new long[] { fieldIndex };
        }
    }

    private boolean isValidType(RealmFieldType columnType, RealmFieldType[] validColumnTypes) {
        for (int i = 0; i < validColumnTypes.length; i++) {
            if (validColumnTypes[i] == columnType) {
                return true;
            }
        }
        return false;
    }

    Long getFieldIndex(String fieldName) {
        return columnIndices.get(fieldName);
    }

    long getAndCheckFieldIndex(String fieldName) {
        Long index = columnIndices.get(fieldName);
        if (index == null) {
            throw new IllegalArgumentException("Field does not exist: " + fieldName);
        }
        return index;
    }

    public RealmFieldType getFieldType(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.getColumnType(columnIndex);
    }

    public interface Function {

        void apply(DynamicRealmObject obj);
    }

    private static class FieldMetaData {

        public final RealmFieldType realmType;

        public final boolean defaultNullable;

        public FieldMetaData(RealmFieldType realmType, boolean defaultNullable) {
            this.realmType = realmType;
            this.defaultNullable = defaultNullable;
        }
    }

    static final class DynamicColumnMap implements Map<String, Long> {

        private final Table table;

        public DynamicColumnMap(Table table) {
            this.table = table;
        }

        @Override
        public Long get(Object key) {
            long ret = table.getColumnIndex((String) key);
            return ret < 0 ? null : ret;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, Long>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long put(String key, Long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Long> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Long> values() {
            throw new UnsupportedOperationException();
        }
    }

    static native long nativeCreateRealmObjectSchema(String className);

    static native void nativeAddProperty(long nativePtr, long nativePropertyPtr);

    static native long[] nativeGetProperties(long nativePtr);

    static native void nativeClose(long nativePtr);

    static native String nativeGetClassName(long nativePtr);
}