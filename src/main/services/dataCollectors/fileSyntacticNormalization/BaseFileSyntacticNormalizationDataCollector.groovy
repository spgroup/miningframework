package services.dataCollectors.fileSyntacticNormalization

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector

import java.nio.file.Path

abstract class BaseFileSyntacticNormalizationDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(BaseFileSyntacticNormalizationDataCollector.class)

    protected String inputFile
    protected String outputFile

    BaseFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        this.inputFile = inputFile
        this.outputFile = outputFile
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def files = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)
        files.parallelStream().forEach(file -> {
            LOG.debug("Starting to run file normalization in file ${inputFile}")
            def isSuccess = runNormalizationOnFile(file.resolve(inputFile), file.resolve(outputFile))
            if (!isSuccess) {
                LOG.debug("Failed to run file normalization in file ${inputFile}")
            }
            LOG.debug("Finished to run file normalization in file ${inputFile}")
        })
    }

    protected abstract boolean runNormalizationOnFile(Path inputFile, Path outputFile);
}
