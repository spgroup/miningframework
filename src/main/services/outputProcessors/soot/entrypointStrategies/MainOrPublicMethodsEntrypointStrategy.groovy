package services.outputProcessors.soot.entrypointStrategies

/**
 * The analyses start from the main methods of the build or, when there is none, from its public methods.
 * conflict-static-analysis expands the marker with the same rule it uses for the call graph roots.
 */
class MainOrPublicMethodsEntrypointStrategy implements EntrypointStrategy {

    static final String MAIN_OR_PUBLIC = "MAIN_OR_PUBLIC"

    List<String> getEntrypoints(String buildJarPath, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        return [MAIN_OR_PUBLIC]
    }
}
