package services.dataCollectors.common

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.util.MergeConflict
import util.CsvUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

class CompareScenarioMergeConflictsDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(CompareScenarioMergeConflictsDataCollector.class)

    private static final REPORT_DIRECTORY = "${System.getProperty("user.dir")}/output/reports/conflicts-comparison"

    private String fileA
    private String fileB

    CompareScenarioMergeConflictsDataCollector(String fileA, String fileB) {
        this.fileA = fileA
        this.fileB = fileB
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        LOG.trace("Starting execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")

        def conflictsComparisons = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)
                .parallelStream()
                .filter(this::hasResponseFromBothTools)
                .filter(this::hasConflictsInBothTools)
                .map(this::extractConflictsFromFiles)
                .flatMap(CompareScenarioMergeConflictsDataCollector::compareMergeConflicts(project, mergeCommit))
                .map(CsvUtils::toCsvRepresentation)
                .collect(Collectors.toList())

        if (conflictsComparisons.isEmpty()) {
            LOG.trace("Finished execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()} without conflicts")
            return
        }

        LOG.trace("Finished execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")
        writeReportToFile(getReportFileName(), conflictsComparisons.collect())
    }

    protected static synchronized writeReportToFile(Path filePath, List<String> lines) {
        def reportFile = filePath.toFile()
        Files.createDirectories(filePath.getParent())
        reportFile.createNewFile()
        reportFile << lines.stream().collect(CsvUtils.asLines()) << System.lineSeparator()
    }

    private Path getReportFileName() {
        return Paths.get("${REPORT_DIRECTORY}/${fileA.replace('.', "_")}-${fileB.replace('.', "_")}.csv")
    }

    private boolean hasResponseFromBothTools(Path scenario) {
        LOG.trace("Checking if has response from both tools for ${scenario.toString()}")
        return Files.exists(scenario.resolve(fileA)) && Files.exists(scenario.resolve(fileB))
    }

    private boolean hasConflictsInBothTools(Path scenario) {
        LOG.trace("Checking if both files have conflicts")
        return MergeConflict.getConflictsNumber(scenario.resolve(fileA)) > 0 && MergeConflict.getConflictsNumber(scenario.resolve(fileB)) > 0
    }

    private Tuple3<Path, Set<MergeConflict>, Set<MergeConflict>> extractConflictsFromFiles(Path scenario) {
        LOG.trace("Extracting conflicts from files in ${scenario.toString()}")
        return new Tuple3(scenario, MergeConflict.extractMergeConflicts(scenario.resolve(fileA)),
                MergeConflict.extractMergeConflicts(scenario.resolve(fileB)))
    }

    private static Closure<Stream<List<String>>> compareMergeConflicts(Project project, MergeCommit mergeCommit) {
        return (Tuple3<Path, Set<MergeConflict>, Set<MergeConflict>> mergeConflicts) -> {
            def scenario = mergeConflicts.getV1()
            def fileAMergeConflicts = mergeConflicts.getV2()
            def fileBMergeConflicts = mergeConflicts.getV3()

            return fileAMergeConflicts.withIndex().parallelStream().flatMap(fileATuple -> {
                def fileAConflict = fileATuple.getV1()
                def i = fileATuple.getV2()

                return fileBMergeConflicts.withIndex().parallelStream().map(fileBTuple -> {
                    def fileBConflict = fileBTuple.getV1()
                    def j = fileBTuple.getV2()

                    return [project.getName(),
                            mergeCommit.getSHA(),
                            scenario.toString(),
                            "file_a_conflict_${i}",
                            "file_b_conflict_${j}",
                            fileAConflict.equalsOrSubstring(fileBConflict).toString()]
                })
            })
        }
    }
}
