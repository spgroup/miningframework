package services.dataCollectors.GenericMerge.executors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.GenericMerge.FileFormatNormalizer
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.dataCollectors.GenericMerge.model.MergeScenarioExecutionSummary
import services.dataCollectors.GenericMerge.model.MergeScenarioResult

import java.nio.file.Path

abstract class MergeToolExecutor {
    private static Logger LOG = LogManager.getLogger(MergeToolExecutor.class)

    MergeScenarioExecutionSummary runToolForMergeScenario(Path scenario) {
        LOG.trace("Starting execution of tool ${getToolName()} in ${scenario}")

        List<Long> executionTimes = new ArrayList<>()
        def outputFilePath = scenario.resolve("merge." + getToolName().toLowerCase() + ".java")
        MergeScenarioResult result = null

        for (int i = 0; i < GenericMergeConfig.NUMBER_OF_EXECUTIONS; i++) {
            LOG.trace("Starting execution ${i + 1} of ${GenericMergeConfig.NUMBER_OF_EXECUTIONS}")
            long startTime = System.nanoTime()
            result = executeTool(scenario, outputFilePath)
            long endTime = System.nanoTime()
            LOG.trace("Finished execution ${i + 1} of ${GenericMergeConfig.NUMBER_OF_EXECUTIONS} IN ${endTime - startTime} ns")
            // If we're running more than one execution, we use the first one as a warm up
            if (GenericMergeConfig.NUMBER_OF_EXECUTIONS == 1 || i > 0) {
                executionTimes.add(endTime - startTime)
            }
        }

        long averageTime = (long) (executionTimes.stream().reduce(0, (prev, cur) -> prev + cur) / executionTimes.size())

        if (result == MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS && !shouldSkipFileNormalization()) {
            FileFormatNormalizer.normalizeFileInPlace(outputFilePath)
        }

        def summary = new MergeScenarioExecutionSummary(scenario,
                outputFilePath,
                result,
                averageTime,
                this.getToolName())

        LOG.trace("Finished execution of tool ${summary.tool} in ${scenario}. Execution took ${summary.time}ns and finished with ${summary.result.toString()} status")
        return summary
    }

    protected abstract MergeScenarioResult executeTool(Path scenario, Path outputFile);

    protected abstract boolean shouldSkipFileNormalization();

    abstract String getToolName();
}
