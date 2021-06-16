package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.util.MergeToolRunner
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    static final List<TextualMergeStrategy> strategies
    static final List<String> mergeApproaches

    private static final List<MergeToolRunner> mergeToolRunners

    static {
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

        mergeToolRunners = [
            new Diff3Runner(),
            new S3MRunner()
        ]
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected files quadruples'

        for (MergeToolRunner runner: mergeToolRunners) {
            runner.collectResults(filesQuadruplePaths)
        }

        println 'Collected merge results'

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

}