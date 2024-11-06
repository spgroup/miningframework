package services.dataCollectors.common;

import interfaces.DataCollector;
import project.MergeCommit;
import project.Project;

import java.util.List;

public class RunDataCollectorsInParallel implements DataCollector {
    private final List<DataCollector> dataCollectorList;

    public RunDataCollectorsInParallel(List<DataCollector> dataCollectorList) {
        this.dataCollectorList = dataCollectorList;
    }

    @Override
    public void collectData(Project project, MergeCommit mergeCommit) {
        this.dataCollectorList.parallelStream().forEach(dataCollector -> dataCollector.collectData(project, mergeCommit));
    }
}
