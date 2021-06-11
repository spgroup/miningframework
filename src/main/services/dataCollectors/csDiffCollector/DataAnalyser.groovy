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
     * Analyses each merge scenario's directories after CSDiff has run.
     * It constructs a {@link CSDiffMergeScenarioSummary} for each
     * merge scenario and a global {@link CSDiffMergeCommitSummary} for each merge commit.
     *
     * @param mergeScenarios to be analyzed
     * @return a summary of the results of the merge commit
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
