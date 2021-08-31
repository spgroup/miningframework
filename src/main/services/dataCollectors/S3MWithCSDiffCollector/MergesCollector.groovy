package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MWithCSDiffCollector.mergeToolRunners.*
import services.util.MergeToolRunner
import util.TextualMergeStrategy

import java.nio.file.Path

class MergesCollector implements DataCollector {

    private static final Map<String, MergeToolRunner> approachToRunner

    static final List<TextualMergeStrategy> strategies
    static final List<String> mergeApproaches

    static {
        // Merge tool runner associated to each merge approach
        approachToRunner = [
            'Diff3': new Diff3Runner(),
            'GitMergeFile': new GitMergeFileRunner()
        ]

        for (TextualMergeStrategy strategy: TextualMergeStrategy.values()) {
            String key = "S3M${strategy.name()}"
            approachToRunner[key] = new S3MRunner(strategy)
        }

        // Textual merge strategies used to run S3M
        strategies = [ TextualMergeStrategy.CSDiff, TextualMergeStrategy.Diff3 ]

        // All merge approaches
        mergeApproaches = [ 'Diff3' ]
        for (TextualMergeStrategy strategy: strategies) {
            String key = "S3M${strategy.name()}"
            mergeApproaches.add(key)
        }

        mergeApproaches.add('Actual')
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = FilesQuadruplesCollector.collectFilesQuadruples(project, mergeCommit)
        println 'Collected files quadruples'

        for (String approach: mergeApproaches) {
            if (approach != 'Actual') {
                MergeToolRunner runner = approachToRunner[approach]
                runner.collectResults(filesQuadruplePaths)
            }
        }

        println "Collected merge results"

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

}