package services.dataCollectors.mergeToolExecutors

import java.nio.file.Path

class MergirafMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static String MERGIRAF_PATH = "./dependencies/mergiraf"

    @Override
    protected List<String> getArgumentsForTool(Path file, Path outputFile) {
        return Arrays.asList(MERGIRAF_PATH,
                "merge",
                file.resolve("base.java").toAbsolutePath().toString(),
                file.resolve("left.java").toAbsolutePath().toString(),
                file.resolve("right.java").toAbsolutePath().toString(),
                "--output=${file.resolve(outputFile).toAbsolutePath().toString()}".toString())

    }

    @Override
    String getToolName() {
        return "mergiraf"
    }
}
