package services.outputProcessors.soot

/**
 * A merge scenario analysed as a whole, with the entrypoints chosen by a strategy: its lines files have the lines
 * modified in all its methods ([scenario directory]/left-right-lines.csv, by GenerateSootInputFilesFromJsonOutputProcessor),
 * so the analyses also find conflicts between a method modified by left and another one modified by right.
 */
class MergeScenario extends Scenario {

    private Set<String> leftModifiedMethods
    private Set<String> rightModifiedMethods

    /**
     * @param className a class modified in the scenario, by which the build of the scenario is found (Scenario.getClassPath)
     */
    MergeScenario(String projectName, String className, String commitSHA, String scenarioDirectory, boolean hasBuild,
                  String entrypoints, Set<String> leftModifiedMethods, Set<String> rightModifiedMethods) {
        super(projectName, className, null, commitSHA, scenarioDirectory, hasBuild, entrypoints)
        this.leftModifiedMethods = leftModifiedMethods
        this.rightModifiedMethods = rightModifiedMethods
    }

    Set<String> getLeftModifiedMethods() {
        return leftModifiedMethods
    }

    Set<String> getRightModifiedMethods() {
        return rightModifiedMethods
    }

    @Override
    String toString() {
        return "${getProjectName()};${getCommitSHA()}"
    }

    @Override
    String getLinesFilePath() {
        return "${getScenarioDirectory()}/left-right-lines.csv"
    }

    @Override
    String getLinesReverseFilePath() {
        return "${getScenarioDirectory()}/right-left-lines.csv"
    }
}
