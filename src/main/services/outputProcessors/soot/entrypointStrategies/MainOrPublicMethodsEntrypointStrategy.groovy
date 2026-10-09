package services.outputProcessors.soot.entrypointStrategies

import soot.G
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.options.Options
import util.JavaApiClassPath

/**
 * The analyses start from the main methods of the build or, when there is none, from its public methods, as
 * conflict-static-analysis chooses the roots of its call graph (StatementsUtil.getCallgraphEntryPoints).
 */
class MainOrPublicMethodsEntrypointStrategy implements EntrypointStrategy {

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        configureSoot(buildJarPath)

        List<SootMethod> mainMethods = findMainMethods()
        if (mainMethods.isEmpty()) {
            mainMethods = findPublicMethods()
        }
        return mainMethods.collect { getSootMethodSignature(it) }.sort()
    }

    static String getSootMethodSignature(SootMethod method) {
        return "<${method.getDeclaringClass().getName()}: ${method.getSubSignature()}>"
    }

    private static void configureSoot(String buildJarPath) {
        G.reset()
        Options.v().set_allow_phantom_refs(true)
        Options.v().set_process_dir([buildJarPath])
        Options.v().set_soot_classpath([buildJarPath, JavaApiClassPath.get()].join(File.pathSeparator))
        Scene.v().loadNecessaryClasses()
    }

    private static List<SootMethod> findMainMethods() {
        List<SootMethod> mainMethods = new ArrayList<>()

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (isMainMethod(method)) {
                    mainMethods.add(method)
                }
            }
        }

        return mainMethods
    }

    private static boolean isMainMethod(SootMethod method) {

        return method.getName().equals("main") &&
                method.isStatic() &&
                method.getReturnType().toString().equals("void") &&
                method.getParameterCount() == 1 &&
                method.getParameterType(0).toString().equals("java.lang.String[]")
    }
    
    private static List<SootMethod> findPublicMethods() {
        List<SootMethod> publicMethods = new ArrayList<>()

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (method.isPublic() && method.isConcrete()) {
                    publicMethods.add(method)
                }
            }
        }

        return publicMethods
    }
}
