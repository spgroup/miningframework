package services.dataCollectors.GenericMerge.executors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.dataCollectors.GenericMerge.model.MergeScenarioResult
import util.ProcessRunner

import java.nio.file.Path

class GenericMergeToolExecutor extends MergeToolExecutor {
    private static Logger LOG = LogManager.getLogger(GenericMergeToolExecutor.class)

    @Override
    protected MergeScenarioResult executeTool(Path scenario, Path outputFile) {
        def working_directory_path = scenario.toAbsolutePath().toString()

        def processBuilder = ProcessRunner.buildProcess(working_directory_path)
        processBuilder.command().addAll(getBuildParameters(outputFile))

        def output = ProcessRunner.startProcess(processBuilder)
        output.waitFor()

        if (output.exitValue() > 1) {
            LOG.warn("Error while merging ${scenario.toAbsolutePath()}. Generic Merge exited with exitCode ${output.exitValue()}")
            LOG.debug("Generic Merge output: ${output.getInputStream().readLines()}")
        }

        return output.exitValue() == 0 ? MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS : output.exitValue() == 1 ? MergeScenarioResult.SUCCESS_WITH_CONFLICTS : MergeScenarioResult.TOOL_ERROR
    }

    @Override
    protected boolean shouldSkipFileNormalization() {
        return false
    }

    @Override
    String getToolName() {
        return "GENERIC_MERGE"
    }

    private static List<String> getBuildParameters(Path outputFile) {
        def list = new ArrayList<String>()
        list.add(GenericMergeConfig.GENERIC_MERGE_BINARY_PATH)
        list.add("merge")
        list.add("--base-path=base.java")
        list.add("--left-path=left.java")
        list.add("--right-path=right.java")
        list.add("--merge-path=${outputFile.toAbsolutePath().toString()}".toString())
        list.add("--language=java")
        return list
    }
}
