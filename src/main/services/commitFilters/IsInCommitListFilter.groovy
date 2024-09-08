package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project

import static com.xlson.groovycsv.CsvParser.parseCsv

/**
 * @requires: that a commits.csv file exists on the project's root, otherwise it will always return true
 * @provides: returns true if the passed mergeCommit SHA is in the commits.csv file
 */
class IsInCommitListFilter implements CommitFilter {

    List<String> commitList

    IsInCommitListFilter() {
        File commitsFile = new File("./commits.csv")
        this.commitList = parseCommitList(commitsFile)
    }

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return isInCommitList(mergeCommit)
    }

    private List<String> parseCommitList (File commitsFile) {
        ArrayList<String> commitList = new ArrayList<String>()
        def iterator = parseCsv(commitsFile.getText())

        for (line in iterator) {
            commitList.add(line["commitSHA"])
        }

        return commitList
    }

    private boolean isInCommitList (MergeCommit mergeCommit) {
        for (commit in commitList) {
            if (mergeCommit.getSHA() == commit) {
                return true;
            }
        }
        return false;
    }
}
