package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.commons.io.FileUtils
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.executors.GenericMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.JDimeMergeToolExecutor
import services.dataCollectors.GenericMerge.executors.MergeToolExecutor
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GenericMergeDataCollector implements DataCollector {
    private static final BASE_EXPERIMENT_PATH = "/usr/src/app"
    private static final GENERIC_MERGE_REPORTS_PATH = "${BASE_EXPERIMENT_PATH}/output/reports/"
    private final List<MergeToolExecutor> mergeToolExecutors;

    GenericMergeDataCollector() {
        this.mergeToolExecutors = new ArrayList<MergeToolExecutor>()
        this.mergeToolExecutors.add(new GenericMergeToolExecutor())
        this.mergeToolExecutors.add(new JDimeMergeToolExecutor())
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> scenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)

        Files.createDirectories(Paths.get("${GENERIC_MERGE_REPORTS_PATH}"));
        def reportFile = new File("${GENERIC_MERGE_REPORTS_PATH}/${project.getName()}.csv");
        reportFile.createNewFile();

        scenarios.stream()
                .filter { scenario -> !anyParentEqualsBase(scenario) }
                .flatMap { scenario ->
                    mergeToolExecutors.stream().map { executor ->
                        executor.runToolForMergeScenario(scenario)
                    }
                }
                .forEach {
                    def list = new ArrayList<String>();
                    list.add(project.getName())
                    list.add(it.tool)
                    list.add(mergeCommit.getSHA())
                    list.add(it.scenario.toAbsolutePath().toString())
                    list.add(it.result.toString())
                    list.add(it.time.toString())

                    reportFile << "${list.join(",").replaceAll('\\\\', '/')}\n"
                }
    }

    private static anyParentEqualsBase(Path scenario) {
        def leftEqualsBase = FileUtils.contentEquals(new File("${scenario.toAbsolutePath()}/basejava"),
                new File("${scenario.toAbsolutePath()}/leftjava"))

        def rightEqualsBase = FileUtils.contentEquals(new File("${scenario.toAbsolutePath()}/basejava"),
                new File("${scenario.toAbsolutePath()}/rightjava"))

        return leftEqualsBase || rightEqualsBase
    }

    static enum MergeScenarioResult {
        SUCCESS_WITHOUT_CONFLICTS,
        SUCCESS_WITH_CONFLICTS,
        TOOL_ERROR
    }

    static class MergeScenarioExecutionSummary {
        public final String tool;
        public final Path scenario;
        public final MergeScenarioResult result;
        public final long time;

        MergeScenarioExecutionSummary(Path scenario, MergeScenarioResult result, long time, String tool) {
            this.scenario = scenario
            this.result = result
            this.time = time
            this.tool = tool
        }
    }
}
