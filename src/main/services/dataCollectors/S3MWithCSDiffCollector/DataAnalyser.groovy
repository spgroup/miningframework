package services.dataCollectors.S3MWithCSDiffCollector

import java.nio.file.Path

class DataAnalyser {

    static MergeCommitSummary analyseScenarios(List<Path> mergeScenarios) {
        MergeCommitSummary summary = new MergeCommitSummary()
        buildCommitSummary(summary, mergeScenarios)
        return summary
    }

    private static void buildCommitSummary(MergeCommitSummary summary, List<Path> mergeScenarios) {
        mergeScenarios.stream().map(MergeScenarioSummary::new).forEach(summary::addMergeSummary)
    }

}