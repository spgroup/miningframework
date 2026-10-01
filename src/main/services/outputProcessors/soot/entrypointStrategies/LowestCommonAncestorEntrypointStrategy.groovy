package services.outputProcessors.soot.entrypointStrategies

import soot.G
import soot.PackManager
import soot.Scene
import soot.SootMethod
import soot.jimple.toolkits.callgraph.Edge
import soot.options.Options
import util.JavaApiClassPath

/**
 * The analyses start from the lowest common ancestors, in the call graph of the build, of the methods modified
 * by left and by right: for each pair (left method, right method), the common callers (a method is its own
 * caller) with the smallest sum of call distances to both methods. The result is the union over the pairs.
 *
 * The call graph is built with CHA from every method of the build: starting only from main methods would hide
 * the modified methods whenever the build has an unrelated main (e.g. one of a bundled dependency).
 */
class LowestCommonAncestorEntrypointStrategy implements EntrypointStrategy {

    private final String javaApiClassPath

    LowestCommonAncestorEntrypointStrategy(String javaApiClassPath = JavaApiClassPath.get()) {
        this.javaApiClassPath = javaApiClassPath
    }

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        if (leftModifiedMethods.isEmpty() || rightModifiedMethods.isEmpty()) {
            return []
        }
        return lowestCommonAncestors(callersOf(buildJarPath), leftModifiedMethods, rightModifiedMethods)
    }

    static List<String> lowestCommonAncestors(Map<String, Set<String>> callers, Set<String> leftMethods, Set<String> rightMethods) {
        Map<String, Map<String, Integer>> distances = [:]
        Set<String> ancestors = new TreeSet<>()
        for (String left : leftMethods) {
            for (String right : rightMethods) {
                Map<String, Integer> fromLeft = distances.computeIfAbsent(left) { ancestorDistances(callers, it) }
                Map<String, Integer> fromRight = distances.computeIfAbsent(right) { ancestorDistances(callers, it) }
                Map<String, Integer> common = fromLeft.findAll { fromRight.containsKey(it.key) }
                        .collectEntries { [(it.key): it.value + fromRight[it.key]] }
                if (common) {
                    int closest = common.values().min()
                    ancestors.addAll(common.findAll { it.value == closest }.keySet())
                }
            }
        }
        return ancestors as List
    }

    /** Breadth-first search over the callers (so recursion is not a problem): distance of each ancestor. */
    private static Map<String, Integer> ancestorDistances(Map<String, Set<String>> callers, String method) {
        Map<String, Integer> distances = [(method): 0]
        Deque<String> queue = new ArrayDeque<>([method])
        while (!queue.isEmpty()) {
            String current = queue.poll()
            for (String caller : callers.getOrDefault(current, [] as Set)) {
                if (!distances.containsKey(caller)) {
                    distances[caller] = distances[current] + 1
                    queue.add(caller)
                }
            }
        }
        return distances
    }

    private Map<String, Set<String>> callersOf(String buildJarPath) {
        G.reset()
        Options options = Options.v()
        options.set_whole_program(true)
        options.set_allow_phantom_refs(true)
        options.set_process_dir([buildJarPath])
        options.set_soot_classpath([buildJarPath, javaApiClassPath].join(File.pathSeparator))
        options.set_output_format(Options.output_format_none)
        // the Java API classes only have signatures: calls to them end the call graph paths
        options.set_exclude(['java.*', 'javax.*', 'sun.*', 'com.sun.*', 'jdk.*', 'org.w3c.*', 'org.xml.*'])
        options.set_no_bodies_for_excluded(true)
        options.setPhaseOption('cg.cha', 'on')

        Scene.v().loadNecessaryClasses()
        Scene.v().setEntryPoints(Scene.v().applicationClasses.collectMany { it.methods.findAll { SootMethod m -> m.concrete } })
        PackManager.v().getPack('cg').apply()

        Map<String, Set<String>> callers = [:]
        for (Edge edge : Scene.v().callGraph) {
            if (edge.src() != null && edge.tgt() != null) {
                callers.computeIfAbsent(edge.tgt().signature) { new HashSet<String>() }.add(edge.src().signature)
            }
        }
        return callers
    }
}
