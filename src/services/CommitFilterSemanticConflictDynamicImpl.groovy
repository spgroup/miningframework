package services
import main.interfaces.CommitFilter
import static com.xlson.groovycsv.CsvParser.parseCsv

import main.util.*
import main.project.*

class CommitFilterSemanticConflictDynamicImpl extends CommitFilterImpl {

    @Override
    public boolean applyFilter(Project project, MergeCommit mergeCommit) {
        if (!MergeHelper.hasMergeConflict(project, mergeCommit)){
            return super.applyFilter(project, mergeCommit)
        } 
        return false
    }

}