package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.CsvUtils
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SyntacticallyCompareScenarioFilesDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(SyntacticallyCompareScenarioFilesDataCollector.class)

    private static String LAST_MERGE_BINARY_PATH = "${System.getProperty("user.dir")}/dependencies/last-merge"

    private static final REPORT_DIRECTORY = "${System.getProperty("user.dir")}/output/reports/syntactic-comparison"

    private String fileA
    private String fileB

    SyntacticallyCompareScenarioFilesDataCollector(String fileA, String fileB) {
        this.fileA = fileA
        this.fileB = fileB
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def results = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)
                .parallelStream()
                .map(file -> {
                    def fileA = file.resolve(this.fileA)
                    def fileB = file.resolve(this.fileB)
                    LOG.trace("Starting syntactic comparison between ${fileA} and ${fileB}")
                    def areFilesSyntacticallyEquivalent = areFilesSyntacticallyEquivalent(fileA, fileB)
                    return [project.getName(), mergeCommit.getSHA(), file, fileA, fileB, areFilesSyntacticallyEquivalent]
                })
                .map(CsvUtils::toCsvRepresentation)

        def reportFile = new File(getReportFileName())
        Files.createDirectories(Paths.get(REPORT_DIRECTORY))
        reportFile.createNewFile()
        reportFile << results.collect(CsvUtils.asLines()) << System.lineSeparator()
    }

    private String getReportFileName() {
        return "${REPORT_DIRECTORY}/${fileA.replace('.', "_")}-${fileB.replace('.', "_")}.csv"
    }

    private static boolean areFilesSyntacticallyEquivalent(Path fileA, Path fileB) {
        if (!Files.exists(fileA)) {
            LOG.trace("Early returning because the file ${fileA} do not exist")
            return false
        }
        if (!Files.exists(fileB)) {
            LOG.trace("Early returning because the file ${fileB} do not exist")
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
