package services.dataCollectors.common

import interfaces.DataCollector
import project.MergeCommit
import project.Project

class RunDataCollectorsInParallel implements DataCollector {
    private List<DataCollector> dataCollectors

    RunDataCollectorsInParallel(List<DataCollector> dataCollectors) {
        this.dataCollectors = dataCollectors
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        this.dataCollectors.parallelStream().forEach(collector -> collector.collectData(project, mergeCommit))
    }
}
