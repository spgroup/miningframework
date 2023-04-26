import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Descriptor;
import com.sun.tools.classfile.Descriptor.InvalidDescriptor;
import com.sun.tools.classfile.Field;
import static javax.tools.JavaFileObject.Kind.CLASS;
import static com.sun.tools.classfile.AccessFlags.ACC_ENUM;
import static com.sun.tools.classfile.AccessFlags.ACC_FINAL;
import static com.sun.tools.classfile.AccessFlags.ACC_STATIC;

public class DetectMutableStaticFields {

    private final String[] modules = { "java.compiler", "jdk.compiler", "jdk.javadoc", "jdk.jdeps" };

    private final String[] packagesToSeekFor = new String[] { "javax.tools", "javax.lang.model", "com.sun.javadoc", "com.sun.source", "com.sun.tools.classfile", "com.sun.tools.doclets", "com.sun.tools.javac", "com.sun.tools.javadoc", "com.sun.tools.javah", "com.sun.tools.javap", "jdk.javadoc" };

    private static final Map<String, List<String>> classFieldsToIgnoreMap = new HashMap<>();

    private static void ignore(String className, String... fields) {
        classFieldsToIgnoreMap.put(className, Arrays.asList(fields));
    }

    static {
        ignore("javax/tools/ToolProvider", "instance");
        ignore("com/sun/tools/javah/JavahTask", "versionRB");
        ignore("com/sun/tools/classfile/Dependencies$DefaultFilter", "instance");
        ignore("com/sun/tools/javap/JavapTask", "versionRB");
        ignore("com/sun/tools/doclets/formats/html/HtmlDoclet", "docletToStart");
        ignore("com/sun/tools/javac/util/JCDiagnostic", "fragmentFormatter");
        ignore("com/sun/tools/javac/util/JavacMessages", "defaultBundle", "defaultMessages");
        ignore("com/sun/tools/javac/file/JRTIndex", "sharedInstance");
        ignore("com/sun/tools/javac/main/JavaCompiler", "versionRB");
        ignore("com/sun/tools/javac/code/Type", "moreInfo");
        ignore("com/sun/tools/javac/util/SharedNameTable", "freelist");
        ignore("com/sun/tools/javac/util/Log", "useRawMessages");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$Configuration", "resolveRequiresAndUsesMethod", "configurationClass");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$Layer", "bootMethod", "defineModulesWithOneLoaderMethod", "configurationMethod", "layerClass");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$Module", "addExportsMethod", "addUsesMethod", "getModuleMethod", "getUnnamedModuleMethod");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$ModuleDescriptor$Version", "versionClass", "parseMethod");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$ModuleFinder", "moduleFinderClass", "ofMethod");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$ServiceLoaderHelper", "loadMethod");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$VMHelper", "vmClass", "getRuntimeArgumentsMethod");
        ignore("com/sun/tools/javac/util/JDK9Wrappers$JmodFile", "jmodFileClass", "checkMagicMethod");
    }

    private final List<String> errors = new ArrayList<>();

    public static void main(String[] args) {
        try {
            new DetectMutableStaticFields().run();
        } catch (Exception ex) {
            throw new AssertionError("Exception during test execution: " + ex, ex);
        }
    }

    private void run() throws IOException, ConstantPoolException, InvalidDescriptor, URISyntaxException {
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null)) {
            for (String module : modules) {
                analyzeModule(fm, module);
            }
        }
        if (errors.size() > 0) {
            for (String error : errors) {
                System.err.println(error);
            }
            throw new AssertionError("There are mutable fields, " + "please check output");
        }
    }

    boolean shouldAnalyzePackage(String packageName) {
        for (String aPackage : packagesToSeekFor) {
            if (packageName.contains(aPackage)) {
                return true;
            }
        }
        return false;
    }

    void analyzeModule(StandardJavaFileManager fm, String moduleName) throws IOException, ConstantPoolException, InvalidDescriptor {
        JavaFileManager.Location location = fm.getLocationForModule(StandardLocation.SYSTEM_MODULES, moduleName);
        if (location == null)
            throw new AssertionError("can't find module " + moduleName);
        for (JavaFileObject file : fm.list(location, "", EnumSet.of(CLASS), true)) {
            String className = fm.inferBinaryName(location, file);
            int index = className.lastIndexOf('.');
            String pckName = index == -1 ? "" : className.substring(0, index);
            if (shouldAnalyzePackage(pckName)) {
                analyzeClassFile(ClassFile.read(file.openInputStream()));
            }
        }
    }

    List<String> currentFieldsToIgnore;

    boolean ignoreField(String field) {
        if (currentFieldsToIgnore != null) {
            for (String fieldToIgnore : currentFieldsToIgnore) {
                if (field.equals(fieldToIgnore)) {
                    return true;
                }
            }
        }
        return false;
    }

    void analyzeClassFile(ClassFile classFileToCheck) throws IOException, ConstantPoolException, Descriptor.InvalidDescriptor {
        boolean enumClass = (classFileToCheck.access_flags.flags & ACC_ENUM) != 0;
        boolean nonFinalStaticEnumField;
        boolean nonFinalStaticField;
        currentFieldsToIgnore = classFieldsToIgnoreMap.get(classFileToCheck.getName());
        for (Field field : classFileToCheck.fields) {
            if (ignoreField(field.getName(classFileToCheck.constant_pool))) {
                continue;
            }
            nonFinalStaticEnumField = (field.access_flags.flags & (ACC_ENUM | ACC_FINAL)) == 0;
            nonFinalStaticField = (field.access_flags.flags & ACC_STATIC) != 0 && (field.access_flags.flags & ACC_FINAL) == 0;
            if (enumClass ? nonFinalStaticEnumField : nonFinalStaticField) {
                errors.add("There is a mutable field named " + field.getName(classFileToCheck.constant_pool) + ", at class " + classFileToCheck.getName());
            }
        }
    }
}
