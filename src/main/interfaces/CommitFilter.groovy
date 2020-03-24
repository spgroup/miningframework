package interfaces

import project.*

public interface CommitFilter {

    public boolean applyFilter(Project project, MergeCommit mergeCommit)

}