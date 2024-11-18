package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

import java.nio.file.Path

class RunNormalizationOnScenarioFilesWithSporkDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(RunNormalizationOnScenarioFilesWithSporkDataCollector.class)

    private static final String SPORK_JAR_PATH = "${System.getProperty("user.dir")}/dependencies/spork.jar"

    private String inputFileName
    private String outputFileName

    RunNormalizationOnScenarioFilesWithSporkDataCollector(String inputFileName, String outputFileName) {
        this.inputFileName = inputFileName
        this.outputFileName = outputFileName
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarioFiles = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)

        LOG.trace("Starting normalization of scenario files")
        scenarioFiles.parallelStream().forEach(scenarioFile -> {
            normalizeFile(scenarioFile)
        })
        LOG.trace("Finished normalization of scenario files")
    }

    private void normalizeFile(Path scenarioDirectory) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))
        processBuilder.command().addAll(getBuildParameters(scenarioDirectory))

        LOG.trace("Calling spork with command \"${processBuilder.command().join(' ')}\"")
        def exitCode = ProcessRunner.startProcess(processBuilder).waitFor()
        if (exitCode != 0) {
            LOG.warn("File normalization failed with exit code ${exitCode}")
        }
    }

    private List<String> getBuildParameters(Path scenarioDirectory) {
        def list = new ArrayList<String>()
        list.add("java")
        list.add("-jar")
        list.add(SPORK_JAR_PATH)
        list.add(scenarioDirectory.resolve(inputFileName).toString())
        list.add(scenarioDirectory.resolve(inputFileName).toString())
        list.add(scenarioDirectory.resolve(inputFileName).toString())
        list.add("--output=${scenarioDirectory.resolve(outputFileName).toString()}".toString())
        return list
    }
}
