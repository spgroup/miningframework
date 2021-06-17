package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.util.MergeToolRunner
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    private static final List<MergeToolRunner> mergeToolRunners

    static final List<TextualMergeStrategy> strategies
    static final List<String> mergeApproaches

    static {
        mergeToolRunners = [
            new Diff3Runner('Diff3'),
            new S3MRunner('S3M')
        ]

        // Textual merge strategies used to run S3M
        strategies = [
            TextualMergeStrategy.Diff3,
            TextualMergeStrategy.CSDiff,
            TextualMergeStrategy.ConsecutiveLines,
            TextualMergeStrategy.CSDiffAndDiff3
        ]

        // All merge approaches
        mergeApproaches = [ 'Textual' ]
        for (TextualMergeStrategy strategy: strategies) {
            mergeApproaches.add(strategy.name())
        }

        mergeApproaches.add('Actual')
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected files quadruples'

        for (MergeToolRunner runner: mergeToolRunners) {
            runner.collectResults(filesQuadruplePaths)
            println "Collected ${runner.getMergeToolName()} results"
        }

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

}