package unit

import org.junit.Assert
import org.junit.Test
import services.outputProcessors.soot.entrypointStrategies.LowestCommonAncestorEntrypointStrategy

/**
 * The lowest common ancestors of the example graphs of the slides "Exemplos 1 Par de mudança" and "Exemplos 2 Pares de
 * mudanças": l is modified by left, r and r2 by right, b is a method of base (not modified) and the expected
 * entrypoints are the yellow methods. Each graph is written as its arrows, from the caller to the method it calls.
 */
class LowestCommonAncestorEntrypointStrategyTest {

    // 1 pair: m calls l and r
    @Test
    void onePairCalledByTheSameMethod() {
        Map<String, Set<String>> callersOfEachMethod = callGraph("m -> l", "m -> r")

        List<String> entrypoints = LowestCommonAncestorEntrypointStrategy.findLowestCommonAncestors(callersOfEachMethod, ["l"] as Set, ["r"] as Set)

        Assert.assertEquals(["m"], entrypoints)
    }

    // 1 pair: m calls l, and calls r through b
    @Test
    void onePairWithABaseMethodBetween() {
        Map<String, Set<String>> callersOfEachMethod = callGraph("m -> l", "m -> b", "b -> r")

        List<String> entrypoints = LowestCommonAncestorEntrypointStrategy.findLowestCommonAncestors(callersOfEachMethod, ["l"] as Set, ["r"] as Set)

        Assert.assertEquals(["m"], entrypoints)
    }

    // 2 pairs: m calls l, r and r2; m is the ancestor of both pairs, (l, r) and (l, r2), but is an entrypoint once
    @Test
    void twoPairsWithTheSameAncestor() {
        Map<String, Set<String>> callersOfEachMethod = callGraph("m -> l", "m -> r", "m -> r2")

        List<String> entrypoints = LowestCommonAncestorEntrypointStrategy.findLowestCommonAncestors(callersOfEachMethod, ["l"] as Set, ["r", "r2"] as Set)

        Assert.assertEquals(["m"], entrypoints)
    }

    // 2 pairs: m calls l, r and b; b calls l and r2. (l, r): m, at 1 + 1 calls; (l, r2): b, at 1 + 1 calls (m is at 1 + 2)
    @Test
    void twoPairsWithDifferentAncestors() {
        Map<String, Set<String>> callersOfEachMethod = callGraph("m -> l", "m -> r", "m -> b", "b -> l", "b -> r2")

        List<String> entrypoints = LowestCommonAncestorEntrypointStrategy.findLowestCommonAncestors(callersOfEachMethod, ["l"] as Set, ["r", "r2"] as Set)

        Assert.assertEquals(["b", "m"], entrypoints)
    }

    /**
     * The callers of each method of a graph written as arrows "caller -> callee", as the strategy gets them from the
     * call graph of a build.
     */
    private static Map<String, Set<String>> callGraph(String... arrows) {
        Map<String, Set<String>> callersOfEachMethod = new HashMap<>()
        for (String arrow : arrows) {
            String caller = arrow.split("->")[0].trim()
            String callee = arrow.split("->")[1].trim()
            if (!callersOfEachMethod.containsKey(callee)) {
                callersOfEachMethod.put(callee, new HashSet<String>())
            }
            callersOfEachMethod.get(callee).add(caller)
        }
        return callersOfEachMethod
    }
}
