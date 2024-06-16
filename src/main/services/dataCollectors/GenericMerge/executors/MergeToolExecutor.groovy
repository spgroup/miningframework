package services.dataCollectors.GenericMerge.executors

import services.dataCollectors.GenericMerge.GenericMergeDataCollector;

import java.nio.file.Path;

abstract class MergeToolExecutor {
    GenericMergeDataCollector.MergeScenarioExecutionSummary runToolForMergeScenario(Path scenario) {
        def startTime = System.nanoTime();
        def outputFilePath = scenario.resolve("merge.${getToolName().toLowerCase()}.java")
        def result = executeTool(scenario, outputFilePath)
        return new GenericMergeDataCollector.MergeScenarioExecutionSummary(scenario, outputFilePath, result, System.nanoTime() - startTime, this.getToolName())
    }

    protected abstract GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario, Path outputFile);

    protected abstract String getToolName();
}
