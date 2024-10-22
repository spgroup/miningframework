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
class IsInProjectCommitListFilter implements CommitFilter {
    private static Logger LOG = LogManager.getLogger(IsInProjectCommitListFilter.class)

    Map<String, Set<String>> commitList

    IsInProjectCommitListFilter() {
        this.commitList = initializeCommitList(new File("./commits.csv"))
    }

    private static Map<String, Set<String>> initializeCommitList(File commitsFile) {
        if (!commitsFile.exists()) {
            LOG.trace("Skipping initialization because the commits.csv file do not exist")
            return new HashMap<String, Set<String>>()
        }

        def commitList = new HashMap<String, Set<String>>()
        def iterator = parseCsv(commitsFile.getText())

        for (line in iterator) {
            LOG.trace("Registering ${line["commitSHA"]} as a valid commit SHA in list")
            String project = line['project']
            String commitSHA = line['commitSHA']
            if (commitList.containsKey(project)) {
                commitList.get(project).add(commitSHA)
            } else {
                commitList.put(project, new HashSet<>([commitSHA]))
            }
        }

        return commitList
    }

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitList.isEmpty() || commitList.containsKey(project.getName()) && commitList.get(project.getName()).contains(mergeCommit.getSHA())
    }
}
