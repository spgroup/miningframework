package crules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import static com.sun.tools.javac.code.Flags.ENUM;
import static com.sun.tools.javac.code.Flags.FINAL;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Flags.SYNTHETIC;
import static com.sun.tools.javac.code.Kinds.Kind.*;

public class MutableFieldsAnalyzer extends AbstractCodingRulesAnalyzer {

    public MutableFieldsAnalyzer(JavacTask task) {
        super(task);
        treeVisitor = new MutableFieldsVisitor();
        eventKind = Kind.ANALYZE;
    }

    private boolean ignoreField(String className, String field) {
        Set<String> fieldsToIgnore = classFieldsToIgnoreMap.get(className);
        return (fieldsToIgnore) != null && fieldsToIgnore.contains(field);
    }

    class MutableFieldsVisitor extends TreeScanner {

        @Override
        public void visitVarDef(JCVariableDecl tree) {
            boolean isJavacPack = tree.sym.outermostClass().fullname.toString().contains(packageToCheck);
            if (isJavacPack && (tree.sym.flags() & SYNTHETIC) == 0 && tree.sym.owner.kind == TYP) {
                if (!ignoreField(tree.sym.owner.flatName().toString(), tree.getName().toString())) {
                    boolean enumClass = (tree.sym.owner.flags() & ENUM) != 0;
                    boolean nonFinalStaticEnumField = (tree.sym.flags() & (ENUM | FINAL)) == 0;
                    boolean nonFinalStaticField = (tree.sym.flags() & STATIC) != 0 && (tree.sym.flags() & FINAL) == 0;
                    if (enumClass ? nonFinalStaticEnumField : nonFinalStaticField) {
                        messages.error(tree, "crules.err.var.must.be.final", tree);
                    }
                }
            }
            super.visitVarDef(tree);
        }
    }

    private static final String packageToCheck = "com.sun.tools.javac";

    private static final Map<String, Set<String>> classFieldsToIgnoreMap = new HashMap<>();

    private static void ignoreFields(String className, String... fieldNames) {
        classFieldsToIgnoreMap.put(className, new HashSet<>(Arrays.asList(fieldNames)));
    }

    static {
        ignoreFields("com.sun.tools.javac.util.JCDiagnostic", "fragmentFormatter");
        ignoreFields("com.sun.tools.javac.util.JavacMessages", "defaultBundle", "defaultMessages");
        ignoreFields("com.sun.tools.javac.file.JRTIndex", "sharedInstance");
        ignoreFields("com.sun.tools.javac.main.JavaCompiler", "versionRB");
        ignoreFields("com.sun.tools.javac.code.Type", "moreInfo");
        ignoreFields("com.sun.tools.javac.util.SharedNameTable", "freelist");
        ignoreFields("com.sun.tools.javac.util.Log", "useRawMessages");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$ModuleFinder", "moduleFinderClass", "ofMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$Configuration", "configurationClass", "resolveRequiresAndUsesMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$Layer", "layerClass", "bootMethod", "defineModulesWithOneLoaderMethod", "configurationMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$Module", "addExportsMethod", "addUsesMethod", "getModuleMethod", "getUnnamedModuleMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$ModuleDescriptor$Version", "versionClass", "parseMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$ServiceLoaderHelper", "loadMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$VMHelper", "vmClass", "getRuntimeArgumentsMethod");
        ignoreFields("com.sun.tools.javac.util.JDK9Wrappers$JmodFile", "jmodFileClass", "checkMagicMethod");
    }
}
