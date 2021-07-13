package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
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
        mergeApproaches = [ 'Textual' ]
        for (TextualMergeStrategy strategy: strategies) {
            mergeApproaches.add(strategy.name())
        }

        mergeApproaches.add('Actual')

        // All merge tool runners
        mergeToolRunners = [ new Diff3Runner() ]
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