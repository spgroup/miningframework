package io.realm.processor;

import com.squareup.javawriter.JavaWriter;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class RealmProxyClassGenerator {

    private ProcessingEnvironment processingEnvironment;

    private String className;

    private String packageName;

    private List<VariableElement> fields = new ArrayList<VariableElement>();

    private Map<String, String> getters = new HashMap<String, String>();

    private Map<String, String> setters = new HashMap<String, String>();

    private List<VariableElement> fieldsToIndex;

    private static final String REALM_PACKAGE_NAME = "io.realm";

    private static final String TABLE_PREFIX = "class_";

    private static final String PROXY_SUFFIX = "RealmProxy";

    private Elements elementUtils;

    private Types typeUtils;

    private TypeMirror realmObject;

    private DeclaredType realmList;

    public RealmProxyClassGenerator(ProcessingEnvironment processingEnvironment, String className, String packageName, List<VariableElement> fields, Map<String, String> getters, Map<String, String> setters, List<VariableElement> fieldsToIndex) {
        this.processingEnvironment = processingEnvironment;
        this.className = className;
        this.packageName = packageName;
        this.fields = fields;
        this.getters = getters;
        this.setters = setters;
        this.fieldsToIndex = fieldsToIndex;
    }

    private static final Map<String, String> JAVA_TO_REALM_TYPES;

    static {
        JAVA_TO_REALM_TYPES = new HashMap<String, String>();
        JAVA_TO_REALM_TYPES.put("byte", "Long");
        JAVA_TO_REALM_TYPES.put("short", "Long");
        JAVA_TO_REALM_TYPES.put("int", "Long");
        JAVA_TO_REALM_TYPES.put("long", "Long");
        JAVA_TO_REALM_TYPES.put("float", "Float");
        JAVA_TO_REALM_TYPES.put("double", "Double");
        JAVA_TO_REALM_TYPES.put("boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("Byte", "Long");
        JAVA_TO_REALM_TYPES.put("Short", "Long");
        JAVA_TO_REALM_TYPES.put("Integer", "Long");
        JAVA_TO_REALM_TYPES.put("Long", "Long");
        JAVA_TO_REALM_TYPES.put("Float", "Float");
        JAVA_TO_REALM_TYPES.put("Double", "Double");
        JAVA_TO_REALM_TYPES.put("Boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("java.lang.String", "String");
        JAVA_TO_REALM_TYPES.put("java.util.Date", "Date");
        JAVA_TO_REALM_TYPES.put("byte[]", "BinaryByteArray");
    }

    private static final Map<String, String> JAVA_TO_COLUMN_TYPES;

    static {
        JAVA_TO_COLUMN_TYPES = new HashMap<String, String>();
        JAVA_TO_COLUMN_TYPES.put("byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("int", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("Byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Integer", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("Double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("Boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.String", "ColumnType.STRING");
        JAVA_TO_COLUMN_TYPES.put("java.util.Date", "ColumnType.DATE");
        JAVA_TO_COLUMN_TYPES.put("byte[]", "ColumnType.BINARY");
    }

    private static final Map<String, String> CASTING_TYPES;

    static {
        CASTING_TYPES = new HashMap<String, String>();
        CASTING_TYPES.put("byte", "long");
        CASTING_TYPES.put("short", "long");
        CASTING_TYPES.put("int", "long");
        CASTING_TYPES.put("long", "long");
        CASTING_TYPES.put("float", "float");
        CASTING_TYPES.put("double", "double");
        CASTING_TYPES.put("boolean", "boolean");
        CASTING_TYPES.put("Byte", "long");
        CASTING_TYPES.put("Short", "long");
        CASTING_TYPES.put("Integer", "long");
        CASTING_TYPES.put("Long", "long");
        CASTING_TYPES.put("Float", "float");
        CASTING_TYPES.put("Double", "double");
        CASTING_TYPES.put("Boolean", "boolean");
        CASTING_TYPES.put("java.lang.String", "String");
        CASTING_TYPES.put("java.util.Date", "Date");
        CASTING_TYPES.put("byte[]", "byte[]");
    }

    public void generate() throws IOException, UnsupportedOperationException {
        String qualifiedGeneratedClassName = String.format("%s.%s%s", REALM_PACKAGE_NAME, className, PROXY_SUFFIX);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
<<<<<<< MINE
        elementUtils = processingEnvironment.getElementUtils();
=======
        Elements elementUtils = processingEnvironment.getElementUtils();
>>>>>>> YOURS
        typeUtils = processingEnvironment.getTypeUtils();
        realmObject = elementUtils.getTypeElement("io.realm.RealmObject").asType();
        realmList = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));
        writer.setIndent("    ");
        writer.emitPackage(REALM_PACKAGE_NAME).emitEmptyLine();
<<<<<<< MINE
        writer.emitImports("io.realm.internal.ColumnType", "io.realm.internal.Table", "io.realm.internal.ImplicitTransaction", "io.realm.internal.Row", "io.realm.internal.LinkView", "io.realm.internal.json.JsonUtils", "io.realm.RealmList", "io.realm.RealmObject", "org.json.JSONObject", "org.json.JSONException", "org.json.JSONArray", "android.util.JsonReader", "android.util.JsonToken", "java.io.IOException", "java.util.*", packageName + ".*").emitEmptyLine();
=======
        writer.emitImports("io.realm.internal.ColumnType", "io.realm.internal.Table", "io.realm.internal.ImplicitTransaction", "io.realm.internal.Row", "io.realm.internal.LinkView", "io.realm.RealmList", "io.realm.RealmObject", "io.realm.Realm", "java.util.*", packageName + ".*").emitEmptyLine();
>>>>>>> YOURS
        writer.beginType(qualifiedGeneratedClassName, "class", EnumSet.of(Modifier.PUBLIC), className).emitEmptyLine();
<<<<<<< MINE
        ListIterator<VariableElement> iterator = fields.listIterator();
        while (iterator.hasNext()) {
            int columnNumber = iterator.nextIndex();
            VariableElement field = iterator.next();
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                String realmType = JAVA_TO_REALM_TYPES.get(fieldTypeCanonicalName);
                String castingType = CASTING_TYPES.get(fieldTypeCanonicalName);
                writer.emitAnnotation("Override");
                String getterPrefix = fieldTypeCanonicalName.equals("boolean") ? "is" : "get";
                writer.beginMethod(fieldTypeCanonicalName, getterPrefix + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement("return (%s) row.get%s(Realm.columnIndices.get(\"%s\").get(\"%s\"))", fieldTypeCanonicalName, realmType, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("row.set%s(Realm.columnIndices.get(\"%s\").get(\"%s\"), (%s) value)", realmType, className, fieldName, castingType);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.beginControlFlow("if (row.isNullLink(Realm.columnIndices.get(\"%s\").get(\"%s\")))", className, fieldName);
                writer.emitStatement("return null");
                writer.endControlFlow();
                writer.emitStatement("return realm.get(%s.class, row.getLink(Realm.columnIndices.get(\"%s\").get(\"%s\")))", fieldTypeCanonicalName, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("row.nullifyLink(Realm.columnIndices.get(\"%s\").get(\"%s\"))", className, fieldName);
                writer.endControlFlow();
                writer.emitStatement("row.setLink(Realm.columnIndices.get(\"%s\").get(\"%s\"), value.row.getIndex())", className, fieldName);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement("return new RealmList(%s.class, row.getLinkList(Realm.columnIndices.get(\"%s\").get(\"%s\")), realm)", genericType, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("LinkView links = row.getLinkList(Realm.columnIndices.get(\"%s\").get(\"%s\"))", className, fieldName);
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("return");
                writer.endControlFlow();
                writer.beginControlFlow("for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value)");
                writer.emitStatement("links.add(linkedObject.row.getIndex())");
                writer.endControlFlow();
                writer.endMethod();
            } else {
                throw new UnsupportedOperationException(String.format("Type %s of field %s is not supported", fieldTypeCanonicalName, fieldName));
            }
            writer.emitEmptyLine();
        }
        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "ImplicitTransaction", "transaction");
        writer.beginControlFlow("if(!transaction.hasTable(\"" + TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeName;
            if (fieldTypeCanonicalName.contains(".")) {
                fieldTypeName = fieldTypeCanonicalName.substring(fieldTypeCanonicalName.lastIndexOf('.') + 1);
            } else {
                fieldTypeName = fieldTypeCanonicalName;
            }
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                writer.emitStatement("table.addColumn(%s, \"%s\")", JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName), fieldName);
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, fieldTypeName);
                writer.emitStatement("%s%s.initTable(transaction)", fieldTypeName, PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s%s\"))", fieldName, TABLE_PREFIX, fieldTypeName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, genericType);
                writer.emitStatement("%s%s.initTable(transaction)", genericType, PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s%s\"))", fieldName, TABLE_PREFIX, genericType);
            }
        }
        for (VariableElement field : fieldsToIndex) {
            String fieldName = field.getSimpleName().toString();
            writer.emitStatement("table.setIndex(table.getColumnIndex(\"%s\"))", fieldName);
        }
        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        writer.endMethod();
        writer.emitEmptyLine();
        writer.beginMethod("void", "validateTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "ImplicitTransaction", "transaction");
        writer.beginControlFlow("if(transaction.hasTable(\"" + TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        writer.beginControlFlow("if(table.getColumnCount() != " + fields.size() + ")");
        writer.emitStatement("throw new IllegalStateException(\"Column count does not match\")");
        writer.endControlFlow();
        writer.emitStatement("Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>()");
        writer.beginControlFlow("for(long i = 0; i < " + fields.size() + "; i++)");
        writer.emitStatement("columnTypes.put(table.getColumnName(i), table.getColumnType(i))");
        writer.endControlFlow();
        ListIterator<VariableElement> fieldsIterator = fields.listIterator();
        while (fieldsIterator.hasNext()) {
            int columnNumber = fieldsIterator.nextIndex();
            VariableElement field = fieldsIterator.next();
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeName;
            if (fieldTypeCanonicalName.contains(".")) {
                fieldTypeName = fieldTypeCanonicalName.substring(fieldTypeCanonicalName.lastIndexOf('.') + 1);
            } else {
                fieldTypeName = fieldTypeCanonicalName;
            }
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != %s)", fieldName, JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName));
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", fieldTypeName, fieldName);
                writer.endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != ColumnType.LINK)", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", fieldTypeName, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, fieldTypeName);
                writer.emitStatement("throw new IllegalStateException(\"Missing table '%s%s' for column '%s'\")", TABLE_PREFIX, fieldTypeName, fieldName);
                writer.endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }
                writer.beginControlFlow("if(!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if(columnTypes.get(\"%s\") != ColumnType.LINK_LIST)", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", genericType, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, genericType);
                writer.emitStatement("throw new IllegalStateException(\"Missing table '%s%s' for column '%s'\")", TABLE_PREFIX, genericType, fieldName);
                writer.endControlFlow();
            }
        }
        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
        writer.beginMethod("List<String>", "getFieldNames", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        List<String> entries = new ArrayList<String>();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            entries.add(String.format("\"%s\"", fieldName));
        }
        String statementSection = joinStringList(entries, ", ");
        writer.emitStatement("return Arrays.asList(%s)", statementSection);
        writer.endMethod();
        writer.emitEmptyLine();
        writer.emitAnnotation("Override");
        writer.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = [\")", className);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String getterPrefix = fieldTypeCanonicalName.equals("boolean") ? "is" : "get";
            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            writer.emitStatement("stringBuilder.append(%s%s())", getterPrefix, capitaliseFirstChar(fieldName));
            writer.emitStatement("stringBuilder.append(\"} \")", fieldName);
        }
        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod();
        writer.emitEmptyLine();
        writer.emitAnnotation("Override");
        writer.beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("int result = 17");
        int counter = 0;
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (HASHCODE.containsKey(fieldTypeCanonicalName)) {
                for (String statement : HASHCODE.get(fieldTypeCanonicalName)) {
                    if (statement.contains("%d") && statement.contains("%s")) {
                        writer.emitStatement(statement, counter, capitaliseFirstChar(fieldName));
                    } else if (statement.contains("%d")) {
                        writer.emitStatement(statement, counter, counter);
                    } else if (statement.contains("%s")) {
                        writer.emitStatement(statement, capitaliseFirstChar(fieldName));
                    } else {
                        throw new AssertionError();
                    }
                }
            } else {
                writer.emitStatement("%s temp_%d = get%s()", fieldTypeCanonicalName, counter, capitaliseFirstChar(fieldName));
                writer.emitStatement("result = 31 * result + (temp_%d != null ? temp_%d.hashCode() : 0)", counter, counter);
            }
            counter++;
        }
        writer.emitStatement("return result");
        writer.endMethod();
        writer.emitEmptyLine();
        String proxyClassName = className + PROXY_SUFFIX;
        writer.emitAnnotation("Override");
        writer.beginMethod("boolean", "equals", EnumSet.of(Modifier.PUBLIC), "Object", "o");
        writer.emitStatement("if (this == o) return true");
        writer.emitStatement("if (o == null || getClass() != o.getClass()) return false");
        writer.emitStatement("%s a%s = (%s)o", proxyClassName, className, proxyClassName);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String capFieldName = capitaliseFirstChar(fieldName);
            String fieldTypeCanonicalName = field.asType().toString();
            if (HOW_TO_EQUAL.containsKey(fieldTypeCanonicalName)) {
                switch(HOW_TO_EQUAL.get(fieldTypeCanonicalName)) {
                    case EQUALS_DIRECT:
                        String getterPrefix = fieldTypeCanonicalName.equals("boolean") ? "is" : "get";
                        writer.emitStatement("if (%s%s() != a%s.%s%s()) return false", getterPrefix, capFieldName, className, getterPrefix, capFieldName);
                        break;
                    case EQUALS_NULL:
                        writer.emitStatement("if (get%s() != null ? !get%s().equals(a%s.get%s()) : a%s.get%s() != null) return false", capFieldName, capFieldName, className, capFieldName, className, capFieldName);
                        break;
                    case EQUALS_ARRAY:
                        writer.emitStatement("if (!Arrays.equals(get%s(), a%s.get%s())) return false", capFieldName, className, capFieldName);
                        break;
                    case EQUALS_COMPARE:
                        writer.emitStatement("if (%s.compare(get%s(), a%s.get%s()) != 0) return false", capitaliseFirstChar(fieldTypeCanonicalName), capitaliseFirstChar(fieldName), className, capitaliseFirstChar(fieldName));
                        break;
                }
            } else if (typeUtils.isAssignable(field.asType(), realmObject) || typeUtils.isAssignable(field.asType(), realmList)) {
                writer.emitStatement("if (get%s() != null ? !get%s().equals(a%s.get%s()) : a%s.get%s() != null) return false", capFieldName, capFieldName, className, capFieldName, className, capFieldName);
            }
        }
        writer.emitStatement("return true");
        writer.endMethod();
        writer.emitEmptyLine();
        emitPopulateUsingJsonObjectMethod(writer);
        emitPopulateUsingJsonStreamMethod(writer);
