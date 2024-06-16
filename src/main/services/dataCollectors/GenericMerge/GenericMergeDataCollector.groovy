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
        def scenarios = filterScenariosForExecution(MergeScenarioCollector.collectMergeScenarios(project, mergeCommit))

        LOG.trace("Starting execution of merge tools on scenario")
        def mergeToolsExecutionResults = scenarios
                .flatMap(scenario -> {
                    return mergeToolExecutors
                            .parallelStream()
                            .map(executor -> executor.runToolForMergeScenario(scenario))
                })
        LOG.trace("Finished execution of merge tools on scenario")

        LOG.trace("Starting write of results to report file")
        def lines = mergeToolsExecutionResults.map(result -> getReportLine(project, mergeCommit, result))
        def reportFile = new File(GENERIC_MERGE_REPORT_FILE_NAME)
        reportFile << lines.collect(Collectors.joining(System.lineSeparator())) << "\n"
        LOG.trace("Finished write of results to report file")
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
        list.add(areFilesSyntacticallyEquivalent(result.scenario.resolve("merge.java"), result.output).toString())
        list.join(",").replaceAll('\\\\', '/')
    }

    private static filterScenariosForExecution(List<Path> scenarios) {
        return scenarios
                .parallelStream()
                .filter(GenericMergeDataCollector::eitherParentDiffersFromBase)
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

        MergeScenarioExecutionSummary(Path scenario, Path output, MergeScenarioResult result, long time, String tool) {
            this.scenario = scenario
            this.output = output
            this.result = result
            this.time = time
            this.tool = tool
        }
    }
}
