package services.dataCollectors.mergeToolExecutors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import services.util.MergeConflict
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path

class MergirafMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static Logger LOG = LogManager.getLogger(MergirafMergeToolExecutorDataCollector.class)

    private static String MERGIRAF_PATH = "./dependencies/mergiraf"

    @Override
    protected MergeExecutionResult executeTool(Path scenario, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))

        processBuilder.command().add(MERGIRAF_PATH)
        processBuilder.command().add("merge")
        processBuilder.command().add(scenario.resolve("base.java").toAbsolutePath().toString())
        processBuilder.command().add(scenario.resolve("left.java").toAbsolutePath().toString())
        processBuilder.command().add(scenario.resolve("right.java").toAbsolutePath().toString())
        processBuilder.command().add("--output=${scenario.resolve(outputFile).toAbsolutePath().toString()}}")

        LOG.trace("Calling mergiraf with command \"${processBuilder.command().join(' ')}\"")

        def process = ProcessRunner.startProcess(processBuilder)
        process.getErrorStream().eachLine(LOG::warn)
        def exitCode = process.waitFor()

        if (!Files.exists(outputFile)) {
            return MergeExecutionResult.TOOL_ERROR
        } else if (MergeConflict.getConflictsNumber(outputFile) > 0) {
            return MergeExecutionResult.SUCCESS_WITH_CONFLICTS
        }
        return MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS
    }

    @Override
    String getToolName() {
        return "MERGIRAF"
    }
}
