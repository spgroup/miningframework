package services.outputProcessors.soot.entrypointStrategies

import soot.G
import soot.PackManager
import soot.Scene
import soot.SootClass
import soot.SootMethod
import soot.jimple.toolkits.callgraph.CallGraph
import soot.jimple.toolkits.callgraph.Edge
import soot.options.Options
import util.JavaApiClassPath

/**
 * Lowest common ancestor (LCA) strategy: the analyses start from the closest common callers of the methods modified
 * by left and by right.
 *
 * Steps:
 *   1. build the call graph of the build and keep, for each method, the methods that call it;
 *   2. for each modified method, find its ancestors: the methods that call it, directly or not, and how many calls
 *      away each one is;
 *   3. for each pair (method modified by left, method modified by right), keep the common ancestors with the smallest
 *      sum of distances to both methods;
 *   4. the entrypoints are the ancestors kept for all the pairs.
 *
 * A method is never its own ancestor, even when left and right modified the same method.
 */
class LowestCommonAncestorEntrypointStrategy implements EntrypointStrategy {

    private final String javaApiClassPath

    LowestCommonAncestorEntrypointStrategy(String javaApiClassPath = JavaApiClassPath.get()) {
        this.javaApiClassPath = javaApiClassPath
    }

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        boolean aSideHasNoModifiedMethod = leftModifiedMethods.isEmpty() || rightModifiedMethods.isEmpty()
        if (aSideHasNoModifiedMethod) {
            return new ArrayList<String>()
        }

        Map<String, Set<String>> callersOfEachMethod = buildCallersOfEachMethod(buildJarPath)
        List<String> lowestCommonAncestors = findLowestCommonAncestors(callersOfEachMethod, leftModifiedMethods, rightModifiedMethods)

        // To see the graph of a scenario (see exportGraphToDot), uncomment:
        // exportGraphToDot(callersOfEachMethod, leftModifiedMethods, rightModifiedMethods, lowestCommonAncestors, new File("lowest-common-ancestor.dot"))

