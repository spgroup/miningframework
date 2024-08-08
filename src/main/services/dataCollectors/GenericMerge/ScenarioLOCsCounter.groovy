package services.dataCollectors.GenericMerge

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import util.CsvUtils
import util.ProcessRunner

import java.nio.file.Path

/**
 * Counts the number of LOCs (ignoring comments and blank lines) in base, left and right.
 * It assumes that there's an executable for cloc in ./dependencies (symbolic links are allowed).*/
class ScenarioLOCsCounter implements DataCollector {
    private static Logger LOG = LogManager.getLogger(MergeToolsComparator.class)

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def scenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
                .parallelStream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .map((scenario) -> {
                    LOG.trace("Starting to count LOCs in ${scenario.toString()}")
                    def base = countLinesOfCodeInFile(scenario.resolve("base.java"))
                    def left = countLinesOfCodeInFile(scenario.resolve("left.java"))
                    def right = countLinesOfCodeInFile(scenario.resolve("right.java"))
                    def total = base + left + right

                    return [project.getName(), mergeCommit.getSHA(), scenario.toString(), base.toString(), left.toString(), right.toString(), total.toString()]
                })
                .map(CsvUtils::toCsvRepresentation)

        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_SCENARIO_LOCS_FILE_NAME)
        reportFile << scenarios.collect(CsvUtils.asLines()) << "\n"
    }

    private static int countLinesOfCodeInFile(Path file) {
        def clocProcessBuilder = ProcessRunner.buildProcess("./dependencies",
                "./cloc",
                file.toAbsolutePath().toString(),
                "--json")

        def output = ProcessRunner.startProcess(clocProcessBuilder)
        output.waitFor()

        def jsonOutput = new JSONObject(output.getInputStream().readLines().join('\n'))
        int sumCode = jsonOutput.getJSONObject("SUM").getInt("code")

        return sumCode
    }
}
