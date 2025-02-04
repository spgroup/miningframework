package services.dataCollectors.fileSyntacticNormalization


import util.ProcessRunner

import java.nio.file.Path
import java.util.concurrent.TimeUnit

class JDimeFileSyntacticNormalizationDataCollector extends BaseFileSyntacticNormalizationDataCollector {
    private static final String JDIME_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"

    JDimeFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        super(inputFile, outputFile)
    }

    @Override
    boolean runNormalizationOnFile(Path inputFile, Path outputFile) {
        def processBuilder = ProcessRunner.buildProcess(JDIME_BINARY_PATH,
                "./JDime",
                "-f",
                "--mode=structured",
                "--output=${outputFile.toAbsolutePath().toString()}".toString(),
                inputFile.toAbsolutePath().toString(),
                inputFile.toAbsolutePath().toString(),
                inputFile.toAbsolutePath().toString())

        def output = ProcessRunner.startProcess(processBuilder)
        def hasCompleted = output.waitFor(1, TimeUnit.HOURS)
        return hasCompleted && output.exitValue() != 0
    }
}
