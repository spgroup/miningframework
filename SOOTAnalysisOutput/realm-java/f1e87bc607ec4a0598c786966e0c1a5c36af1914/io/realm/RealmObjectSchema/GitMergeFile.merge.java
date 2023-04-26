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
import io.realm.internal.OsObject;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

public abstract class RealmObjectSchema {

    static final Map<Class<?>, FieldMetaData> SUPPORTED_SIMPLE_FIELDS;

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

    static final Map<Class<?>, FieldMetaData> SUPPORTED_LINKED_FIELDS;

    static {
        Map<Class<?>, FieldMetaData> m = new HashMap<>();
        m.put(RealmObject.class, new FieldMetaData(RealmFieldType.OBJECT, false));
        m.put(RealmList.class, new FieldMetaData(RealmFieldType.LIST, false));
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

    @Deprecated
    public void close() {
    }

    public String getClassName() {
        return table.getClassName();
    }

    public abstract RealmObjectSchema setClassName(String className);

<<<<<<< MINE
    public abstract RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes);
=======
    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        FieldMetaData metadata = SUPPORTED_SIMPLE_FIELDS.get(fieldType);
        if (metadata == null) {
            if (SUPPORTED_LINKED_FIELDS.containsKey(fieldType)) {
                throw new IllegalArgumentException("Use addRealmObjectField() instead to add fields that link to other RealmObjects: " + fieldName);
            } else {
                throw new IllegalArgumentException(String.format(Locale.US, "Realm doesn't support this field type: %s(%s)", fieldName, fieldType));
            }
        }
        if (containsAttribute(attributes, FieldAttribute.PRIMARY_KEY)) {
            checkAddPrimaryKeyForSync();
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
>>>>>>> YOURS

    public abstract RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema removeField(String fieldName);

    public abstract RealmObjectSchema renameField(String currentFieldName, String newFieldName);

    public boolean hasField(String fieldName) {
        return table.getColumnIndex(fieldName) != Table.NO_MATCH;
    }

    public abstract RealmObjectSchema addIndex(String fieldName);

    public boolean hasIndex(String fieldName) {
        checkLegalName(fieldName);
        checkFieldExists(fieldName);
        return table.hasSearchIndex(table.getColumnIndex(fieldName));
    }

    public abstract RealmObjectSchema removeIndex(String fieldName);

<<<<<<< MINE
    public abstract RealmObjectSchema addPrimaryKey(String fieldName);
=======
    public RealmObjectSchema addPrimaryKey(String fieldName) {
        checkAddPrimaryKeyForSync();
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
>>>>>>> YOURS

    public abstract RealmObjectSchema removePrimaryKey();

    public abstract RealmObjectSchema setRequired(String fieldName, boolean required);

    public abstract RealmObjectSchema setNullable(String fieldName, boolean nullable);

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
            String name = table.getColumnName(i);
            if (!OsObject.isObjectIdColumn(name)) {
                columnNames.add(name);
            }
        }
        return columnNames;
    }

    public abstract RealmObjectSchema transform(Function function);

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

    static final Map<Class<?>, FieldMetaData> getSupportedSimpleFields() {
        return SUPPORTED_SIMPLE_FIELDS;
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

<<<<<<< MINE
    void checkLegalName(String fieldName) {
=======
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

    static boolean containsAttribute(FieldAttribute[] attributeList, FieldAttribute attribute) {
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

    static void checkLegalName(String fieldName) {
>>>>>>> YOURS
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
        if (table.getColumnIndex(fieldName) == Table.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist on object '" + getClassName() + "': " + fieldName);
        }
    }

<<<<<<< MINE
    long getColumnIndex(String fieldName) {
=======
    private void checkAddPrimaryKeyForSync() {
        if (realm.configuration.isSyncConfiguration()) {
            throw new UnsupportedOperationException("'addPrimaryKey' is not supported by synced Realms.");
        }
    }

    private long getColumnIndex(String fieldName) {
>>>>>>> YOURS
        long columnIndex = table.getColumnIndex(fieldName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(String.format(Locale.US, "Field name '%s' does not exist on schema for '%s'", fieldName, getClassName()));
        }
        return columnIndex;
    }

    static final class DynamicColumnIndices extends ColumnInfo {

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

    static final class FieldMetaData {

        final RealmFieldType realmType;

        final boolean defaultNullable;

        FieldMetaData(RealmFieldType realmType, boolean defaultNullable) {
            this.realmType = realmType;
            this.defaultNullable = defaultNullable;
        }
    }
}
