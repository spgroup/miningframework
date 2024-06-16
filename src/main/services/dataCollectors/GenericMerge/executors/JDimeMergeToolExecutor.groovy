package services.dataCollectors.GenericMerge.executors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.GenericMerge.GenericMergeDataCollector
import services.util.MergeConflict
import util.ProcessRunner

import java.nio.file.Path

class JDimeMergeToolExecutor extends MergeToolExecutor {
    private static Logger LOG = LogManager.getLogger(JDimeMergeToolExecutor.class)

    private static final BASE_EXPERIMENT_PATH = System.getProperty("miningframework.generic_merge.base_experiment_path")
    private static final String JDIME_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/jdime/install/JDime/bin"

    @Override
    protected GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario, Path outputFile) {
        def working_directory_path = scenario.toAbsolutePath().toString()

        def processBuilder = ProcessRunner.buildProcess(JDIME_BINARY_PATH,
                "./JDime",
                "--mode=structured",
                "--output=${outputFile.toAbsolutePath().toString()}".toString(),
                "${working_directory_path}/left.java",
                "${working_directory_path}/base.java",
                "${working_directory_path}/right.java")

        def output = ProcessRunner.startProcess(processBuilder)
        output.waitFor()

        if (output.exitValue() >= 200) {
            LOG.warn("Error while merging ${scenario.toAbsolutePath()}. jDime exited with exitCode ${output.exitValue()}")
            LOG.debug("jDime output: ${output.getInputStream().readLines()}")
        }

        def mergeConflictsCount = MergeConflict.getConflictsNumber(outputFile)
        if (mergeConflictsCount > 0) {
            return GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITH_CONFLICTS
        }

        return GenericMergeDataCollector.MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS
    }

    @Override
    String getToolName() {
        return "JDIME"
    }
}
