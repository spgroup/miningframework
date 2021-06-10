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
        TextualMergeStrategy.CSDiff
    ]

    static final List<String> mergeApproaches = getMergeApproaches()

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        S3MRunner.collectS3MResults(mergeScenarios, strategies)
        println 'Collected S3M results'

        MergeCommitSummary summary = DataAnalyser.analyseScenarios(mergeScenarios)
        println 'Summarized collected data'

        SpreadsheetBuilder.buildSpreadsheets(project, mergeCommit, summary)
        println 'Built spreadsheets'
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