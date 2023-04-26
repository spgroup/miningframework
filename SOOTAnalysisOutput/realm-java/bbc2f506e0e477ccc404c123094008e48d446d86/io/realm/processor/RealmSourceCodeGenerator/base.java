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
import java.util.*;

public class RealmSourceCodeGenerator {

    private ProcessingEnvironment processingEnvironment;

    private String className;

    private String packageName;

    private List<VariableElement> fields;

    public RealmSourceCodeGenerator(ProcessingEnvironment processingEnvironment, String className, String packageName, List<VariableElement> fields) {
        this.processingEnvironment = processingEnvironment;
        this.className = className;
        this.packageName = packageName;
        this.fields = fields;
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
        CASTING_TYPES.put("byte[]", "byte[]");
    }

    public void generate() throws IOException, UnsupportedOperationException {
        String qualifiedGeneratedClassName = String.format("%s.%sRealmProxy", packageName, className);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        Elements elementUtils = processingEnvironment.getElementUtils();
        Types typeUtils = processingEnvironment.getTypeUtils();
        TypeMirror realmObject = elementUtils.getTypeElement("io.realm.RealmObject").asType();
        DeclaredType realmList = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));
        writer.setIndent("    ");
        writer.emitPackage(packageName).emitEmptyLine();
        writer.emitImports("io.realm.internal.ColumnType", "io.realm.internal.Table", "io.realm.internal.ImplicitTransaction", "io.realm.internal.Row", "io.realm.internal.LinkView", "io.realm.RealmList", "io.realm.RealmObject").emitEmptyLine();
        writer.beginType(qualifiedGeneratedClassName, "class", EnumSet.of(Modifier.PUBLIC), className).emitEmptyLine();
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
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement("return (%s) realmGetRow().get%s(%d)", fieldTypeCanonicalName, realmType, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("realmGetRow().set%s(%d, (%s) value)", realmType, columnNumber, castingType);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.beginControlFlow("if (realmGetRow().isNullLink(%d))", columnNumber);
                writer.emitStatement("return null");
                writer.endControlFlow();
                writer.emitStatement("return realm.get(%s.class, realmGetRow().getLink(%d))", fieldTypeCanonicalName, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("realmGetRow().nullifyLink(%d)", columnNumber);
                writer.endControlFlow();
                writer.emitStatement("realmGetRow().setLink(%d, value.realmGetRow().getIndex())", columnNumber);
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
                writer.emitStatement("return new RealmList(%s.class, realmGetRow().getLinkList(%d), realm)", genericType, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("LinkView links = realmGetRow().getLinkList(%d)", columnNumber);
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("return");
                writer.endControlFlow();
                writer.beginControlFlow("for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value)");
                writer.emitStatement("links.add(linkedObject.realmGetRow().getIndex())");
                writer.endControlFlow();
                writer.endMethod();
            } else {
                throw new UnsupportedOperationException(String.format("Type %s of field %s is not supported", fieldTypeCanonicalName, fieldName));
            }
            writer.emitEmptyLine();
        }
        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), "ImplicitTransaction", "transaction");
        writer.beginControlFlow("if(!transaction.hasTable(\"" + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s\")", this.className);
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
                writer.emitStatement("table.addColumn(%s, \"%s\")", JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName), fieldName.toLowerCase(Locale.getDefault()));
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!transaction.hasTable(\"%s\"))", fieldTypeName);
                writer.emitStatement("%sRealmProxy.initTable(transaction)", fieldTypeCanonicalName);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s\"))", fieldName.toLowerCase(Locale.getDefault()), fieldTypeName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }
                writer.beginControlFlow("if (!transaction.hasTable(\"%s\"))", genericType);
                writer.emitStatement("%sRealmProxy.initTable(transaction)", genericCanonicalType);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s\"))", fieldName.toLowerCase(Locale.getDefault()), genericType);
            }
        }
        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s\")", this.className);
        writer.endMethod();
        writer.emitEmptyLine();
        writer.emitAnnotation("Override");
        writer.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = [\")", className);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            writer.emitStatement("stringBuilder.append(get%s())", capitaliseFirstChar(fieldName));
            writer.emitStatement("stringBuilder.append(\"} \")", fieldName);
        }
        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod();
        writer.endType();
        writer.close();
    }

    private static String capitaliseFirstChar(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
