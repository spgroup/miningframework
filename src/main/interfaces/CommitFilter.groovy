package main.interfaces

import main.project.*

public interface CommitFilter {

    public boolean applyFilter(Project project, MergeCommit mergeCommit)

}