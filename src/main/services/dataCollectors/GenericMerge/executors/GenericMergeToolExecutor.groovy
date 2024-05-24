package services.dataCollectors.GenericMerge.executors

import services.dataCollectors.GenericMerge.GenericMergeDataCollector
import util.ProcessRunner

import java.nio.file.Path

class GenericMergeToolExecutor extends MergeToolExecutor {
    private static final BASE_EXPERIMENT_PATH = "/usr/src/app"
    private static final String GENERIC_MERGE_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/generic-merge"

    @Override
    protected GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario) {
        def working_directory_path = scenario.toAbsolutePath().toString();

        def processBuilder = ProcessRunner.buildProcess(working_directory_path);
        processBuilder.command().addAll(getBuildParameters())

        def output = ProcessRunner.startProcess(processBuilder);
        output.waitFor()

        if (output.exitValue() > 1) {
            println("Error while merging ${scenario.toAbsolutePath()}: ${output.getInputStream().readLines()}")
        }

        return output.exitValue() == 0 ? GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS : output.exitValue() == 1 ? GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITH_CONFLICTS : GenericMergeDataCollector.MergeScenarioResult.TOOL_ERROR;
    }

    private static List<String> getBuildParameters() {
        return List.of(GENERIC_MERGE_BINARY_PATH,
                "--base-path=basejava",
                "--left-path=leftjava",
                "--right-path=rightjava",
                "--merge-path=merge.generic.java",
                "--language=java")
    }
}