=======
        emitAccessors(writer);
        emitInitTableMethod(writer);
        emitValidateTableMethod(writer);
        emitGetFieldNamesMethod(writer);
        emitToStringMethod(writer);
        emitHashcodeMethod(writer);
        emitEqualsMethod(writer);
>>>>>>> YOURS
        writer.endType();
        writer.close();
    }

    private void emitPopulateUsingJsonObjectMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "populateUsingJsonObject", EnumSet.of(Modifier.PROTECTED), Arrays.asList("JSONObject", "json"), Arrays.asList("JSONException"));
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                RealmJsonTypeHelper.emitFillRealmObjectWithJsonValue(fieldName, fieldTypeCanonicalName, writer);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                RealmJsonTypeHelper.emitFillRealmListWithJsonValue(fieldName, ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(), writer);
            } else {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(fieldName, fieldTypeCanonicalName, writer);
            }
        }
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitPopulateUsingJsonStreamMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "populateUsingJsonStream", EnumSet.of(Modifier.PROTECTED), Arrays.asList("JsonReader", "reader"), Arrays.asList("IOException"));
        writer.emitStatement("reader.beginObject()");
        writer.beginControlFlow("while (reader.hasNext())");
        writer.emitStatement("String name = reader.nextName()");
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (i == 0) {
                writer.beginControlFlow("if (name.equals(\"%s\") && reader.peek() != JsonToken.NULL)", fieldName);
            } else {
                writer.nextControlFlow("else if (name.equals(\"%s\")  && reader.peek() != JsonToken.NULL)", fieldName);
            }
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                RealmJsonTypeHelper.emitFillRealmObjectFromStream(fieldName, fieldTypeCanonicalName, writer);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                RealmJsonTypeHelper.emitFillRealmListFromStream(fieldName, ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(), writer);
            } else {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(fieldName, fieldTypeCanonicalName, writer);
            }
        }
        writer.nextControlFlow("else");
        writer.emitStatement("reader.skipValue()");
        writer.endControlFlow();
        writer.endControlFlow();
        writer.emitStatement("reader.endObject()");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitAccessors(JavaWriter writer) throws IOException {
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                String realmType = JAVA_TO_REALM_TYPES.get(fieldTypeCanonicalName);
                String castingType = CASTING_TYPES.get(fieldTypeCanonicalName);
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, getters.get(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement("realm.checkIfValid()");
                writer.emitStatement("return (%s) row.get%s(Realm.columnIndices.get(\"%s\").get(\"%s\"))", fieldTypeCanonicalName, realmType, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", setters.get(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("realm.checkIfValid()");
                writer.emitStatement("row.set%s(Realm.columnIndices.get(\"%s\").get(\"%s\"), (%s) value)", realmType, className, fieldName, castingType);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, getters.get(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.beginControlFlow("if (row.isNullLink(Realm.columnIndices.get(\"%s\").get(\"%s\")))", className, fieldName);
                writer.emitStatement("return null");
                writer.endControlFlow();
                writer.emitStatement("return realm.get(%s.class, row.getLink(Realm.columnIndices.get(\"%s\").get(\"%s\")))", fieldTypeCanonicalName, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", setters.get(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("row.nullifyLink(Realm.columnIndices.get(\"%s\").get(\"%s\"))", className, fieldName);
                writer.endControlFlow();
                writer.emitStatement("row.setLink(Realm.columnIndices.get(\"%s\").get(\"%s\"), value.row.getIndex())", className, fieldName);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = getGenericType(field);
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, getters.get(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement("return new RealmList<%s>(%s.class, row.getLinkList(Realm.columnIndices.get(\"%s\").get(\"%s\")), realm)", genericType, genericType, className, fieldName);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", setters.get(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("LinkView links = row.getLinkList(Realm.columnIndices.get(\"%s\").get(\"%s\"))", className, fieldName);
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("return");
                writer.endControlFlow();
                writer.beginControlFlow("for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value)");
                writer.emitStatement("links.add(linkedObject.row.getIndex())");
                writer.endControlFlow();
                writer.endMethod();
            } else {
                throw new UnsupportedOperationException(String.format("Type %s of field %s is not supported", fieldTypeCanonicalName, fieldName));
            }
            writer.emitEmptyLine();
        }
    }

    private void emitInitTableMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "ImplicitTransaction", "transaction");
        writer.beginControlFlow("if(!transaction.hasTable(\"" + TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeSimpleName = getFieldTypeSimpleName(field);
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                writer.emitStatement("table.addColumn(%s, \"%s\")", JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName), fieldName);
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, fieldTypeSimpleName);
                writer.emitStatement("%s%s.initTable(transaction)", fieldTypeSimpleName, PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s%s\"))", fieldName, TABLE_PREFIX, fieldTypeSimpleName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = getGenericType(field);
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, genericType);
                writer.emitStatement("%s%s.initTable(transaction)", genericType, PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s%s\"))", fieldName, TABLE_PREFIX, genericType);
            }
        }
        for (VariableElement field : fieldsToIndex) {
            String fieldName = field.getSimpleName().toString();
            writer.emitStatement("table.setIndex(table.getColumnIndex(\"%s\"))", fieldName);
        }
        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitValidateTableMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("void", "validateTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "ImplicitTransaction", "transaction");
        writer.beginControlFlow("if(transaction.hasTable(\"" + TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", TABLE_PREFIX, this.className);
        writer.beginControlFlow("if(table.getColumnCount() != " + fields.size() + ")");
        writer.emitStatement("throw new IllegalStateException(\"Column count does not match\")");
        writer.endControlFlow();
        writer.emitStatement("Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>()");
        writer.beginControlFlow("for(long i = 0; i < " + fields.size() + "; i++)");
        writer.emitStatement("columnTypes.put(table.getColumnName(i), table.getColumnType(i))");
        writer.endControlFlow();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeSimpleName = getFieldTypeSimpleName(field);
            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != %s)", fieldName, JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName));
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", fieldTypeSimpleName, fieldName);
                writer.endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != ColumnType.LINK)", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", fieldTypeSimpleName, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, fieldTypeSimpleName);
                writer.emitStatement("throw new IllegalStateException(\"Missing table '%s%s' for column '%s'\")", TABLE_PREFIX, fieldTypeSimpleName, fieldName);
                writer.endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = getGenericType(field);
                writer.beginControlFlow("if(!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Missing column '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if(columnTypes.get(\"%s\") != ColumnType.LINK_LIST)", fieldName);
                writer.emitStatement("throw new IllegalStateException(\"Invalid type '%s' for column '%s'\")", genericType, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", TABLE_PREFIX, genericType);
                writer.emitStatement("throw new IllegalStateException(\"Missing table '%s%s' for column '%s'\")", TABLE_PREFIX, genericType, fieldName);
                writer.endControlFlow();
            }
        }
        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetFieldNamesMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("List<String>", "getFieldNames", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        List<String> entries = new ArrayList<String>();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            entries.add(String.format("\"%s\"", fieldName));
        }
        String statementSection = joinStringList(entries, ", ");
        writer.emitStatement("return Arrays.asList(%s)", statementSection);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitToStringMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = [\")", className);
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            String fieldName = field.getSimpleName().toString();
            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                String fieldTypeSimpleName = getFieldTypeSimpleName(field);
                writer.emitStatement("stringBuilder.append(%s() != null ? \"%s\" : \"null\")", getters.get(fieldName), fieldTypeSimpleName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = getGenericType(field);
                writer.emitStatement("stringBuilder.append(\"RealmList<%s>[\").append(%s().size()).append(\"]\")", genericType, getters.get(fieldName));
            } else {
                writer.emitStatement("stringBuilder.append(%s())", getters.get(fieldName));
            }
            writer.emitStatement("stringBuilder.append(\"}\")");
            if (i < fields.size() - 1) {
                writer.emitStatement("stringBuilder.append(\",\")");
            }
        }
        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitHashcodeMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("String realmName = realm.getPath()");
        writer.emitStatement("String tableName = row.getTable().getName()");
        writer.emitStatement("long rowIndex = row.getIndex()");
        writer.emitEmptyLine();
        writer.emitStatement("int result = 17");
        writer.emitStatement("result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0)");
        writer.emitStatement("result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0)");
        writer.emitStatement("result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32))");
        writer.emitStatement("return result");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitEqualsMethod(JavaWriter writer) throws IOException {
        String proxyClassName = className + PROXY_SUFFIX;
        writer.emitAnnotation("Override");
        writer.beginMethod("boolean", "equals", EnumSet.of(Modifier.PUBLIC), "Object", "o");
        writer.emitStatement("if (this == o) return true");
        writer.emitStatement("if (o == null || getClass() != o.getClass()) return false");
        writer.emitStatement("%s a%s = (%s)o", proxyClassName, className, proxyClassName);
        writer.emitEmptyLine();
        writer.emitStatement("String path = realm.getPath()");
        writer.emitStatement("String otherPath = a%s.realm.getPath()", className);
        writer.emitStatement("if (path != null ? !path.equals(otherPath) : otherPath != null) return false;");
        writer.emitEmptyLine();
        writer.emitStatement("String tableName = row.getTable().getName()");
        writer.emitStatement("String otherTableName = a%s.row.getTable().getName()", className);
        writer.emitStatement("if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false");
        writer.emitEmptyLine();
        writer.emitStatement("if (row.getIndex() != a%s.row.getIndex()) return false", className);
        writer.emitEmptyLine();
        writer.emitStatement("return true");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    public static String joinStringList(List<String> strings, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            String item = iterator.next();
            if (index > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }

    private String getFieldTypeSimpleName(VariableElement field) {
        String fieldTypeCanonicalName = field.asType().toString();
        String fieldTypeName;
        if (fieldTypeCanonicalName.contains(".")) {
            fieldTypeName = fieldTypeCanonicalName.substring(fieldTypeCanonicalName.lastIndexOf('.') + 1);
        } else {
            fieldTypeName = fieldTypeCanonicalName;
        }
        return fieldTypeName;
    }

    private String getGenericType(VariableElement field) {
        String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
        String genericType;
        if (genericCanonicalType.contains(".")) {
            genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
        } else {
            genericType = genericCanonicalType;
        }
        return genericType;
    }
}