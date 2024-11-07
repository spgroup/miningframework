package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

import java.nio.file.Path

class RunNormalizationOnScenarioFilesDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(RunNormalizationOnScenarioFilesDataCollector.class)

    private static final String JDIME_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"

    private List<String> filesToRunNormalization

    RunNormalizationOnScenarioFilesDataCollector(List<String> filesToRunNormalization) {
        this.filesToRunNormalization = filesToRunNormalization
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarioFiles = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)

        LOG.trace("Starting normalization of scenario files")
        scenarioFiles.parallelStream().forEach(scenarioFile -> this.filesToRunNormalization.parallelStream().forEach(fileToRunNormalization -> {
            def file = scenarioFile.resolve(fileToRunNormalization)
            LOG.trace("Starting normalization of ${file}")
            normalizeFileInPlace(file)
            LOG.trace("Finished normalization of ${file}")
        }))
        LOG.trace("Finished normalization of scenario files")
    }

    private static void normalizeFileInPlace(Path file) {
        def processBuilder = ProcessRunner.buildProcess(JDIME_BINARY_PATH,
                "./JDime",
                "-f",
                "--mode=structured",
                "--output=${file.toAbsolutePath().toString()}".toString(),
                file.toAbsolutePath().toString(),
                file.toAbsolutePath().toString(),
                file.toAbsolutePath().toString())

        def exitCode = ProcessRunner.startProcess(processBuilder).waitFor()
        if (exitCode != 0) {
            LOG.warn("File normalization failed with exit code ${exitCode}")
        }
    }
}
