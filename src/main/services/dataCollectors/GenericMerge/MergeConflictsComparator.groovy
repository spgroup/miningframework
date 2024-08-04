package services.dataCollectors.GenericMerge


import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import services.util.MergeConflict
import util.CsvUtils

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

class MergeConflictsComparator implements DataCollector {
    private static Logger LOG = LogManager.getLogger(MergeConflictsComparator.class)

    private static final String GENERIC_MERGE_FILE_NAME = "merge.generic_merge.java"
    private static final String JDIME_FILE_NAME = "merge.jdime.java"

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        LOG.trace("Starting execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")

        def conflictsComparisons = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
                .parallelStream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .filter(MergeConflictsComparator::hasResponseFromBothTools)
                .filter(MergeConflictsComparator::hasConflictsInBothTools)
                .map(MergeConflictsComparator::extractConflictsFromFiles)
                .flatMap(MergeConflictsComparator::compareMergeConflicts(project, mergeCommit))
                .map(CsvUtils::toCsvRepresentation)

        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_MERGE_CONFLICTS)
        def fileContent = conflictsComparisons.collect(CsvUtils.asLines())
        if (fileContent.isBlank() || fileContent.isEmpty()) {
            LOG.trace("Finished execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()} without conflicts")
            return
        }
        reportFile << fileContent << "\n"

        LOG.trace("Finished execution of Merge Conflicts Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")
    }

    private static boolean hasResponseFromBothTools(Path scenario) {
        LOG.trace("Checking if has response from both tools for ${scenario.toString()}")
        return Files.exists(scenario.resolve(GENERIC_MERGE_FILE_NAME)) && Files.exists(scenario.resolve(JDIME_FILE_NAME))
    }

    private static boolean hasConflictsInBothTools(Path scenario) {
        LOG.trace("Checking if both files have conflicts")
        return MergeConflict.getConflictsNumber(scenario.resolve(GENERIC_MERGE_FILE_NAME)) > 0 && MergeConflict.getConflictsNumber(scenario.resolve(JDIME_FILE_NAME)) > 0
    }

    private static Tuple3<Path, Set<MergeConflict>, Set<MergeConflict>> extractConflictsFromFiles(Path scenario) {
        LOG.trace("Extracting conflicts from files in ${scenario.toString()}")
        return new Tuple3(scenario, MergeConflict.extractMergeConflicts(scenario.resolve(GENERIC_MERGE_FILE_NAME)),
                MergeConflict.extractMergeConflicts(scenario.resolve(JDIME_FILE_NAME)))
    }

    private static Closure<Stream<List<String>>> compareMergeConflicts(Project project, MergeCommit mergeCommit) {
        return (Tuple3<Path, Set<MergeConflict>, Set<MergeConflict>> mergeConflicts) -> {
            def scenario = mergeConflicts.getV1()
            def genericMergeConflicts = mergeConflicts.getV2()
            def jDimeConflicts = mergeConflicts.getV3()

            return genericMergeConflicts.withIndex().parallelStream().flatMap(genericMergeTuple -> {
                def genericMergeConflict = genericMergeTuple.getV1()
                def i = genericMergeTuple.getV2()

                return jDimeConflicts.withIndex().parallelStream().map(jDimeTuple -> {
                    def jDimeConflict = jDimeTuple.getV1()
                    def j = jDimeTuple.getV2()

                    LOG.trace("Checking if conflicts generic_merge_conflict_${i} and jdime_conflict_${j} are equal")

                    return [project.getName(),
                            mergeCommit.getSHA(),
                            scenario.toString(),
                            "generic_merge_conflict_${i}",
                            "jdime_conflict_${j}",
                            genericMergeConflict.equals(jDimeConflict).toString()]
                })
            })
        }
    }
}
