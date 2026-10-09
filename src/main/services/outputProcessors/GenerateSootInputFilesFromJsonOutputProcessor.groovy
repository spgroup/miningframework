package services.outputProcessors

import groovy.json.JsonSlurper
import interfaces.OutputProcessor
import services.dataCollectors.modifiedLinesCollector.exporters.CsvModifiedLinesExporter

import static app.MiningFramework.arguments

/**
 * Same as GenerateSootInputFilesOutputProcessor (scripts/parse_to_soot.py), from the modified lines JSON instead of
 * the CSV: for each method of the scenarios with a build, in
 * [outputPath]/files/[project]/[merge commit]/changed-methods/[class]/[method]/, left-right-lines.csv with left
 * modifications as sinks and right ones as sources, and right-left-lines.csv the other way around. Deletions are not used.
 * The same files with the lines of all the methods of a scenario are in [outputPath]/files/[project]/[merge commit]/,
 * for the analyses of a whole scenario (MergeScenario).
 *
 * @requires: a [outputPath]/data/results-with-build-information.json file, as JsonModifiedLinesExporter writes it
 * @provides: the input files of the soot analyses
 */
class GenerateSootInputFilesFromJsonOutputProcessor implements OutputProcessor {

    void processOutput() {
        generateSootInputFiles(arguments.getOutputPath())
    }

    void generateSootInputFiles(String outputPath, File json = new File(outputPath, 'data/results-with-build-information.json')) {
        for (Map scenario : new JsonSlurper().parse(json).findAll { it.hasBuild }) {
            File scenarioFolder = new File(outputPath, "files/${scenario.project}/${scenario.commits.merge}")
            String scenarioLeftRightLines = ''
            String scenarioRightLeftLines = ''

            for (Map method : scenario.modifiedMethods) {
                String className = CsvModifiedLinesExporter.clean(method.className)
                List<List> lines = method.leftModifications.collect { [it, 'sink', 'source'] } + method.rightModifications.collect { [it, 'source', 'sink'] }
                if (lines) {
                    String leftRightLines = lines.collect { "${className},${it[1]},${it[0]}\n" }.join()
                    String rightLeftLines = lines.collect { "${className},${it[2]},${it[0]}\n" }.join()

                    File folder = new File(scenarioFolder, "changed-methods/${className}/${CsvModifiedLinesExporter.clean(method.method)}")
                    folder.mkdirs()
                    new File(folder, 'left-right-lines.csv').text = leftRightLines
                    new File(folder, 'right-left-lines.csv').text = rightLeftLines

                    scenarioLeftRightLines += leftRightLines
                    scenarioRightLeftLines += rightLeftLines
                }
            }

            if (scenarioLeftRightLines) {
                new File(scenarioFolder, 'left-right-lines.csv').text = scenarioLeftRightLines
                new File(scenarioFolder, 'right-left-lines.csv').text = scenarioRightLeftLines
            }
        }
    }
}
