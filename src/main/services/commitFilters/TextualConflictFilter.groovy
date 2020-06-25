package services.commitFilters

import interfaces.CommitFilter

import project.MergeCommit
import project.Project
import util.MergeHelper

class TextualConflictFilter implements CommitFilter {
    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {

        boolean hasMergeConflict = MergeHelper.hasMergeConflict(project, mergeCommit)

        if (hasMergeConflict) {
            println project.getName() + " " + mergeCommit.getSHA() + " filtered because of a textual merge conflict"
        }

        return !hasMergeConflict
    }
}
