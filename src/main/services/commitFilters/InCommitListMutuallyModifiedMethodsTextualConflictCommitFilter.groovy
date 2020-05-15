package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project


/**
 *  Combines the output of IsInCommitListFilter, MutuallyModifiedMethodsCommitFilter
 *  and TextualConflictFilter with and clauses
 */
class InCommitListMutuallyModifiedMethodsTextualConflictCommitFilter implements CommitFilter {
    private CommitFilter commitListFilter = new IsInCommitListFilter();
    private CommitFilter mutuallyModifiedFilter = new MutuallyModifiedMethodsCommitFilter();
    private CommitFilter textualConflictFilter = new TextualConflictFilter();

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitListFilter.applyFilter(project, mergeCommit)
                && mutuallyModifiedFilter.applyFilter(project, mergeCommit)
                && textualConflictFilter.applyFilter(project, mergeCommit);
    }
}
