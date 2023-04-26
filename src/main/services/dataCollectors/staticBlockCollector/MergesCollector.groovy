package services.dataCollectors.staticBlockCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project



import java.nio.file.Path

class MergesCollector implements DataCollector {

    private static final Map<String, TriplaFilesRunner> approachToRunner

    static final List<String> strategies
    static final List<String> mergeApproaches

    static {
        // Tripla runner associated to each merge approach
        approachToRunner = [
            'SimpleInitializationBlockHandler': new SimpleInitializationBlockHandlerRunner(),
            'InsertionLevelInitializationBlockHandler': new InsertionLevelInitializationBlockHandlerRunner(),
            'GitMergeFile': new GitMergeFileRunner()
        ]

        // Textual merge algoritms used to run S3M
        strategies = [ 'InsertionLevelInitializationBlockHandler', 'SimpleInitializationBlockHandler','GitMergeFile']

        // All merge approaches
            mergeApproaches = [ 'InsertionLevelInitializationBlockHandler', 'SimpleInitializationBlockHandler','GitMergeFile'  ]
    /*    for (String strategy: strategies) {
            String key = "S3M${strategy}"
            mergeApproaches.add(key)
        }*/
        mergeApproaches.add('Actual')
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> filesQuadruplePaths = FilesQuadruplesCollector.collectFilesQuadruples(project, mergeCommit)
        println 'Collected files quadruples'

        for (String approach: mergeApproaches) {
            if (approach != 'Actual') {
                TriplaFilesRunner runner = approachToRunner[approach]
                runner.collectResults(project, mergeCommit, filesQuadruplePaths)

            }
        }

        println "Collected merge results"

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(filesQuadruplePaths)
        println 'Summarized collected data'


        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'


        StaticBlockManagerFiles staticBlockManagerFiles = new StaticBlockManagerFiles(filesQuadruplePaths)

    }

}