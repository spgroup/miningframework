package io.realm;

import java.util.Set;
import io.realm.annotations.Required;

public abstract class RealmObjectSchema {

    public abstract void close();

    public abstract String getClassName();

    public abstract RealmObjectSchema setClassName(String className);

    public abstract RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes);

    public abstract RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema);

    public abstract RealmObjectSchema removeField(String fieldName);

    public abstract RealmObjectSchema renameField(String currentFieldName, String newFieldName);

    public abstract boolean hasField(String fieldName);

    public abstract RealmObjectSchema addIndex(String fieldName);

    public abstract boolean hasIndex(String fieldName);

    public abstract RealmObjectSchema removeIndex(String fieldName);

    public abstract RealmObjectSchema addPrimaryKey(String fieldName);

    public abstract RealmObjectSchema removePrimaryKey();

    public abstract RealmObjectSchema setRequired(String fieldName, boolean required);

    public abstract RealmObjectSchema setNullable(String fieldName, boolean nullable);

    public abstract boolean isRequired(String fieldName);

    public abstract boolean isNullable(String fieldName);

    public abstract boolean isPrimaryKey(String fieldName);

    public abstract boolean hasPrimaryKey();

    public abstract String getPrimaryKey();

    public abstract Set<String> getFieldNames();

    public abstract RealmObjectSchema transform(Function function);

    public abstract RealmFieldType getFieldType(String fieldName);

    abstract long[] getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes);

    abstract RealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required);

    abstract RealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo);

    public interface Function {

        void apply(DynamicRealmObject obj);
    }

    protected static class FieldMetaData {

        protected final RealmFieldType realmType;

        protected final boolean defaultNullable;

        protected FieldMetaData(RealmFieldType realmType, boolean defaultNullable) {
            this.realmType = realmType;
            this.defaultNullable = defaultNullable;
        }
    }
}