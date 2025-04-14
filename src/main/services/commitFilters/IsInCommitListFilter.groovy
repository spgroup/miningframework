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
        this.commitList = initializeCommitList(new File("./commits.csv"))
    }

    private static List<String> initializeCommitList(File commitsFile) {
        if (!commitsFile.exists()) {
            LOG.trace("Skipping initialization because the commits.csv file do not exist")
            return new ArrayList<>()
        }

        ArrayList<String> commitList = new ArrayList<String>()
        def iterator = parseCsv(commitsFile.getText())

        for (line in iterator) {
            LOG.trace("Registering ${line["commitSHA"]} as a valid commit SHA in list")
            commitList.add(line["commitSHA"])
        }

        return commitList
    }

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitList.isEmpty() || commitList.contains(mergeCommit.getSHA())
    }
}
