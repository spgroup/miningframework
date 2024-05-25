package services.dataCollectors.GenericMerge.executors

import services.dataCollectors.GenericMerge.GenericMergeDataCollector
import util.ProcessRunner

import java.nio.file.Path

class JDimeMergeToolExecutor extends MergeToolExecutor {
    private static final BASE_EXPERIMENT_PATH = "/usr/src/app"
    private static final String JDIME_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/jdime/install/JDime/bin"

    @Override
    protected GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario) {
        def working_directory_path = scenario.toAbsolutePath().toString();

        def processBuilder = ProcessRunner.buildProcess(
                JDIME_BINARY_PATH,
                "./JDime",
                "-sf",
                "--mode=structured",
                "--output=${working_directory_path}/merge.jdime.java",
                "${working_directory_path}/left.java",
                "${working_directory_path}/base.java",
                "${working_directory_path}/right.java"
        )

        def output = ProcessRunner.startProcess(processBuilder);
        output.waitFor()

        if (output.exitValue() == 0) {
            return GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS
        } else if (output.exitValue() >= 200) {
            println("Error while merging ${scenario.toAbsolutePath()}: ${output.getInputStream().readLines()}")
            return GenericMergeDataCollector.MergeScenarioResult.TOOL_ERROR
        }
        return GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITH_CONFLICTS
    }

    @Override
    protected String getToolName() {
        return "JDIME"
    }

    private static List<String> getBuildParameters(String basePath) {
        def list = new ArrayList<String>()
        list.add("./JDime")
        list.add("--mode=structured")
        list.add("--output=${basePath}/merge.generic.java")
        list.add("${basePath}/leftjava")
        list.add("${basePath}/basejava")
        list.add("${basePath}/rightjava")
        return list;
    }
}
