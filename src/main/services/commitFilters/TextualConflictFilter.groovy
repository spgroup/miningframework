package services.commitFilters

import interfaces.CommitFilter

import project.MergeCommit
import project.Project
import util.MergeHelper

class TextualConflictFilter implements CommitFilter {
    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return !MergeHelper.hasMergeConflict(project, mergeCommit)
    }
}
