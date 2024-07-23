package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.executors.GenericMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.JDimeMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.MergeToolExecutor
import services.dataCollectors.GenericMerge.model.MergeCommitExecutionSummary
import services.dataCollectors.GenericMerge.model.MergeScenarioExecutionSummary
import services.dataCollectors.GenericMerge.model.MergeScenarioResult
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector

import java.nio.file.Path
import java.util.stream.Collectors

class GenericMergeDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(GenericMergeDataCollector.class)

    private final List<MergeToolExecutor> mergeToolExecutors

    GenericMergeDataCollector() {
        this.mergeToolExecutors = new ArrayList<MergeToolExecutor>()
        if (GenericMergeConfig.MERGE_TOOL_EXECUTORS_TO_USE.contains("generic_merge")) {
            LOG.debug("Registering Generic Merge as a merge tool executor")
            this.mergeToolExecutors.add(new GenericMergeToolExecutor())
        }
        if (GenericMergeConfig.MERGE_TOOL_EXECUTORS_TO_USE.contains("jdime")) {
            LOG.debug("Registering jDime as a merge tool executor")
            this.mergeToolExecutors.add(new JDimeMergeToolExecutor())
        }
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarios = filterScenariosForExecution(MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)).collect(Collectors.toList())

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


        // Check which tools successfully integrated the scenario
        def toolsInWhichIntegrationSucceeded = toolsCommitSummary
                .entrySet()
                .stream()
                .filter { it -> it.value.result == MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS }
                .map { x -> x.key }
                .collect(Collectors.toList())

        if (toolsInWhichIntegrationSucceeded.size() == 0) {
            LOG.info("Integration failed in all tools")
        } else if (toolsInWhichIntegrationSucceeded.size() != mergeToolExecutors.size()) {
            LOG.info("At least one of the tools either reported a conflict or failed on the commit while the other did not")
            toolsInWhichIntegrationSucceeded.forEach { tool ->
                {
                    def toolCommitSummary = toolsCommitSummary.get(tool)
                    if (toolCommitSummary.allScenariosMatch) {
                        LOG.info("Output of the tool " + tool + " fully matched the commit merge. Skipping build analysis")
                    } else {
                        LOG.info("Output of the tool " + tool + " did not fully matched the commit merge. Starting build analysis")
                        BuildRequester.requestBuildWithRevision(project, mergeCommit, scenarios, tool)
                    }
                }
            }
        } else {
            LOG.info("All the tools reported the same response")
        }

        LOG.trace("Starting write of files results to report file")
        def lines = mergeToolsExecutionResults.parallelStream().map(result -> getReportLine(project, mergeCommit, result))
        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_FILE_NAME)
        reportFile << lines.collect(Collectors.joining(System.lineSeparator())) << "\n"
        LOG.trace("Finished write of files results to report file")

        LOG.trace("Starting write of commit report")
        def commitLines = toolsCommitSummary.entrySet().parallelStream().map { it ->
            def list = new ArrayList<String>()
            list.add(project.getName())
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

    private static filterScenariosForExecution(List<Path> scenarios) {
        return scenarios
                .parallelStream()
                .filter(GenericMergeDataCollector::eitherParentDiffersFromBase)
    }

    private static boolean eitherParentDiffersFromBase(Path scenario) {
        def leftEqualsBase = FileUtils.contentEquals(new File("${scenario.toAbsolutePath()}/base.java"),
                new File("${scenario.toAbsolutePath()}/left.java"))

        def rightEqualsBase = FileUtils.contentEquals(new File("${scenario.toAbsolutePath()}/base.java"),
                new File("${scenario.toAbsolutePath()}/right.java"))

        if (leftEqualsBase) {
            LOG.trace("In scenario ${scenario.toString()} left equals base")
        }

        if (rightEqualsBase) {
            LOG.trace("In scenario ${scenario.toString()} right equals base")
        }

        return !leftEqualsBase && !rightEqualsBase
    }
}
