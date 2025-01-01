package services.dataCollectors.mergeToolExecutors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import util.ProcessRunner

import java.nio.file.Path

class MergirafMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static Logger LOG = LogManager.getLogger(MergirafMergeToolExecutorDataCollector.class)

    private static MERGIRAF_PATH = "${System.getProperty("user.dir")}/dependencies/mergiraf"

    @Override
    protected MergeExecutionResult executeTool(Path scenario, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))

        processBuilder.command().add(MERGIRAF_PATH)
        processBuilder.command().add("merge")
        processBuilder.command().add(scenario.resolve("base.java").toAbsolutePath().toString())
        processBuilder.command().add(scenario.resolve("left.java").toAbsolutePath().toString())
        processBuilder.command().add(scenario.resolve("right.java").toAbsolutePath().toString())
        processBuilder.redirectOutput(outputFile.toFile())

        LOG.trace("Calling mergiraf with command \"${processBuilder.command().join(' ')}\"")

        def exitCode = ProcessRunner.startProcess(processBuilder).waitFor()
        if (exitCode == 0) {
            return MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS
        } else (exitCode == 1) {
            return MergeExecutionResult.SUCCESS_WITH_CONFLICTS
        }
        return MergeExecutionResult.TOOL_ERROR
    }

    @Override
    String getToolName() {
        return "MERGIRAF"
    }
}
