package services.dataCollectors.fileSyntacticNormalization


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import util.ProcessRunner

import java.nio.file.Path

class SporkFileSyntacticNormalizationDataCollector extends BaseFileSyntacticNormalizationDataCollector {
    private static Logger LOG = LogManager.getLogger(SporkFileSyntacticNormalizationDataCollector.class)

    private static final String SPORK_JAR_PATH = "${System.getProperty("user.dir")}/dependencies/spork.jar"

    SporkFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        super(inputFile, outputFile)
    }

    @Override
    protected boolean runNormalizationOnFile(Path inputFile, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))
        processBuilder.command().addAll(getBuildParameters(inputFile, outputFile))

        LOG.trace("Calling spork with command \"${processBuilder.command().join(' ')}\"")
        def process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        return true
    }

    private static List<String> getBuildParameters(Path inputFile, Path outputFile) {
        def list = new ArrayList<String>()
        list.add("java")
        list.add("-jar")
        list.add(SPORK_JAR_PATH)
        list.add(inputFile.toString())
        list.add(inputFile.toString())
        list.add(inputFile.toString())
        list.add("--output=${outputFile.toString()}".toString())
        return list
    }
}
