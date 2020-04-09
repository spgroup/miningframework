package services.dataCollectors

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.MergeHelper

import static app.MiningFramework.arguments

/**
 * @produces: a [outputPath]/mergeconflicts/results.csv file with the following format:
 * project;merge commit;textual conflict
 * [projectName];[merge sha];(true | false)
 */
class MergeConflictCollector implements DataCollector {

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        setUp()

        collectMergeData(project, mergeCommit)

        println "${project.getName()} - Merge Data collection finished!"
    }

    void setUp() {
        File resultsFile = getResultsFile()
        if (!resultsFile.exists()) {
            resultsFile.getParentFile().mkdirs()

            resultsFile << "project;merge commit;textual conflict\n"
        }
    }

    void collectMergeData (Project project, MergeCommit mergeCommit) {
        boolean hasMergeConflict = MergeHelper.hasMergeConflict(project, mergeCommit)

        File resultsFile = getResultsFile()

        resultsFile << "${project.getName()};${mergeCommit.getSHA()};${hasMergeConflict}\n"
    }

    File getResultsFile () {
        String outputPath = arguments.getOutputPath()

        return new File("${outputPath}/mergeconflicts/results.csv")
    }
}
