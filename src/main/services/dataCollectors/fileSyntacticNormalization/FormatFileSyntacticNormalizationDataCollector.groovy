package services.dataCollectors.fileSyntacticNormalization


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import util.ProcessRunner

import java.nio.file.Path

class FormatFileSyntacticNormalizationDataCollector extends BaseFileSyntacticNormalizationDataCollector {
    private static Logger LOG = LogManager.getLogger(FormatFileSyntacticNormalizationDataCollector.class)

    private static final String FORMAT_PATH = "${System.getProperty("user.dir")}/dependencies/format"

    FormatFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        super(inputFile, outputFile)
    }

    @Override
    protected boolean runNormalizationOnFile(Path inputFile, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"))
        processBuilder.command().add(FORMAT_PATH)
        processBuilder.command().add(inputFile.toString())
        processBuilder.redirectOutput(outputFile.toFile())

        LOG.trace("Calling format with command \"${processBuilder.command().join(' ')}\"")

        def exitCode = ProcessRunner.startProcess(processBuilder).waitFor()
        return exitCode != 0
    }
}
