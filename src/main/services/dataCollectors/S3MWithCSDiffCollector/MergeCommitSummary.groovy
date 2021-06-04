package services.dataCollectors.S3MWithCSDiffCollector

import util.TextualMergeStrategy

class MergeCommitSummary {

    int numberOfModifiedFiles
    Map<TextualMergeStrategy, Integer> numberOfConflicts
    boolean strategiesHaveSameOutputs
    boolean strategiesHaveSameConflicts
    List<MergeScenarioSummary> mergeScenarioSummaries

    MergeCommitSummary() {
        this.numberOfModifiedFiles = 0

        this.numberOfConflicts = [:]
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            this.numberOfConflicts.put(strategy, 0)
        }

        this.strategiesHaveSameOutputs = true
        this.strategiesHaveSameConflicts = true
        this.mergeScenarioSummaries = []
    }

    void addMergeSummary(MergeScenarioSummary mergeScenarioSummary) {
        this.numberOfModifiedFiles++
        addConflicts(mergeScenarioSummary)

        this.strategiesHaveSameOutputs &= mergeScenarioSummary.strategiesHaveSameOutputs()
        this.strategiesHaveSameConflicts &= mergeScenarioSummary.strategiesHaveSameConflicts()

        this.mergeScenarioSummaries.add(mergeScenarioSummary)
    }

    private void addConflicts(MergeScenarioSummary mergeScenarioSummary) {
        for (TextualMergeStrategy strategy: MergesCollector.strategies) {
            int currentNumberOfConflicts = this.numberOfConflicts.get(strategy)
            int additionalNumberOfConflicts = mergeScenarioSummary.numberOfConflicts.get(strategy)
            this.numberOfConflicts.put(strategy, currentNumberOfConflicts + additionalNumberOfConflicts)
        }
    }

    @Override
    String toString() {
        // TODO: Fill in here
    }

}