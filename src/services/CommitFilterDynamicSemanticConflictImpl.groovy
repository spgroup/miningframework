package services
import main.interfaces.CommitFilter
import static com.xlson.groovycsv.CsvParser.parseCsv

import main.util.*
import main.project.*

class CommitFilterDynamicSemanticConflictImpl extends CommitFilterImpl {

    @Override
    public boolean applyFilter(Project project, MergeCommit mergeCommit) {
        boolean hasMergeConflict = MergeHelper.hasMergeConflict(project, mergeCommit)
        
        return !hasMergeConflict ? super.applyFilter(project, mergeCommit) : false

    }

}