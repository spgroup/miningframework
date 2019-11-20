package services.S3MHandlersAnalysis.implementations

import main.interfaces.DataCollector
import main.project.MergeCommit
import main.project.Project
import services.S3MHandlersAnalysis.Handlers
import services.S3MHandlersAnalysis.datacollection.DataAnalyser
import services.S3MHandlersAnalysis.datacollection.MergeScenarioCollector
import services.S3MHandlersAnalysis.datacollection.S3MRunner
import services.S3MHandlersAnalysis.datacollection.SpreadsheetBuilder
import services.S3MHandlersAnalysis.util.MergeCommitSummary

import java.nio.file.Path

class MergesCollector implements DataCollector {
    // groovy -cp src src/main/app/MiningFramework.groovy -a b22d2fc334ece38945974c789654e8f56d812b02 -i services.S3MHandlersAnalysis.MiningModule projects.csv

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        S3MRunner.collectS3MResults(mergeScenarios, [Handlers.Renaming])
        println 'Collected S3M results'

        MergeCommitSummary summary = DataAnalyser.analyseScenarios(project, mergeCommit, mergeScenarios)
        println 'Summarized collected data'

        SpreadsheetBuilder.buildSpreadsheets(project, mergeCommit, summary)
        println 'Built spreadsheets'
    }

}
