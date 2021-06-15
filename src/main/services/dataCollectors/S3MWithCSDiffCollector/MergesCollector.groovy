package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    static final List<TextualMergeStrategy> strategies = [
        TextualMergeStrategy.Diff3,
        TextualMergeStrategy.CSDiff,
        TextualMergeStrategy.ConsecutiveLines,
        TextualMergeStrategy.CSDiffAndDiff3
    ]

    static final List<String> mergeApproaches = getMergeApproaches()

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected files quadruples'

        S3MRunner.collectS3MResults(filesQuadruplePaths, strategies)
        println 'Collected S3M results'

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

    private static List<String> getMergeApproaches() {
        List<String> mergeApproaches = [ 'Textual' ]
        for (TextualMergeStrategy strategy: strategies) {
            mergeApproaches.add(strategy.name())
        }

        mergeApproaches.add('Actual')
        return mergeApproaches
    }

}