package services.outputProcessors.soot.methodResolvers

import soot.Body
import soot.G
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.Unit
import soot.options.Options
import util.JavaApiClassPath

/**
 * Resolves modified lines to the Soot signatures of the methods of the build that contain them, which
 * conflict-static-analysis takes as entrypoints (e.g. "<a.C: void process(a.Node,java.util.List)>").
 * It only reads the build, not the source.
 */
class BytecodeMethodResolver implements MethodResolver {

    /**
     * @return the Soot signatures of the methods with a statement in a modified line
     */
    Set<String> resolve(String buildJarPath, String sourceDirectory, Map<String, List<Integer>> definitions) {
        configureSoot(buildJarPath)

        Set<String> signatures = new TreeSet<>()
        for (SootClass aClass : listSootClasses()) {
            if (aClass.resolvingLevel() != SootClass.BODIES || aClass.isPhantomClass()) continue
            String className = retriveClassNameInDefinitions(aClass, definitions)
            if (className == null) continue
            for (SootMethod m : aClass.getMethods()) {
                Body body = retrieveActiveBodySafely(m)
                if (body == null) continue
                if (hasStatementInLines(body, definitions.get(className))) {
                    signatures.add(getSootMethodSignature(m))
                }
            }
        }
        return signatures
    }

    static String getSootMethodSignature(SootMethod method) {
        return "<${method.getDeclaringClass().getName()}: ${method.getSubSignature()}>"
    }
    /*
     * As conflict-static-analysis (SootWrapper), with the line numbers of the statements.
     */
    private static void configureSoot(String buildJarPath) {
        G.reset()
        Options.v().set_allow_phantom_refs(true)
        Options.v().set_process_dir([buildJarPath])
        Options.v().set_full_resolver(true)
        Options.v().set_keep_line_number(true)
        Options.v().set_soot_classpath([buildJarPath, JavaApiClassPath.get()].join(File.pathSeparator))
        Options.v().setPhaseOption("jb", "use-original-names:true")
        Scene.v().loadNecessaryClasses()
    }

    private static List<SootClass> listSootClasses() {
        List<SootClass> classes = new ArrayList<>()
        for (SootClass c : Scene.v().getApplicationClasses()) {
            Scene.v().loadClass(c.getName(), SootClass.BODIES)
            classes.add(c)
        }
        return classes
    }

    /*
     * It returns a class name in the definitions set or
     * return null. It also searches the outer classes of
     * aClass, from the innermost one: the outer class of an anonymous
     * or local class is the one that encloses it (e.g. Outer$Inner for
     * Outer$Inner$1), and the definitions have the class of the file (Outer).
     */
    private static String retriveClassNameInDefinitions(SootClass aClass, Map<String, List<Integer>> definitions) {
        SootClass currentClass = aClass
        while (currentClass != null) {
            if (definitions.containsKey(currentClass.getName())) {
                return currentClass.getName()
            }
            currentClass = retrieveOuterClass(currentClass)
        }
        return null
    }

    /*
     * Retrieves the outer class. Either using the
     * getOuterClass method (if the isInnerClass returns true),
     * or finding the name of the outer class via substring.
     */
    private static SootClass retrieveOuterClass(SootClass aClass) {
        if (aClass.isInnerClass()) {
            return aClass.getOuterClass()
        }
        if (aClass.getName().contains('$')) {
            int idx = aClass.getName().indexOf('$')
            String outer = aClass.getName().substring(0, idx)
            return Scene.v().getSootClass(outer) // note: this method getSootClass might throw a RuntimeException.
        }
        return null
    }

    /*
     * Retrieves the active body of an method, if any. There is
     * no simple way to check whether we can retrieve an active method
     * or not. So, in this implementation, we first try to retrieve
     * one. If an exception is thrown, we just return null.
     */
    private static Body retrieveActiveBodySafely(SootMethod method) {
        try {
            return method.retrieveActiveBody()
        } catch (RuntimeException e) {
            return null
        }
    }

    private static boolean hasStatementInLines(Body body, List<Integer> lines) {
        for (Unit u : body.getUnits()) {
            if (lines.contains(u.getJavaSourceStartLineNumber())) {
                return true
            }
        }
        return false
    }
}
