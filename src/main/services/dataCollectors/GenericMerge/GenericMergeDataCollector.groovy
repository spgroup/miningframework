package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.commons.io.FileUtils
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GenericMergeDataCollector implements DataCollector {
    private static final GENERIC_MERGE_BINARY_PATH = "/Users/jpedroh/Projetos/msc/generic-merge/target/release/generic-merge"
    private static final GENERIC_MERGE_REPORTS_PATH = "/Users/jpedroh/Projetos/msc/miningframework/output/reports/"

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> scenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)

        Files.createDirectories(Paths.get("${GENERIC_MERGE_REPORTS_PATH}"));
        def reportFile = new File("${GENERIC_MERGE_REPORTS_PATH}/${project.getName()}.csv");
        reportFile.createNewFile();

        scenarios.stream().forEach {
            def result = runToolInMergeScenario(it)
            def line = "${project.getName()},${mergeCommit.getSHA()},${it.toAbsolutePath().toString()},${anyParentEqualsBase(it)},${result.result},${result.time}"
            reportFile << "${line.replaceAll('\\\\', '/')}\n"
        }
    }

    private static anyParentEqualsBase(Path scenario) {
        def leftEqualsBase = FileUtils.contentEquals(
                new File("${scenario.toAbsolutePath()}/basejava"),
                new File("${scenario.toAbsolutePath()}/leftjava")
        )

        def rightEqualsBase = FileUtils.contentEquals(
                new File("${scenario.toAbsolutePath()}/basejava"),
                new File("${scenario.toAbsolutePath()}/rightjava")
        )

        return leftEqualsBase || rightEqualsBase
    }

    private static MergeScenarioExecution runToolInMergeScenario(Path scenario) {
            def working_directory_path = scenario.toAbsolutePath().toString();

            def processBuilder = ProcessRunner.buildProcess(working_directory_path);
            processBuilder.command().addAll(getBuildParameters())

            def startTime = System.nanoTime();
            def output = ProcessRunner.startProcess(processBuilder);
            output.waitFor()
            def endTime = System.nanoTime();

        if (output.exitValue() > 1) {
                println("Error while merging ${scenario.toAbsolutePath()}: ${output.getInputStream().readLines()}")
        }

        def result = output.exitValue() == 0 ? ScenarioResult.SUCCESS_WITHOUT_CONFLICTS : output.exitValue() == 1 ? ScenarioResult.SUCCESS_WITH_CONFLICTS : ScenarioResult.TOOL_ERROR;

        return new MergeScenarioExecution(result, endTime - startTime);
    }

    private static enum ScenarioResult {
        SUCCESS_WITHOUT_CONFLICTS,
        SUCCESS_WITH_CONFLICTS,
        TOOL_ERROR
    }

    private static class MergeScenarioExecution{
        ScenarioResult result;
        long time;

        MergeScenarioExecution(ScenarioResult result, long time) {
            this.result = result
            this.time = time
        }
    }

    private static List<String> getBuildParameters() {
        def list = new ArrayList<String>()
        list.add(GENERIC_MERGE_BINARY_PATH)
        list.add("--base-path=basejava")
        list.add("--left-path=leftjava")
        list.add("--right-path=rightjava")
        list.add("--merge-path=merge.generic.java")
        list.add("--language=java")

        return list
    }
}
