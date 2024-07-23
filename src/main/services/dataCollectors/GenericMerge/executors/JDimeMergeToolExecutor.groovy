package services.dataCollectors.GenericMerge.executors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.dataCollectors.GenericMerge.model.MergeScenarioResult
import services.util.MergeConflict
import util.ProcessRunner

import java.nio.file.Path

class JDimeMergeToolExecutor extends MergeToolExecutor {
    private static Logger LOG = LogManager.getLogger(JDimeMergeToolExecutor.class)

    @Override
    protected MergeScenarioResult executeTool(Path scenario, Path outputFile) {
        def working_directory_path = scenario.toAbsolutePath().toString()

        def processBuilder = ProcessRunner.buildProcess(GenericMergeConfig.JDIME_BINARY_PATH,
                "./JDime",
                "-f",
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
            return MergeScenarioResult.TOOL_ERROR
        }

        def mergeConflictsCount = MergeConflict.getConflictsNumber(outputFile)
        if (mergeConflictsCount > 0) {
            return MergeScenarioResult.SUCCESS_WITH_CONFLICTS
        }

        return MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS
    }

    @Override
    protected boolean shouldSkipFileNormalization() {
        return true
    }

    @Override
    String getToolName() {
        return "JDIME"
    }
}
