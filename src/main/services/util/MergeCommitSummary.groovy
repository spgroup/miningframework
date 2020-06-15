package services.util

class MergeCommitSummary {

    int numberOfModifiedFiles
    List<Integer> numberOfConflicts
    boolean handlersHaveSameOutputs
    boolean handlersHaveSameConflicts
    Map<String, String> checkingBuilds

    List<MergeScenarioSummary> mergeScenarioSummaries

    MergeCommitSummary() {
        this.numberOfModifiedFiles = 0
        this.numberOfConflicts = [0, 0, 0, 0, 0]
        this.handlersHaveSameOutputs = true
        this.handlersHaveSameConflicts = true
        this.mergeScenarioSummaries = []
        this.checkingBuilds = [:]
    }

    /**
     * Add the merge scenario summary's information to this summary.
     * @param scenarioSummary
     */
    void addMergeSummary(MergeScenarioSummary scenarioSummary) {
        numberOfModifiedFiles++
        addConflicts(scenarioSummary.numberOfConflicts)

        handlersHaveSameOutputs &= !scenarioSummary.getDifferenceBetweenMergeResults().contains(false)
        handlersHaveSameConflicts &= !scenarioSummary.getDifferenceBetweenConflictSets().contains(false)

        mergeScenarioSummaries.add(scenarioSummary)
    }

    /**
     * Add a link for a Travis build.
     * @param link
     * @param mergeAlgorithm
     */
    void markAsChecking(String link, String mergeAlgorithm) {
        checkingBuilds[mergeAlgorithm] = link
    }

    private void addConflicts(List<Integer> localNumberOfConflicts) {
        for (int i = 0; i < numberOfConflicts.size(); i++) {
            numberOfConflicts[i] += localNumberOfConflicts[i]
        }
    }

    @Override
    String toString() {
        return "${numberOfModifiedFiles},${Utils.toStringList(numberOfConflicts, ',')},${handlersHaveSameOutputs},${handlersHaveSameConflicts},${toStringMap(checkingBuilds, ',')},,,"
    }

    private static String toStringMap(Map<String, String> map, String separator) {
        StringBuilder string = new StringBuilder()

        map.each { key, value ->
            string.append(Utils.getHyperLink(value, key)).append(separator)
        }

        return string.toString()
    }

}
