package services.dataCollectors.S3MMergesCollector

import project.MergeCommit
import project.Project
import services.util.BuildRequester
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
    static MergeCommitSummary analyseScenarios(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios) {
        MergeCommitSummary summary = new MergeCommitSummary()
        buildCommitSummary(summary, mergeScenarios)

        checkForFalseNegatives(project, mergeCommit, mergeScenarios, summary)
        return summary
    }

    private static void buildCommitSummary(MergeCommitSummary summary, List<Path> mergeScenarios) {
        mergeScenarios.stream()
                .map(MergeScenarioSummary::new)
                .forEach(summary::addMergeSummary)
    }

    private static void checkForFalseNegatives(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, MergeCommitSummary summary) {
        summary.numberOfConflicts.eachWithIndex { int numConflicts, int i ->
            if (numConflicts == 0 && !summary.handlersHaveSameConflicts) {
                // there's a merge result with at least one conflict
                String buildLink = BuildRequester.requestBuildWithRevision(project, mergeCommit, mergeScenarios, i)
                summary.markAsChecking(buildLink, Handlers.mergeAlgorithms[i])
                println 'Requested Travis build'
            }
        }
    }

}