        return lowestCommonAncestors
    }

    // Step 1: the call graph

    /**
     * Builds the call graph of the build and returns, for each method, the methods that call it directly.
     */
    private Map<String, Set<String>> buildCallersOfEachMethod(String buildJarPath) {
        configureSoot(buildJarPath)
        Scene.v().loadNecessaryClasses()
        Scene.v().setEntryPoints(getMethodsWithBodyOfTheBuild())
        PackManager.v().getPack("cg").apply()

        return getCallersOfEachMethod(Scene.v().getCallGraph())
    }

    /**
     * The call graph is built with CHA (Class Hierarchy Analysis): a call to an overridable method goes to every
     * implementation of it in the class hierarchy. The classes of the Java API only have signatures, so a call to
     * them ends a path of the call graph.
     */
    private void configureSoot(String buildJarPath) {
        G.reset()
        Options options = Options.v()
        options.set_whole_program(true)
        options.set_allow_phantom_refs(true)
        options.set_process_dir([buildJarPath])
        options.set_soot_classpath(buildJarPath + File.pathSeparator + javaApiClassPath)
        options.set_output_format(Options.output_format_none)
        options.set_exclude(["java.*", "javax.*", "sun.*", "com.sun.*", "jdk.*", "org.w3c.*", "org.xml.*"])
        options.set_no_bodies_for_excluded(true)
        options.setPhaseOption("cg.cha", "on")
    }

    /**
     * The call graph starts from every method of the build that has a body. Starting only from the main methods
     * would hide the modified methods whenever the main method is not related to them (e.g. one of a dependency
     * packed in the build).
     */
    private static List<SootMethod> getMethodsWithBodyOfTheBuild() {
        List<SootMethod> methodsWithBody = new ArrayList<>()
        for (SootClass buildClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : buildClass.getMethods()) {
                if (method.isConcrete()) {
                    methodsWithBody.add(method)
                }
            }
        }
        return methodsWithBody
    }

    /**
     * Each edge of the call graph says that a method (the caller) calls another one (the callee). This inverts it:
     * for each callee, the methods that call it.
     */
    private static Map<String, Set<String>> getCallersOfEachMethod(CallGraph callGraph) {
        Map<String, Set<String>> callersOfEachMethod = new HashMap<>()
        for (Edge edge : callGraph) {
            boolean edgeHasBothMethods = edge.src() != null && edge.tgt() != null
            if (edgeHasBothMethods) {
                String caller = getSootMethodSignature(edge.src())
                String callee = getSootMethodSignature(edge.tgt())
                addCaller(callersOfEachMethod, callee, caller)
            }
        }
        return callersOfEachMethod
    }

    private static void addCaller(Map<String, Set<String>> callersOfEachMethod, String callee, String caller) {
        if (!callersOfEachMethod.containsKey(callee)) {
            callersOfEachMethod.put(callee, new HashSet<String>())
        }
        callersOfEachMethod.get(callee).add(caller)
    }

    // Step 2: the ancestors of each modified method

    /**
     * The ancestors of each modified method, found only once for a method modified by both left and right.
     */
    private static Map<String, Map<String, Integer>> findAncestorsOfEachModifiedMethod(Map<String, Set<String>> callersOfEachMethod, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        Map<String, Map<String, Integer>> ancestorsOfEachModifiedMethod = new HashMap<>()
        for (String leftMethod : leftModifiedMethods) {
            ancestorsOfEachModifiedMethod.put(leftMethod, findAncestors(callersOfEachMethod, leftMethod))
        }
        for (String rightMethod : rightModifiedMethods) {
            if (!ancestorsOfEachModifiedMethod.containsKey(rightMethod)) {
                ancestorsOfEachModifiedMethod.put(rightMethod, findAncestors(callersOfEachMethod, rightMethod))
            }
        }
        return ancestorsOfEachModifiedMethod
    }

    /**
     * The ancestors of a method: every method that calls it, directly or not, with how many calls away it is
     * (1 = calls it directly, 2 = calls one of its callers, ...).
     *
     * It visits the callers level by level (breadth-first search): the first time an ancestor is found is through
     * its shortest path, and each method is visited only once, so recursive calls do not loop forever. The method
     * itself is not one of its ancestors, even when it calls itself.
     */
    private static Map<String, Integer> findAncestors(Map<String, Set<String>> callersOfEachMethod, String method) {
        Map<String, Integer> distanceOfEachVisitedMethod = new HashMap<>()
        Deque<String> methodsToVisit = new ArrayDeque<>()

        distanceOfEachVisitedMethod.put(method, 0)
        methodsToVisit.addLast(method)

        while (!methodsToVisit.isEmpty()) {
            String currentMethod = methodsToVisit.removeFirst()
            int callerDistance = distanceOfEachVisitedMethod.get(currentMethod) + 1

            for (String caller : getCallers(callersOfEachMethod, currentMethod)) {
                boolean alreadyVisited = distanceOfEachVisitedMethod.containsKey(caller)
                if (!alreadyVisited) {
                    distanceOfEachVisitedMethod.put(caller, callerDistance)
                    methodsToVisit.addLast(caller)
                }
            }
        }

        distanceOfEachVisitedMethod.remove(method)
        return distanceOfEachVisitedMethod
    }

    private static Set<String> getCallers(Map<String, Set<String>> callersOfEachMethod, String method) {
        Set<String> callers = callersOfEachMethod.get(method)
        if (callers == null) {
            return new HashSet<String>() // no method calls it
        }
        return callers
    }

    // Steps 3 and 4: the lowest common ancestors of the pairs

    /**
     * For each pair (method modified by left, method modified by right), its closest common ancestors. The result
     * is all of them, sorted and without repetitions.
     */
    static List<String> findLowestCommonAncestors(Map<String, Set<String>> callersOfEachMethod, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        Map<String, Map<String, Integer>> ancestorsOfEachModifiedMethod = findAncestorsOfEachModifiedMethod(callersOfEachMethod, leftModifiedMethods, rightModifiedMethods)

        Set<String> lowestCommonAncestors = new TreeSet<>()
        for (String leftMethod : leftModifiedMethods) {
            for (String rightMethod : rightModifiedMethods) {
                Map<String, Integer> leftMethodAncestors = ancestorsOfEachModifiedMethod.get(leftMethod)
                Map<String, Integer> rightMethodAncestors = ancestorsOfEachModifiedMethod.get(rightMethod)
                lowestCommonAncestors.addAll(findClosestCommonAncestors(leftMethodAncestors, rightMethodAncestors))
            }
        }
        return new ArrayList<String>(lowestCommonAncestors)
    }

    /**
     * The common ancestors of a pair with the smallest sum of distances to both methods: more than one when there is
     * a tie, none when the pair has no common ancestor.
     */
    private static List<String> findClosestCommonAncestors(Map<String, Integer> leftMethodAncestors, Map<String, Integer> rightMethodAncestors) {
        Map<String, Integer> distanceOfEachCommonAncestor = findCommonAncestors(leftMethodAncestors, rightMethodAncestors)

        List<String> closestCommonAncestors = new ArrayList<>()
        if (distanceOfEachCommonAncestor.isEmpty()) {
            return closestCommonAncestors
        }

        int smallestDistance = findSmallestDistance(distanceOfEachCommonAncestor)
        for (String commonAncestor : distanceOfEachCommonAncestor.keySet()) {
            if (distanceOfEachCommonAncestor.get(commonAncestor) == smallestDistance) {
                closestCommonAncestors.add(commonAncestor)
            }
        }
        return closestCommonAncestors
    }

    /**
     * The ancestors of both methods, with the sum of their distances to the two methods.
     */
    private static Map<String, Integer> findCommonAncestors(Map<String, Integer> leftMethodAncestors, Map<String, Integer> rightMethodAncestors) {
        Map<String, Integer> distanceOfEachCommonAncestor = new HashMap<>()
        for (String ancestor : leftMethodAncestors.keySet()) {
            boolean isAlsoAncestorOfRightMethod = rightMethodAncestors.containsKey(ancestor)
            if (isAlsoAncestorOfRightMethod) {
                int distanceToBothMethods = leftMethodAncestors.get(ancestor) + rightMethodAncestors.get(ancestor)
                distanceOfEachCommonAncestor.put(ancestor, distanceToBothMethods)
            }
        }
        return distanceOfEachCommonAncestor
    }

    private static int findSmallestDistance(Map<String, Integer> distanceOfEachAncestor) {
        int smallestDistance = Integer.MAX_VALUE
        for (int distance : distanceOfEachAncestor.values()) {
            if (distance < smallestDistance) {
                smallestDistance = distance
            }
        }
        return smallestDistance
    }

    // Debugging: the graph in the DOT format of Graphviz

    /**
     * Writes the part of the call graph the strategy looks at: the modified methods and their ancestors, with an
     * arrow from each caller to the method it calls. Colors: entrypoints found in yellow, methods modified by left in
     * green, by right in magenta, by both in blue, the other ancestors in black.
     * Not used by the strategy: call it to debug a scenario. To see it as an image: dot -Tpng graph.dot -o graph.png
     * The graph of a big build can have thousands of ancestors.
     */
    static void exportGraphToDot(Map<String, Set<String>> callersOfEachMethod, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods, List<String> entrypoints, File dotFile) {
        Set<String> methodsToShow = findMethodsToShow(callersOfEachMethod, leftModifiedMethods, rightModifiedMethods)

        StringBuilder dot = new StringBuilder()
        dot.append("digraph LowestCommonAncestor {\n")
        for (String method : methodsToShow) {
            String color = getColor(method, leftModifiedMethods, rightModifiedMethods, entrypoints)
            dot.append("  \"" + method + "\" [label=\"" + getShortName(method) + "\", color=" + color + "];\n")
        }
        for (String callee : methodsToShow) {
            for (String caller : new TreeSet<String>(getCallers(callersOfEachMethod, callee))) {
                if (methodsToShow.contains(caller)) {
                    dot.append("  \"" + caller + "\" -> \"" + callee + "\";\n")
                }
            }
        }
        dot.append("}\n")

        dotFile.text = dot.toString()
    }

    private static Set<String> findMethodsToShow(Map<String, Set<String>> callersOfEachMethod, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        Set<String> modifiedMethods = new TreeSet<>()
        modifiedMethods.addAll(leftModifiedMethods)
        modifiedMethods.addAll(rightModifiedMethods)

        Set<String> methodsToShow = new TreeSet<>()
        for (String modifiedMethod : modifiedMethods) {
            methodsToShow.add(modifiedMethod)
            methodsToShow.addAll(findAncestors(callersOfEachMethod, modifiedMethod).keySet())
        }
        return methodsToShow
    }

    private static String getColor(String method, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods, List<String> entrypoints) {
        boolean isEntrypoint = entrypoints.contains(method)
        boolean isModifiedByLeft = leftModifiedMethods.contains(method)
        boolean isModifiedByRight = rightModifiedMethods.contains(method)

        if (isEntrypoint) {
            return "yellow"
        }
        if (isModifiedByLeft && isModifiedByRight) {
            return "blue"
        }
        if (isModifiedByLeft) {
            return "green"
        }
        if (isModifiedByRight) {
            return "magenta"
        }
        return "black"
    }

    /**
     * e.g. "<a.b.Text: void countDupWords()>" -> "Text.countDupWords()"; a name that is not a Soot signature stays as it is.
     */
    private static String getShortName(String method) {
        boolean isSootSignature = method.startsWith("<") && method.contains(": ")
        if (!isSootSignature) {
            return method
        }
        String className = method.substring(1, method.indexOf(": "))
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1)
        String subsignature = method.substring(method.indexOf(": ") + 2, method.length() - 1)
        String methodWithParameters = subsignature.substring(subsignature.indexOf(" ") + 1)
        return simpleClassName + "." + methodWithParameters
    }

    /**
     * The signature conflict-static-analysis takes as entrypoint: the class name without quotes and the Soot
     * subsignature of the method.
     */
    static String getSootMethodSignature(SootMethod method) {
        return "<" + method.getDeclaringClass().getName() + ": " + method.getSubSignature() + ">"
    }
}
