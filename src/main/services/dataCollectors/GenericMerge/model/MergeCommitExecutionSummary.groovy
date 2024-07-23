package services.dataCollectors.GenericMerge.model

class MergeCommitExecutionSummary {
    public final MergeScenarioResult result
    public final boolean allScenariosMatch

    private MergeCommitExecutionSummary(MergeScenarioResult result, boolean allScenariosMatch) {
        this.result = result
        this.allScenariosMatch = allScenariosMatch
    }

    static MergeCommitExecutionSummary fromFileResultsList(List<MergeScenarioExecutionSummary> results) {
        def result = results.stream()
                .map { it -> it.result }
                .reduce(MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS, MergeCommitExecutionSummary::validateResult)
        def allMatchesOracle = results.stream().every { it -> it.isEquivalentToOracle() }
        return new MergeCommitExecutionSummary(result, allMatchesOracle)
    }

    private static validateResult(MergeScenarioResult prev, MergeScenarioResult cur) {
        return cur != MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS ? cur : prev
    }
}
