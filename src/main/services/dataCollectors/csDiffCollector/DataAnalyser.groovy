package services.dataCollectors.csDiffCollector

import project.MergeCommit
import project.Project
import services.util.BuildRequester
import services.util.CSDiffMergeCommitSummary
import services.util.CSDiffMergeScenarioSummary
import services.util.MergeCommitSummary
import services.util.MergeScenarioSummary
import util.Handlers

import java.nio.file.Path

class DataAnalyser {

    /**
     * Analyses each merge scenario's directories after S3M has run. It constructs a {@link MergeScenarioSummary} for each
     * merge scenario and a global {@link MergeCommitSummary} for each merge commit.
     * @param project
     * @param mergeCommit
     * @param mergeScenarios
     * @return a summary of results of the merge commit
     */
    static CSDiffMergeCommitSummary analyseScenarios(List<Path> mergeScenarios) {
        CSDiffMergeCommitSummary summary = new CSDiffMergeCommitSummary()
        buildCommitSummary(summary, mergeScenarios)

        return summary
    }

    private static void buildCommitSummary(CSDiffMergeCommitSummary summary, List<Path> mergeScenarios) {
        mergeScenarios.stream()
                .map(CSDiffMergeScenarioSummary::new)
                .forEach(summary::addMergeSummary)
    }

}
