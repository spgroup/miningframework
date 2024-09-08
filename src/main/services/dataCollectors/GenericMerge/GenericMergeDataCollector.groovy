package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.executors.GenericMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.JDimeMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.MergeToolExecutor
import services.dataCollectors.GenericMerge.model.MergeCommitExecutionSummary
import services.dataCollectors.GenericMerge.model.MergeScenarioExecutionSummary
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter

import java.util.stream.Collectors

class GenericMergeDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(GenericMergeDataCollector.class)

    private final List<MergeToolExecutor> mergeToolExecutors = Arrays.asList(new GenericMergeToolExecutor(),
            new JDimeMergeToolExecutor())

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        LOG.trace("Starting filtering of files to exclude fast forwards")
        def scenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
                .parallelStream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .collect(Collectors.toList())
        LOG.trace("Finished filtering of files to exclude fast forwards")

        LOG.trace("Starting normalization of merge files on scenario")
        scenarios.parallelStream().forEach { scenario -> FileFormatNormalizer.normalizeFileInPlace(scenario.resolve("merge.java"))
        }
        LOG.trace("Finished normalization of merge files on scenario")

        LOG.trace("Starting execution of merge tools on scenario")
        def mergeToolsExecutionResults = scenarios
                .stream()
                .flatMap(scenario -> {
                    return mergeToolExecutors
                            .parallelStream()
                            .map(executor -> executor.runToolForMergeScenario(scenario))
                }).collect(Collectors.toList())
        LOG.trace("Finished execution of merge tools on scenario")

        // Aggregate scenario results by tool
        def toolsCommitSummary = mergeToolsExecutionResults
                .parallelStream()
                .collect(Collectors.groupingBy(MergeScenarioExecutionSummary::getTool,
                        Collectors.collectingAndThen(Collectors.toList(),
                                MergeCommitExecutionSummary::fromFileResultsList)))

        LOG.trace("Starting write of files results to report file")
        def lines = mergeToolsExecutionResults.parallelStream().map(result -> getReportLine(project, mergeCommit, result))
        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_FILE_NAME)
        reportFile << lines.collect(Collectors.joining(System.lineSeparator())) << "\n"
        LOG.trace("Finished write of files results to report file")

        LOG.trace("Starting write of commit report")
        def commitLines = toolsCommitSummary.entrySet().parallelStream().map { it ->
            def list = new ArrayList<String>()
            list.add(project.getName())
            list.add(project.getPath())
            list.add(mergeCommit.getAncestorSHA())
            list.add(mergeCommit.getLeftSHA())
            list.add(mergeCommit.getRightSHA())
            list.add(mergeCommit.getSHA())
            list.add(it.key)
            list.add(it.value.result.toString())
            list.add(it.value.allScenariosMatch.toString())
            return list.join(",").replaceAll('\\\\', '/')
        }
        def commitReportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_COMMITS_FILE_NAME)
        commitReportFile << commitLines.collect(Collectors.joining(System.lineSeparator())) << "\n"
        LOG.trace("Finished write of commit report")
    }

    private static getReportLine(Project project, MergeCommit mergeCommit, MergeScenarioExecutionSummary result) {
        def list = new ArrayList<String>()
        list.add(project.getName())
        list.add(mergeCommit.getSHA())
        list.add(result.tool)
        list.add(result.scenario.toAbsolutePath().toString())
        list.add(result.output.toAbsolutePath().toString())
        list.add(result.result.toString())
        list.add(result.time.toString())
        list.add(result.isEquivalentToOracle().toString())
        list.join(",").replaceAll('\\\\', '/')
    }
}
