package services.outputProcessors.soot.entrypointStrategies

/**
 * The analyses start from the methods modified by both left and right.
 */
class MutuallyModifiedMethodsEntrypointStrategy implements EntrypointStrategy {

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        return leftModifiedMethods.intersect(rightModifiedMethods).sort()
    }
}
