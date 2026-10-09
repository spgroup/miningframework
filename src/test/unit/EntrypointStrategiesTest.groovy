package unit

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import services.outputProcessors.soot.LinesToSootMethodResolver
import services.outputProcessors.soot.entrypointStrategies.LowestCommonAncestorEntrypointStrategy
import services.outputProcessors.soot.entrypointStrategies.MainOrPublicMethodsEntrypointStrategy
import services.outputProcessors.soot.entrypointStrategies.ModifiedMethodsEntrypointStrategy
import services.outputProcessors.soot.entrypointStrategies.MutuallyModifiedMethodsEntrypointStrategy

/**
 * Entrypoint strategies on the example of the slides "Suitable entry point algorithms for static analysis in semantic
 * conflict detection tools" (test_repositories/entrypointStrategies/Text.java): main calls generateReport, which calls
 * countDupWords, countComments and countDupWhiteSpace; left modifies countDupWhiteSpace and right modifies
 * countDupWords and countDupWhiteSpace.
 */
class EntrypointStrategiesTest {

    private static final String BUILD_JAR = "test_repositories/entrypointStrategies/build.jar"
    private static final Map<String, List<Integer>> LEFT_LINES = [Text: [41]]
    private static final Map<String, List<Integer>> RIGHT_LINES = [Text: [23, 38]]

    private static final String MAIN = "<Text: void main(java.lang.String[])>"
    private static final String GENERATE_REPORT = "<Text: void generateReport()>"
    private static final String COUNT_DUP_WORDS = "<Text: void countDupWords()>"
    private static final String COUNT_DUP_WHITE_SPACE = "<Text: void countDupWhiteSpace()>"

    private static Set<String> leftModifiedMethods
    private static Set<String> rightModifiedMethods

    @BeforeClass
    static void setup() {
        leftModifiedMethods = LinesToSootMethodResolver.resolve(BUILD_JAR, LEFT_LINES)
        rightModifiedMethods = LinesToSootMethodResolver.resolve(BUILD_JAR, RIGHT_LINES)
    }

    @Test
    void modifiedLinesAreResolvedToTheirMethods() {
        Assert.assertEquals([COUNT_DUP_WHITE_SPACE] as Set, leftModifiedMethods)
        Assert.assertEquals([COUNT_DUP_WORDS, COUNT_DUP_WHITE_SPACE] as Set, rightModifiedMethods)
    }

    // 1: main or public methods
    @Test
    void mainOrPublicMethods() {
        List<String> entrypoints = new MainOrPublicMethodsEntrypointStrategy().getEntrypoints(BUILD_JAR, leftModifiedMethods, rightModifiedMethods)

        Assert.assertEquals([MAIN], entrypoints)
    }

    // 2: methods changed by two developers
    @Test
    void mutuallyModifiedMethods() {
        List<String> entrypoints = new MutuallyModifiedMethodsEntrypointStrategy().getEntrypoints(BUILD_JAR, leftModifiedMethods, rightModifiedMethods)

        Assert.assertEquals([COUNT_DUP_WHITE_SPACE], entrypoints)
    }

    // 3: methods changed by at least one developer
    @Test
    void modifiedMethods() {
        List<String> entrypoints = new ModifiedMethodsEntrypointStrategy().getEntrypoints(BUILD_JAR, leftModifiedMethods, rightModifiedMethods)

        Assert.assertEquals([COUNT_DUP_WHITE_SPACE, COUNT_DUP_WORDS], entrypoints)
    }

    // 4: most recent common ancestor of each pair (left method, right method), not the method itself: generateReport
    // for both (countDupWhiteSpace, countDupWords) and (countDupWhiteSpace, countDupWhiteSpace)
    @Test
    void lowestCommonAncestors() {
        List<String> entrypoints = new LowestCommonAncestorEntrypointStrategy().getEntrypoints(BUILD_JAR, leftModifiedMethods, rightModifiedMethods)

        Assert.assertEquals([GENERATE_REPORT], entrypoints)
    }
}
