package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.FileFormatNormalizer
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector

class RunNormalizationOnScenarioFilesDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(RunNormalizationOnScenarioFilesDataCollector.class)

    private List<String> filesToRunNormalization

    RunNormalizationOnScenarioFilesDataCollector(List<String> filesToRunNormalization) {
        this.filesToRunNormalization = filesToRunNormalization
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarioFiles = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)

        scenarioFiles.parallelStream().forEach(scenarioFile -> {
            this.filesToRunNormalization.parallelStream().forEach(fileToRunNormalization -> {
                LOG.trace("Starting to run normalization on file ${scenarioFile.resolve(fileToRunNormalization)}")
                FileFormatNormalizer.normalizeFileInPlace(scenarioFile.resolve(fileToRunNormalization))
                LOG.trace("Finished to run normalization on file ${scenarioFile.resolve(fileToRunNormalization)}")
            })
        })
    }
}
