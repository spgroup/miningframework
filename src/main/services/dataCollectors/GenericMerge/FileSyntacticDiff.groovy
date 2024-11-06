package services.dataCollectors.GenericMerge

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path

class FileSyntacticDiff {
    private static Logger LOG = LogManager.getLogger(FileSyntacticDiff.class)

    private static String LAST_MERGE_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/last-merge"

    static boolean areFilesSyntacticallyEquivalent(Path fileA, Path fileB) {
        if (!Files.exists(fileA) || !Files.exists(fileB)) {
            LOG.trace("Early returning because one of the files ${} do not exist")
            return false
        }

        def process = ProcessRunner.buildProcess("./")

        def list = new ArrayList<String>()
        list.add(LAST_MERGE_BINARY_PATH)
        list.add("diff")
        list.add("--left-path=${fileA.toAbsolutePath().toString()}".toString())
        list.add("--right-path=${fileB.toAbsolutePath().toString()}".toString())
        list.add("--language=java")
        process.command().addAll(list)

        def output = ProcessRunner.startProcess(process)
        output.waitFor()

        if (output.exitValue() > 1) {
            LOG.warn("Error while running comparison between ${fileA.toString()} and ${fileB.toString()}: ${output.getInputStream().readLines()}")
        }

        return output.exitValue() == 0
    }
}
