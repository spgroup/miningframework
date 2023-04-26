package io.realm;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import io.realm.annotations.Required;
import io.realm.internal.CheckedRow;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsObjectStore;
import io.realm.internal.OsResults;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

public abstract class RealmObjectSchema {

    static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(String.class, new FieldMetaData(RealmFieldType.STRING, RealmFieldType.STRING_LIST, true));
        m.put(short.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Short.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(int.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Integer.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(long.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Long.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(float.class, new FieldMetaData(RealmFieldType.FLOAT, RealmFieldType.FLOAT_LIST, false));
        m.put(Float.class, new FieldMetaData(RealmFieldType.FLOAT, RealmFieldType.FLOAT_LIST, true));
        m.put(double.class, new FieldMetaData(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE_LIST, false));
        m.put(Double.class, new FieldMetaData(RealmFieldType.DOUBLE, RealmFieldType.DOUBLE_LIST, true));
        m.put(boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN_LIST, false));
        m.put(Boolean.class, new FieldMetaData(RealmFieldType.BOOLEAN, RealmFieldType.BOOLEAN_LIST, true));
        m.put(byte.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, false));
        m.put(Byte.class, new FieldMetaData(RealmFieldType.INTEGER, RealmFieldType.INTEGER_LIST, true));
        m.put(byte[].class, new FieldMetaData(RealmFieldType.BINARY, RealmFieldType.BINARY_LIST, true));
        m.put(Date.class, new FieldMetaData(RealmFieldType.DATE, RealmFieldType.DATE_LIST, true));
        SUPPORTED_SIMPLE_FIELDS = Collections.unmodifiableMap(m);
    }

    static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, null, false));
        m.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, null, false));
        SUPPORTED_LINKED_FIELDS = Collections.unmodifiableMap(m);
    }

    final RealmSchema schema;

    final BaseRealm realm;

    final Table table;

    private final ColumnInfo columnInfo;

    RealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table, ColumnInfo columnInfo) {
        this.schema = schema;
        this.realm = realm;
        this.table = table;
        this.columnInfo = columnInfo;
    }

    public String getClassName() {
        return table.getClassName();
    }

    public abstract RealmObjectSchema setClassName(String className);

    public abstract RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes);

    public abstract RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema addRealmListField(String fieldName, Class<?> primitiveType);

    public abstract RealmObjectSchema removeField(String fieldName);

    public abstract RealmObjectSchema renameField(String currentFieldName, String newFieldName);

    public boolean hasField(String fieldName) {
        return table.getColumnKey(fieldName) != Table.NO_MATCH;
    }

    public abstract RealmObjectSchema addIndex(String fieldName);

    public boolean hasIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        return table.hasSearchIndex(table.getColumnKey(fieldName));
    }

    public abstract RealmObjectSchema removeIndex(String fieldName);

    public abstract RealmObjectSchema addPrimaryKey(String fieldName);

    public abstract RealmObjectSchema removePrimaryKey();

    public abstract RealmObjectSchema setRequired(String fieldName, boolean required);

    public abstract RealmObjectSchema setNullable(String fieldName, boolean nullable);

    public boolean isRequired(String fieldName) {
        long columnIndex = getColumnKey(fieldName);
        return !table.isColumnNullable(columnIndex);
    }

    public boolean isNullable(String fieldName) {
        long columnIndex = getColumnKey(fieldName);
        return table.isColumnNullable(columnIndex);
    }

    public boolean isPrimaryKey(String fieldName) {
        checkFieldExists(fieldName);
        return fieldName.equals(OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName()));
    }

    public boolean hasPrimaryKey() {
        return OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName()) != null;
    }

    public String getPrimaryKey() {
        String pkField = OsObjectStore.getPrimaryKeyForObject(realm.sharedRealm, getClassName());
        if (pkField == null) {
            throw new IllegalStateException(getClassName() + " doesn't have a primary key.");
        }
        return pkField;
    }

    public Set<String> getFieldNames() {
        int columnCount = (int) table.getColumnCount();
        Set<String> columnNames = new LinkedHashSet<>(columnCount);
        for (String column : table.getColumnNames()) {
            columnNames.add(column);
        }
        return columnNames;
    }

    public abstract RealmObjectSchema transform(Function function);

    public RealmFieldType getFieldType(String fieldName) {
        long columnKey = getColumnKey(fieldName);
        return table.getColumnType(columnKey);
    }

    abstract FieldDescriptor getFieldDescriptors(String fieldDescription, RealmFieldType... validColumnTypes);

    RealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        long columnIndex = table.addColumn(type, name, (required) ? Table.NOT_NULLABLE : Table.NULLABLE);
        if (indexed) {
            table.addSearchIndex(columnIndex);
        }
        if (primary) {
            OsObjectStore.setPrimaryKeyForObject(realm.sharedRealm, getClassName(), name);
        }
        return this;
    }

    RealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        table.addColumnLink(type, name, realm.getSharedRealm().getTable(Table.getTableNameForClass(linkedTo.getClassName())));
        return this;
    }

    long getAndCheckFieldColumnKey(String fieldName) {
        long columnKey = columnInfo.getColumnKey(fieldName);
        if (columnKey < 0) {
            throw new IllegalArgumentException("Field does not exist: " + fieldName);
        }
        return columnKey;
    }

    Table getTable() {
        return table;
    }

    static final Map<Class<?>, FieldMetaData> getSupportedSimpleFields() {
        return SUPPORTED_SIMPLE_FIELDS;
    }

    protected final SchemaConnector getSchemaConnector() {
        return new SchemaConnector(schema);
    }

    public interface Function {

        void apply(DynamicRealmObject obj);
    }

    long getFieldColumnKey(String fieldName) {
        return columnInfo.getColumnKey(fieldName);
    }

    static void checkLegalName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name can not be null or empty");
        }
        if (fieldName.contains(".")) {
            throw new IllegalArgumentException("Field name can not contain '.'");
        }
        if (fieldName.length() > 63) {
            throw new IllegalArgumentException("Field name is currently limited to max 63 characters.");
        }
    }

    void checkFieldExists(String fieldName) {
        if (table.getColumnKey(fieldName) == Table.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

<<<<<<< MINE
@Override
        public long getColumnKey(String columnName) {
            return table.getColumnKey(columnName);
=======
long getColumnIndex(String fieldName) {
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format(Locale.US, "Field name '%s' does not exist on schema for '%s'", fieldName, getClassName()));
        }
        return columnIndex;
>>>>>>> YOURS
        }

    static final class DynamicColumnIndices extends ColumnInfo {

        private final Table table;

        DynamicColumnIndices(Table table) {
            super(null, false);
            this.table = table;
        }

        @Override
        public long getColumnKey(String columnName) {
            return table.getColumnKey(columnName);
        }

        @Override
        public ColumnDetails getColumnDetails(String columnName) {
            throw new UnsupportedOperationException("DynamicColumnIndices do not support 'getColumnDetails'");
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

    static final class FieldMetaData {

        final RealmFieldType fieldType;

        final RealmFieldType listType;

        final boolean defaultNullable;

        FieldMetaData(RealmFieldType fieldType, @Nullable RealmFieldType listType, boolean defaultNullable) {
            this.fieldType = fieldType;
            this.listType = listType;
            this.defaultNullable = defaultNullable;
        }
    }
}