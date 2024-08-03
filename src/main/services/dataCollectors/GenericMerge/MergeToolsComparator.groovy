package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import util.CsvUtils

import java.nio.file.Path

class MergeToolsComparator implements DataCollector {
    private static Logger LOG = LogManager.getLogger(MergeToolsComparator.class)

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        LOG.trace("Starting execution of Merge Tools Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")

        def results = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
                .parallelStream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .map(scenario -> checkIfOutputsAreEquivalent(project, mergeCommit, scenario))
                .map(CsvUtils::toCsvRepresentation)

        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_FILES_EQUIVALENT)
        reportFile << results.collect(CsvUtils.asLines()) << "\n"

        LOG.trace("Finished execution of Merge Tools Comparator on project ${project.getName()} and merge commit ${mergeCommit.getSHA()}")
    }

    private static List<String> checkIfOutputsAreEquivalent(Project project, MergeCommit mergeCommit, Path scenario) {
        LOG.trace("Starting to check if output for ${scenario.toString()} are equivalents")

        def genericMergePath = scenario.resolve("merge.generic_merge.java")
        def jDimePath = scenario.resolve("merge.jdime.java")

        def result = [project.getName(),
                      mergeCommit.getSHA(),
                      scenario.toString(),
                      FileSyntacticDiff.areFilesSyntacticallyEquivalent(genericMergePath, jDimePath).toString()]

        LOG.trace("Finished checking if output for ${scenario.toString()} are equivalents")
        return result
    }
}
