package services.util

class CSDiffMergeCommitSummary {

    int numberOfModifiedFiles
    List<Integer> numberOfConflicts
    List<Integer> numberOfFilesWithConflicts
    boolean mergesHaveSameOutputs
    boolean actualMergeAndCSDiffHaveSameOutput
    boolean actualMergeAndTextualHaveSameOutput
    boolean actualMergeAndGitMergeHaveSameOutput

    List<CSDiffMergeScenarioSummary> mergeScenarioSummaries

    CSDiffMergeCommitSummary() {
        this.numberOfModifiedFiles = 0
        this.numberOfConflicts = [0, 0, 0]
        this.numberOfFilesWithConflicts = [0, 0, 0]
        this.mergesHaveSameOutputs = true
        this.actualMergeAndCSDiffHaveSameOutput = true
        this.actualMergeAndTextualHaveSameOutput = true
        this.actualMergeAndGitMergeHaveSameOutput = true
        this.mergeScenarioSummaries = []
    }

    /**
     * Add the merge scenario summary's information to this summary.
     * @param scenarioSummary
     */
    void addMergeSummary(CSDiffMergeScenarioSummary scenarioSummary) {
        numberOfModifiedFiles++
        addConflicts(scenarioSummary.numberOfConflicts)

        mergesHaveSameOutputs &= !scenarioSummary.getHasDifferenceBetweenMergeResults()
        actualMergeAndCSDiffHaveSameOutput &= !scenarioSummary.getHasDifferenceFromCSDiffToActualMerge()
        actualMergeAndTextualHaveSameOutput &= !scenarioSummary.getHasDifferenceFromTextualToActualMerge()
        actualMergeAndGitMergeHaveSameOutput &= !scenarioSummary.getHasDifferenceFromGitMergeToActualMerge()

        mergeScenarioSummaries.add(scenarioSummary)
    }

    private void addConflicts(List<Integer> localNumberOfConflicts) {
        for (int i = 0; i < numberOfConflicts.size(); i++) {
            numberOfConflicts[i] += localNumberOfConflicts[i]
            if (localNumberOfConflicts[i] > 0) {
                numberOfFilesWithConflicts[i] += 1
            }
        }
    }

    @Override
    String toString() {
        return "${numberOfModifiedFiles},${Utils.toStringList(numberOfFilesWithConflicts, ',')},${Utils.toStringList(numberOfConflicts, ',')},${mergesHaveSameOutputs},${actualMergeAndTextualHaveSameOutput},${actualMergeAndCSDiffHaveSameOutput},${actualMergeAndGitMergeHaveSameOutput},,,"
    }

    private static String toStringMap(Map<String, String> map, String separator) {
        StringBuilder string = new StringBuilder()

        map.each { key, value ->
            string.append(Utils.getHyperLink(value, key)).append(separator)
        }

        return string.toString()
    }

}
