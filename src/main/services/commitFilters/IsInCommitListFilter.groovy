package services.commitFilters

import interfaces.CommitFilter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project

import static com.xlson.groovycsv.CsvParser.parseCsv

/**
 * @requires: that a commits.csv file exists on the project's root, otherwise it will always return true
 * @provides: returns true if the passed mergeCommit SHA is in the commits.csv file
 */
class IsInCommitListFilter implements CommitFilter {
    private static Logger LOG = LogManager.getLogger(IsInCommitListFilter.class)

    List<String> commitList

    IsInCommitListFilter() {
        File commitsFile = new File("./commits.csv")
        this.commitList = commitsFile.exists() ? parseCommitList(commitsFile) : new ArrayList<>()
    }

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitList.isEmpty() || commitList.contains(mergeCommit.getSHA())
    }

    private List<String> parseCommitList(File commitsFile) {
        ArrayList<String> commitList = new ArrayList<String>()
        def iterator = parseCsv(commitsFile.getText())

        for (line in iterator) {
            LOG.trace("Registering ${line["commitSHA"]} as a valid commit SHA in list")
            commitList.add(line["commitSHA"])
        }

        return commitList
    }
}
