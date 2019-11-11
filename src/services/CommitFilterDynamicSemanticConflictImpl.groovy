package services
import main.interfaces.CommitFilter
import static com.xlson.groovycsv.CsvParser.parseCsv

import main.util.*
import main.project.*

class CommitFilterDynamicSemanticConflictImpl extends CommitFilterImpl {

    @Override
    public boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return (!MergeHelper.hasMergeConflict(project, mergeCommit) || MergeHelper.checkForNotEmptyDiff(project, mergeCommit)) ? super.applyFilter(project, mergeCommit) : false
    }

}