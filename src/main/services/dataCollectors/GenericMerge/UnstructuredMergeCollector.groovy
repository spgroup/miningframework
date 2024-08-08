package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import util.CsvUtils
import util.ProcessRunner

import java.nio.file.Path

class UnstructuredMergeCollector implements DataCollector {
    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
                .stream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .map(scenario -> {
                    def executionTime = runGitMergeFile(scenario)
                    return [project.getName(), mergeCommit.getSHA(), scenario.toString(), executionTime]
                })
                .map(CsvUtils::toCsvRepresentation)

        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_UNSTRUCTURED_TIMES_FILE_NAME)
        reportFile << scenarios.collect(CsvUtils.asLines()) << "\n"
    }

    private static long runGitMergeFile(Path scenario) {
        def executionTimes = new ArrayList<Long>()

        for (int i = 0; i < GenericMergeConfig.NUMBER_OF_EXECUTIONS; i++) {
            long startTime = System.nanoTime()

            def processBuilder = ProcessRunner.buildProcess(GenericMergeConfig.BASE_EXPERIMENT_PATH,
                    "git",
                    "merge-file",
                    scenario.resolve("left.java").toString(),
                    scenario.resolve("base.java").toString(),
                    scenario.resolve("right.java").toString())
            ProcessRunner.startProcess(processBuilder).waitFor()

            long endTime = System.nanoTime()
            // If we're running more than one execution, we use the first one as a warm up
            if (GenericMergeConfig.NUMBER_OF_EXECUTIONS == 1 || i > 0) {
                executionTimes.add(endTime - startTime)
            }
        }

        return (long) (executionTimes.stream().reduce(0, (prev, cur) -> prev + cur) / executionTimes.size())
    }
}
