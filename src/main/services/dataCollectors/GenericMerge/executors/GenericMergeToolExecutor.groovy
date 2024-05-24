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
        def list = new ArrayList<String>()
        list.add(GENERIC_MERGE_BINARY_PATH)
        list.add("--base-path=basejava")
        list.add("--left-path=leftjava")
        list.add("--right-path=rightjava")
        list.add("--merge-path=merge.generic.java")
        list.add("--language=java")
        return list;
    }
}
