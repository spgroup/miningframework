package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project

import static com.xlson.groovycsv.CsvParser.parseCsv

class IsInCommitListFilter implements CommitFilter {
    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        boolean result = true
        File commitsFile = new File("./commits.csv")

        if (commitsFile.exists()) {
            List<String> commitList = parseCommitList(commitsFile)

            result = isInCommitList(commitList, mergeCommit)
        }
        return result
    }

    private List<String> parseCommitList (File commitsFile) {
        ArrayList<String> commitList = new ArrayList<String>()
        def iterator = parseCsv(commitsFile.getText())

        for (line in iterator) {
            commitList.add(line["commitSHA"])
        }

        return commitList
    }

    private boolean isInCommitList (List commitList, MergeCommit mergeCommit) {
        for (commit in commitList) {
            if (mergeCommit.getSHA() == commit) {
                return true;
            }
        }
        return false;
    }
}
