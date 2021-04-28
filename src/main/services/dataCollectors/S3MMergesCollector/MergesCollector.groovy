package services.dataCollectors.S3MMergesCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.util.MergeCommitSummary
import util.Handlers

import java.nio.file.Path

class MergesCollector implements DataCollector {
    // groovy -cp src src/main/app/MiningFramework.groovy -a b22d2fc334ece38945974c789654e8f56d812b02 -i services.S3MHandlersAnalysis.MiningModule projects.csv

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        .collectS3MResults(mergeScenarios, [Handlers.Renaming])
        println 'Collected S3M results'

        MergeCommitSummary summary = DataAnalyser.analyseScenarios(project, mergeCommit, mergeScenarios)
        println 'Summarized collected data'

        SpreadsheetBuilder.buildSpreadsheets(project, mergeCommit, summary)
        println 'Built spreadsheets'
    }

}
