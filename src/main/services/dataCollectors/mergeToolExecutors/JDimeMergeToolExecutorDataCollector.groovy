package services.dataCollectors.mergeToolExecutors


import java.nio.file.Path

class JDimeMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    @Override
    protected String getExecutionDirectory() {
        return "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"
    }

    @Override
    protected List<String> getArgumentsForTool(Path file, Path outputFile) {
        return Arrays.asList("./JDime",
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
