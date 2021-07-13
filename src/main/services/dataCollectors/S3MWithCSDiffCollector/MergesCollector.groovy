package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MWithCSDiffCollector.mergeToolRunners.*
import services.util.MergeToolRunner
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    static final List<TextualMergeStrategy> strategies
    static final List<String> mergeApproaches

    private static final List<MergeToolRunner> mergeToolRunners

    static {
        // Textual merge strategies used to run S3M
        strategies = [ TextualMergeStrategy.CSDiff ]

        // All merge approaches
        mergeApproaches = [ 'Diff3', 'GitMergeFile' ]
        for (TextualMergeStrategy strategy: strategies) {
            String key = "S3M${strategy.name()}"
            mergeApproaches.add(key)
        }

        mergeApproaches.add('Actual')

        // All merge tool runners
        mergeToolRunners = [ new Diff3Runner(), new GitMergeFileRunner() ]
        for (TextualMergeStrategy strategy: strategies) {
            mergeToolRunners.add(new S3MRunner(strategy))
        }
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = FilesQuadruplesCollector.collectFilesQuadruples(project, mergeCommit)
        println 'Collected files quadruples'

        for (MergeToolRunner runner: mergeToolRunners) {
            runner.collectResults(filesQuadruplePaths)
        }

        println "Collected merge results"

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

}