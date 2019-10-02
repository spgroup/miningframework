package services

import main.interfaces.DataCollector
import main.project.MergeCommit
import main.project.Project
import main.util.FileManager
import main.util.ProcessRunner
import main.util.MergeHelper

import java.util.regex.Matcher
import java.util.regex.Pattern

import static main.app.MiningFramework.arguments

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
