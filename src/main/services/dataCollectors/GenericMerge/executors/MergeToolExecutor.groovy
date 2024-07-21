package services.dataCollectors.GenericMerge.executors

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.dataCollectors.GenericMerge.GenericMergeDataCollector

import java.nio.file.Path

abstract class MergeToolExecutor {
    private static Logger LOG = LogManager.getLogger(MergeToolExecutor.class)

    private static final int NUMBER_OF_EXECUTIONS = 5

    GenericMergeDataCollector.MergeScenarioExecutionSummary runToolForMergeScenario(Path scenario) {
        LOG.trace("Starting execution of merge scenario with tool ${getToolName()}")

        List<Long> executionTimes = new ArrayList<>()
        def outputFilePath = scenario.resolve("merge." + getToolName().toLowerCase() + ".java")
        GenericMergeDataCollector.MergeScenarioResult result = null

        for (int i = 0; i < NUMBER_OF_EXECUTIONS; i++) {
            LOG.trace("Starting execution ${i + 1} of ${NUMBER_OF_EXECUTIONS}")
            long startTime = System.nanoTime()
            result = executeTool(scenario, outputFilePath)
            long endTime = System.nanoTime()
            LOG.trace("Finished execution ${i + 1} of ${NUMBER_OF_EXECUTIONS} IN ${endTime - startTime} ns")
            // If we're running more than one execution, we use the first one as a warm up
            if (NUMBER_OF_EXECUTIONS == 1 || i > 0) {
                executionTimes.add(endTime - startTime)
            }
        }

        long averageTime = (long) (executionTimes.stream().reduce(0, (prev, cur) -> prev + cur) / executionTimes.size())

        def summary = new GenericMergeDataCollector.MergeScenarioExecutionSummary(scenario,
                outputFilePath,
                result,
                averageTime,
                this.getToolName())

        LOG.trace("Finished execution of merge scenario with tool ${summary.tool} in ${summary.time}ns with ${summary.result.toString()}")
        return summary
    }

    protected abstract GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario, Path outputFile);

    abstract String getToolName();
}
