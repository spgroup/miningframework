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
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class GenericMergeDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(GenericMergeDataCollector.class)

    private static final BASE_EXPERIMENT_PATH = System.getProperty("miningframework.generic_merge.base_experiment_path", "/usr/src/app")
    private static final String GENERIC_MERGE_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/generic-merge"
    public static final GENERIC_MERGE_REPORT_PATH = "${BASE_EXPERIMENT_PATH}/output/reports"
    public static final GENERIC_MERGE_REPORT_FILE_NAME = "${GENERIC_MERGE_REPORT_PATH}/generic-merge-execution.csv"
    public static final MERGE_TOOL_EXECUTORS_TO_USE = System.getProperty("miningframework.generic_merge.merge_tool_executors_to_use", "generic_merge,jdime").split(",")

    private final List<MergeToolExecutor> mergeToolExecutors

    GenericMergeDataCollector() {
        this.mergeToolExecutors = new ArrayList<MergeToolExecutor>()
        if (MERGE_TOOL_EXECUTORS_TO_USE.contains("generic_merge")) {
            LOG.debug("Registering Generic Merge as a merge tool executor")
            this.mergeToolExecutors.add(new GenericMergeToolExecutor())
        }
        if (MERGE_TOOL_EXECUTORS_TO_USE.contains("jdime")) {
            LOG.debug("Registering jDime as a merge tool executor")
            this.mergeToolExecutors.add(new JDimeMergeToolExecutor())
        }
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarios = filterScenariosForExecution(MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)).collect(Collectors.toUnmodifiableList())

        LOG.trace("Starting execution of merge tools on scenario")
        def mergeToolsExecutionResults = scenarios
                .stream()
                .flatMap(scenario -> {
                    return mergeToolExecutors
                            .parallelStream()
                            .map(executor -> executor.runToolForMergeScenario(scenario))
                }).collect(Collectors.toUnmodifiableList())
        LOG.trace("Finished execution of merge tools on scenario")

        // Aggregate scenario results by tool
        def toolsCommitSummary = mergeToolsExecutionResults
                .parallelStream()
                .collect(Collectors.groupingBy(MergeScenarioExecutionSummary::getTool,
                        Collectors.collectingAndThen(Collectors.toList(),
                                MergeCommitExecutionSummary::fromFileResultsList)
                ))


        // Check which tools successfully integrated the scenario
        def toolsInWhichIntegrationSucceeded = toolsCommitSummary
                .entrySet()
                .stream()
                .filter { it -> it.value.result == MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS }
                .map { x -> x.key }
                .collect(Collectors.toUnmodifiableList())

        // Are there any exclusive conflicts/tool errors?
        if (toolsInWhichIntegrationSucceeded.size() != mergeToolExecutors.size()) {
            LOG.info("At least one of the tools either reported a conflict or failed on the commit while the other did not")
            toolsInWhichIntegrationSucceeded.forEach { tool -> {
                def toolCommitSummary = toolsCommitSummary.get(tool)
                if (toolCommitSummary.allScenariosMatch) {
                    LOG.info("Output of the tool " + tool + " fully matched the commit merge. Skipping build analysis")
                } else {
                    LOG.info("Output of the tool " + tool + " did not fully matched the commit merge. Starting build analysis")
                    BuildRequester.requestBuildWithRevision(project, mergeCommit, scenarios, tool)
                }
            }}
        }

        LOG.trace("Starting write of files results to report file")
        def lines = mergeToolsExecutionResults.parallelStream().map(result -> getReportLine(project, mergeCommit, result))
        def reportFile = new File(GENERIC_MERGE_REPORT_FILE_NAME)
        reportFile << lines.collect(Collectors.joining(System.lineSeparator())) << "\n"
        LOG.trace("Finished write of files results to report file")
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

    static enum MergeScenarioResult {
        SUCCESS_WITHOUT_CONFLICTS,
        SUCCESS_WITH_CONFLICTS,
        TOOL_ERROR
    }

    static class MergeScenarioExecutionSummary {
        public final String tool
        public final Path scenario
        public final Path output
        public final MergeScenarioResult result
        public final long time
        public final boolean equivalentToOracle

        MergeScenarioExecutionSummary(Path scenario, Path output, MergeScenarioResult result, long time, String tool) {
            this.scenario = scenario
            this.output = output
            this.result = result
            this.time = time
            this.tool = tool
            this.equivalentToOracle = areFilesSyntacticallyEquivalent(scenario.resolve("merge.java"), output)
        }

        String getTool() {
            return tool
        }

        boolean isEquivalentToOracle() {
            return equivalentToOracle
        }

        private static boolean areFilesSyntacticallyEquivalent(Path fileA, Path fileB) {
            if (!Files.exists(fileA) || !Files.exists(fileB)) {
                LOG.trace("Early returning because one of the files ${} do not exist")
                return false
            }

            def process = ProcessRunner.buildProcess("./")

            def list = new ArrayList<String>()
            list.add(GENERIC_MERGE_BINARY_PATH)
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

    static class MergeCommitExecutionSummary {
        public final MergeScenarioResult result
        public final boolean allScenariosMatch

        private MergeCommitExecutionSummary(MergeScenarioResult result, boolean allScenariosMatch) {
            this.result = result
            this.allScenariosMatch = allScenariosMatch
        }

        static MergeCommitExecutionSummary fromFileResultsList(List<MergeScenarioExecutionSummary> results) {
            return new MergeCommitExecutionSummary(MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS,
                    results.stream().every { it -> it.isEquivalentToOracle() })
        }
    }
}
