package services.dataCollectors.S3MWithCSDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.TextualMergeStrategy

import java.nio.file.Path

class S3MWithCSDiffCollector implements DataCollector {

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        List<TextualMergeStrategy> textualMergeStrategies = [
            TextualMergeStrategy.Diff3,
            TextualMergeStrategy.CSDiff
        ]

        S3MRunner.collectS3MResults(mergeScenarios, textualMergeStrategies)
        println 'Collected S3M results'
    }

}