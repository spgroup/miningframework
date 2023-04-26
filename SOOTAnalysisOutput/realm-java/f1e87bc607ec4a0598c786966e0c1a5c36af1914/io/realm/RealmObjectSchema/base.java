package io.realm;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import io.realm.annotations.Required;
import io.realm.internal.ColumnInfo;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

public class RealmObjectSchema {

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(String.class, new FieldMetaData(RealmFieldType.STRING, true));
        m.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, false));
        m.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, true));
        m.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, false));
        m.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, true));
        m.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, false));
        m.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, true));
        m.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, false));
        m.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, true));
        m.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, true));
        m.put(Date.class, new FieldMetaData(RealmFieldType.DATE, true));
        SUPPORTED_SIMPLE_FIELDS = Collections.unmodifiableMap(m);
    }

    private static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, false));
        m.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, false));
        SUPPORTED_LINKED_FIELDS = Collections.unmodifiableMap(m);
    }

    private final RealmSchema schema;

    private final BaseRealm realm;

    private final ColumnInfo columnInfo;

    private final Table table;

    RealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table) {
        this(realm, schema, table, new DynamicColumnIndices(table));
    }

    RealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table, ColumnInfo columnInfo) {
        this.schema = schema;
        this.realm = realm;
        this.table = table;
        this.columnInfo = columnInfo;
    }

    @Deprecated
    public void close() {
    }

    public String getClassName() {
        return table.getClassName();
    }

    public RealmObjectSchema setClassName(String className) {
        realm.checkNotInSync();
        checkEmpty(className);
        String internalTableName = Table.getTableNameForClass(className);
        if (internalTableName.length() > Table.TABLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Class name is too long. Limit is 56 characters: \'" + className + "\' (" + Integer.toString(className.length()) + ")");
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
                throw new IllegalArgumentException(String.format(Locale.US, "Realm doesn't support this field type: %s(%s)", fieldName, fieldType));
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
        table.addColumnLink(RealmFieldType.OBJECT, fieldName, realm.sharedRealm.getTable(Table.getTableNameForClass(objectSchema.getClassName())));
        return this;
    }

    public RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        checkLegalName(fieldName);
        checkFieldNameIsAvailable(fieldName);
        table.addColumnLink(RealmFieldType.LIST, fieldName, realm.sharedRealm.getTable(Table.getTableNameForClass(objectSchema.getClassName())));
        return this;
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

    public RealmFieldType getFieldType(String fieldName) {
        long columnIndex = getColumnIndex(fieldName);
        return table.getColumnType(columnIndex);
    }

    protected final FieldDescriptor getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes) {
        return FieldDescriptor.createStandardFieldDescriptor(getSchemaConnector(), getTable(), fieldDescription, validColumnTypes);
    }

    RealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        long columnIndex = table.addColumn(type, name, (required) ? Table.NOT_NULLABLE : Table.NULLABLE);
        if (indexed) {
            table.addSearchIndex(columnIndex);
        }
        if (primary) {
            table.setPrimaryKey(name);
        }
        return this;
    }

    RealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        table.addColumnLink(type, name, realm.getSharedRealm().getTable(Table.getTableNameForClass(linkedTo.getClassName())));
        return this;
    }

    long getAndCheckFieldIndex(String fieldName) {
        long index = columnInfo.getColumnIndex(fieldName);
        if (index < 0) {
            throw new IllegalArgumentException("Field does not exist: " + fieldName);
        }
        return index;
    }

    Table getTable() {
        return table;
    }

    private SchemaConnector getSchemaConnector() {
        return new SchemaConnector(schema);
    }

    public interface Function {

        void apply(DynamicRealmObject obj);
    }

    long getFieldIndex(String fieldName) {
        return columnInfo.getColumnIndex(fieldName);
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
            throw (RuntimeException) e;
        }
    }

    private boolean containsAttribute(FieldAttribute[] attributeList, FieldAttribute attribute) {
        if (attributeList == null || attributeList.length == 0) {
            return false;
        }
        for (FieldAttribute anAttributeList : attributeList) {
            if (anAttributeList == attribute) {
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
            throw new IllegalArgumentException(String.format(Locale.US, "Field name '%s' does not exist on schema for '%s'", fieldName, getClassName()));
        }
        return columnIndex;
    }

    private void checkEmpty(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Null or empty class names are not allowed");
        }
    }

    private static final class DynamicColumnIndices extends ColumnInfo {

        private final Table table;

        DynamicColumnIndices(Table table) {
            super(null, false);
            this.table = table;
        }

        @Override
        public long getColumnIndex(String columnName) {
            return table.getColumnIndex(columnName);
        }

        @Override
        public RealmFieldType getColumnType(String columnName) {
            throw new UnsupportedOperationException("DynamicColumnIndices do not support 'getColumnType'");
        }

        @Override
        public String getLinkedTable(String columnName) {
            throw new UnsupportedOperationException("DynamicColumnIndices do not support 'getLinkedTable'");
        }

        @Override
        public void copyFrom(ColumnInfo src) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot be copied");
        }

        @Override
        protected ColumnInfo copy(boolean immutable) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot be copied");
        }

        @Override
        protected void copy(ColumnInfo src, ColumnInfo dst) {
            throw new UnsupportedOperationException("DynamicColumnIndices cannot copy");
        }
    }

    private static final class FieldMetaData {

        final RealmFieldType realmType;

        final boolean defaultNullable;

        FieldMetaData(RealmFieldType realmType, boolean defaultNullable) {
            this.realmType = realmType;
            this.defaultNullable = defaultNullable;
        }
    }
}
