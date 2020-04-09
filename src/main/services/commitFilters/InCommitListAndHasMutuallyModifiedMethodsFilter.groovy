package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project


/**
 *  Combines the output of IsInCommitListFilter and MutuallyModifiedMethodsCommitFilter with a and clause
 */
class InCommitListAndHasMutuallyModifiedMethodsFilter implements CommitFilter {
    private CommitFilter commitListFilter = new IsInCommitListFilter();
    private CommitFilter mutuallyModifiedFilter = new MutuallyModifiedMethodsCommitFilter();

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitListFilter.applyFilter(project, mergeCommit)
                && mutuallyModifiedFilter.applyFilter(project, mergeCommit)
    }
}
