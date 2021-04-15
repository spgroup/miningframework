package services.commitFilters

import app.MiningFramework
import interfaces.CommitFilter
import project.MergeCommit
import project.Project
import static app.MiningFramework.arguments


/**
 *  Combines the output of IsInCommitListFilter, MutuallyModifiedStaticBlocksCommitFilter
 *  and TextualConflictFilter with and clauses
 */
class InCommitListMutuallyModifiedStaticBlocksFilter implements CommitFilter {
    private CommitFilter commitListFilter = new IsInCommitListFilter();
    private CommitFilter mutuallyModifiedFilter = new MutuallyModifiedStaticBlocksCommitFilter();

    private File filteredScenarios = null;

    void createLogFileIfDoesntExist() {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        filteredScenarios = new File(dataFolder.getAbsolutePath() + "/filtered_scenarios.csv");

        if (!filteredScenarios.exists()) {
            dataFolder.mkdirs()
            filteredScenarios << "project,merge commit,filter reason\n"
        }
    }

    void addFilteredLog(Project project, MergeCommit mergeCommit, String reason) {
        filteredScenarios << project.getName() + "," + mergeCommit.getSHA() + "," + reason + "\n"
    }


    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        createLogFileIfDoesntExist();
        boolean isInCommitList = commitListFilter.applyFilter(project, mergeCommit);
        if (!isInCommitList) {
            return false;
        }

        boolean hasMutuallyModifiedStaticBlocks = mutuallyModifiedFilter.applyFilter(project, mergeCommit);
        if (!hasMutuallyModifiedStaticBlocks) {
            addFilteredLog(project, mergeCommit, "no mutually modified static blocks")
            return false;
        }

        return true;
    }
}