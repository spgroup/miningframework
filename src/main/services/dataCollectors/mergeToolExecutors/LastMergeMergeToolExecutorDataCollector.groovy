package services.dataCollectors.mergeToolExecutors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import util.ProcessRunner

import java.nio.file.Path

class LastMergeMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static Logger LOG = LogManager.getLogger(LastMergeMergeToolExecutorDataCollector.class)

    private static final String LAST_MERGE_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/last-merge"

    @Override
    protected MergeExecutionResult executeTool(Path file, Path outputFile) {
        def workingDirectory = file.toAbsolutePath().toString()

        def processBuilder = ProcessRunner.buildProcess(workingDirectory)
        processBuilder.command().addAll(getBuildParameters(outputFile))

        def output = ProcessRunner.startProcess(processBuilder)
        output.waitFor()

        if (output.exitValue() > 1) {
            LOG.warn("Error while merging ${file.toAbsolutePath()}. LAST Merge exited with exitCode ${output.exitValue()}")
            LOG.debug("LAST Merge output: ${output.getInputStream().readLines()}")
        }

        return output.exitValue() == 0 ? MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS : output.exitValue() == 1 ? MergeExecutionResult.SUCCESS_WITH_CONFLICTS : MergeExecutionResult.TOOL_ERROR
    }

    @Override
    String getToolName() {
        return "last_merge"
    }

    private static List<String> getBuildParameters(Path outputFile) {
        def list = new ArrayList<String>()
        list.add(LAST_MERGE_BINARY_PATH)
        list.add("merge")
        list.add("--base-path=base.java")
        list.add("--left-path=left.java")
        list.add("--right-path=right.java")
        list.add("--merge-path=${outputFile.toAbsolutePath().toString()}".toString())
        list.add("--language=java")
        return list
    }
}
