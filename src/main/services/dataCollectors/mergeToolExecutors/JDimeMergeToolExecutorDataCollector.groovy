package services.dataCollectors.mergeToolExecutors


import java.nio.file.Path

class JDimeMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static final String JDIME_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"

    @Override
    protected List<String> getArgumentsForTool(Path file, Path outputFile) {
        return Arrays.asList(JDIME_BINARY_PATH,
                "-f",
                "--mode=structured",
                "--output=${file.resolve(outputFile).toAbsolutePath().toString()}}".toString(),
                file.resolve("left.java").toAbsolutePath().toString(),
                file.resolve("base.java").toAbsolutePath().toString(),
                file.resolve("right.java").toAbsolutePath().toString())
    }

    @Override
    String getToolName() {
        return "jdime"
    }
}
