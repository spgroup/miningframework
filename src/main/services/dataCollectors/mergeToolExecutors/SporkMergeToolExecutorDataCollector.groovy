package services.dataCollectors.mergeToolExecutors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class SporkMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static Logger LOG = LogManager.getLogger(SporkMergeToolExecutorDataCollector.class)

    private static final String SPORK_JAR_PATH = "${System.getProperty("user.dir")}/dependencies/spork.jar"

    @Override
    protected MergeExecutionResult executeTool(Path file, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))
        processBuilder.command().addAll(getBuildParameters(file, outputFile))

        LOG.trace("Calling spork with command \"${processBuilder.command().join(' ')}\"")
        def output = ProcessRunner.startProcess(processBuilder)
        def hasCompleted = output.waitFor(1, TimeUnit.HOURS)
        if (!hasCompleted) {
            LOG.warn("Spork has timed out during execution")
            return MergeExecutionResult.TIMEOUT
        }

        if (!Files.exists(outputFile)) {
            LOG.warn("SPORK execution failed: ${output.getInputStream().readLines()}")
            return MergeExecutionResult.TOOL_ERROR
        }

        return output.exitValue() == 0 ? MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS : MergeExecutionResult.SUCCESS_WITH_CONFLICTS
    }

    @Override
    String getToolName() {
        return 'spork'
    }

    private static List<String> getBuildParameters(Path file, Path outputFile) {
        def list = new ArrayList<String>()
        list.add("java")
        list.add("-jar")
        list.add(SPORK_JAR_PATH)
        list.add("-e")
        list.add(file.resolve("left.java").toString())
        list.add(file.resolve("base.java").toString())
        list.add(file.resolve("right.java").toString())
        list.add("--output=${outputFile.toString()}".toString())
        return list
    }
}
