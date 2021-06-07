package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    public static final List<TextualMergeStrategy> strategies = [
        TextualMergeStrategy.Diff3,
        TextualMergeStrategy.CSDiff,
        TextualMergeStrategy.CSDiffAndDiff3,
        TextualMergeStrategy.ConsecutiveLines
    ]

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

}