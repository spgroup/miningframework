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

        def processBuilder = ProcessRunner.buildProcess(JDIME_BINARY_PATH);
        processBuilder.command().addAll(getBuildParameters(working_directory_path))

        def output = ProcessRunner.startProcess(processBuilder);
        output.waitFor()

        return output.exitValue() == 0 ? GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS : output.exitValue() == 1 ? GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITH_CONFLICTS : GenericMergeDataCollector.MergeScenarioResult.TOOL_ERROR;
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
