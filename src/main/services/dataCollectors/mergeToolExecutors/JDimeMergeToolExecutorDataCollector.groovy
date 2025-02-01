package services.dataCollectors.mergeToolExecutors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import services.util.MergeConflict
import util.ProcessRunner

import java.nio.file.Path
import java.util.concurrent.TimeUnit

class JDimeMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static Logger LOG = LogManager.getLogger(JDimeMergeToolExecutorDataCollector.class)

    private static final String JDIME_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"

    @Override
    protected MergeExecutionResult executeTool(Path scenario, Path outputFile) {
        def working_directory_path = scenario.toAbsolutePath().toString()

        def processBuilder = ProcessRunner.buildProcess(JDIME_BINARY_PATH,
                "./JDime",
                "-f",
                "--mode=structured",
                "--output=${outputFile.toAbsolutePath().toString()}".toString(),
                "${working_directory_path}/left.java",
                "${working_directory_path}/base.java",
                "${working_directory_path}/right.java")

        def output = ProcessRunner.startProcess(processBuilder)
        def hasCompleted = output.waitFor(1, TimeUnit.HOURS)
        if (!hasCompleted) {
            LOG.warn("jDime has timed out during execution")
            return MergeExecutionResult.TIMEOUT
        }

        if (output.exitValue() >= 200) {
            LOG.warn("Error while merging ${scenario.toAbsolutePath()}. jDime exited with exitCode ${output.exitValue()}")
            LOG.debug("jDime output: ${output.getInputStream().readLines()}")
            return MergeExecutionResult.TOOL_ERROR
        }

        def mergeConflictsCount = MergeConflict.getConflictsNumber(outputFile)
        if (mergeConflictsCount > 0) {
            return MergeExecutionResult.SUCCESS_WITH_CONFLICTS
        }

        return MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS
    }

    @Override
    String getToolName() {
        return "jdime"
    }
}
