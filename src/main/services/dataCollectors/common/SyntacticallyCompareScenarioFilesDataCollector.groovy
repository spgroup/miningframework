package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.FileSyntacticDiff
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.CsvUtils

import java.nio.file.Files
import java.nio.file.Paths

class SyntacticallyCompareScenarioFilesDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(SyntacticallyCompareScenarioFilesDataCollector.class)

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
                    def areFilesSyntacticallyEquivalent = FileSyntacticDiff.areFilesSyntacticallyEquivalent(fileA, fileB)
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
}
