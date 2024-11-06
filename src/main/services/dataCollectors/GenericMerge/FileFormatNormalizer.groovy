package services.dataCollectors.GenericMerge

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import util.ProcessRunner

import java.nio.file.Path

class FileFormatNormalizer {
    private static Logger LOG = LogManager.getLogger(FileFormatNormalizer.class)

    private static final String JDIME_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/jdime/install/JDime/bin"

    static void normalizeFileInPlace(Path file) {
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
