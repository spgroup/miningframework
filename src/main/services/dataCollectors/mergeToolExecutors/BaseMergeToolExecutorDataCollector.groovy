package services.dataCollectors.mergeToolExecutors

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import services.dataCollectors.mergeToolExecutors.model.MergeExecutionSummary
import util.CsvUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import static app.MiningFramework.arguments

abstract class BaseMergeToolExecutorDataCollector implements DataCollector {
    private static Logger LOG = LogManager.getLogger(BaseMergeToolExecutorDataCollector.class)

    protected static PERF_SAMPLING_TOTAL_NUMBER_OF_EXECUTIONS = 10

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarioFiles = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)
        if (scenarioFiles.isEmpty()) {
            LOG.debug("Early returning because there are no mutually modified files")
            return
        }

        def summaries = scenarioFiles.stream()
                .map(this::runMergeForFile)
                .map(summary -> [project.getName(), mergeCommit.getSHA(), summary.file, summary.output, summary.result, summary.time])
                .map(CsvUtils::toCsvRepresentation)
                .collect(Collectors.toList())

        writeReportToFile(arguments.getOutputPath() + "/reports/merge-tools/${getToolName()}.csv", summaries)
    }

    protected static synchronized writeReportToFile(String reportFilePath, List<String> lines) {
        def reportFile = new File(reportFilePath)
        Files.createDirectories(Paths.get(arguments.getOutputPath() + "/reports/merge-tools/"))
        reportFile.createNewFile()
        reportFile << lines.stream().collect(CsvUtils::asLines()) << System.lineSeparator()
    }

    MergeExecutionSummary runMergeForFile(Path file) {
        LOG.trace("Starting execution of tool ${getToolName()} in ${file}")
        List<Long> executionTimes = new ArrayList<>()
        def outputFilePath = file.resolve("merge." + getToolName().toLowerCase() + ".java")
        MergeExecutionResult result = null

        for (int i = 0; i < PERF_SAMPLING_TOTAL_NUMBER_OF_EXECUTIONS; i++) {
            LOG.trace("Starting execution ${i + 1} of ${PERF_SAMPLING_TOTAL_NUMBER_OF_EXECUTIONS}")
            long startTime = System.nanoTime()
            result = executeTool(file, outputFilePath)
            long endTime = System.nanoTime()
            LOG.trace("Finished execution ${i + 1} of ${PERF_SAMPLING_TOTAL_NUMBER_OF_EXECUTIONS} IN ${endTime - startTime} ns")
            // If we're running more than one execution, we use the first one as a warm up
            if (PERF_SAMPLING_TOTAL_NUMBER_OF_EXECUTIONS == 1 || i > 0) {
                executionTimes.add(endTime - startTime)
            }
        }

        long averageTime = (long) (executionTimes.stream().reduce(0, (prev, cur) -> prev + cur) / executionTimes.size())

        def summary = new MergeExecutionSummary(file, outputFilePath, result, averageTime)

        LOG.trace("Finished execution of tool ${getToolName()} in ${file}. Execution took ${summary.time}ns and finished with ${summary.result.toString()} status")
        return summary
    }

    protected abstract MergeExecutionResult executeTool(Path file, Path outputFile);

    abstract String getToolName();
}
