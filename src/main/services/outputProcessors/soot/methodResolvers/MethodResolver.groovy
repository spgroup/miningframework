package services.outputProcessors.soot.methodResolvers

/**
 * Resolves the modified lines of a merge scenario to the Soot signatures of the methods that contain them, which
 * conflict-static-analysis takes as entrypoints (e.g. "<a.C: void process(a.Node,java.util.List)>"). To plug in
 * another way of resolving them, implement this interface and register it in MethodResolvers.
 */
interface MethodResolver {

    /**
     * @param buildJarPath the build of the merge
     * @param sourceDirectory the merge version of the modified Java files (e.g. [scenario]/source/merge/C.java)
     * @param definitions the modified lines of each class, e.g. {"a.C": [10, 12]}, in the numbering of the merge
     * @return the Soot signatures of the methods with a modified line
     */
    Set<String> resolve(String buildJarPath, String sourceDirectory, Map<String, List<Integer>> definitions)
}
