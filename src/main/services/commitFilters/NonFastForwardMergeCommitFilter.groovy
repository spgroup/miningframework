package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project

public class NonFastForwardMergeCommitFilter implements CommitFilter {
    private CommitFilter hasMergeScenarioFilter = new S3MCommitFilter()

    @Override
    public boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return hasMergeScenarioFilter.applyFilter(project, mergeCommit) &&
                containsMutuallyModifiedFiles(mergeCommit)
    }

    private static boolean containsMutuallyModifiedFiles(MergeCommit mergeCommit) {
        if (mergeCommit.getAncestorSHA() == null) {
            /**
             * Some merge scenarios don't return an valid ancestor SHA this check prevents
             * unexpected crashes
             */
            return false
        }
        boolean containsMutuallyModifiedFiles = mergeCommit.getAncestorSHA() != mergeCommit.getLeftSHA() &&
                mergeCommit.getAncestorSHA() != mergeCommit.getRightSHA() &&
                mergeCommit.getLeftSHA() != mergeCommit.getRightSHA()

        if (!containsMutuallyModifiedFiles) {
            println 'removed ' + mergeCommit.getSHA()
        }

        return containsMutuallyModifiedFiles
    }
}
