package services.dataCollectors.GenericMerge.executors

import services.dataCollectors.GenericMerge.GenericMergeDataCollector;

import java.nio.file.Path;

abstract class MergeToolExecutor {
    static GenericMergeDataCollector.MergeScenarioExecutionSummary runToolForMergeScenario(Path scenario) {
        def startTime = System.nanoTime();
        def result = executeTool(scenario)
        return new GenericMergeDataCollector.MergeScenarioExecutionSummary(scenario, result, System.nanoTime() - startTime, this.getSimpleName())
    }

    protected abstract GenericMergeDataCollector.MergeScenarioResult executeTool(Path scenario);
}
