package services.outputProcessors.soot.entrypointStrategies

/**
 * The analyses start from the methods modified by left or by right.
 */
class ModifiedMethodsEntrypointStrategy implements EntrypointStrategy {

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        return ((leftModifiedMethods + rightModifiedMethods) as List).sort()
    }
}
