package services.dataCollectors.modifiedLinesCollector.sourceScenarios

import groovy.cli.commons.CliBuilder
import groovy.json.JsonSlurper
import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.SourceModifiedLinesCollector
import services.dataCollectors.modifiedLinesCollector.exporters.CsvModifiedLinesExporter
import services.dataCollectors.modifiedLinesCollector.exporters.JsonModifiedLinesExporter
import services.dataCollectors.modifiedLinesCollector.exporters.ModifiedLinesExporter
import services.outputProcessors.GenerateSootInputFilesOutputProcessor
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor

/**
 * Runs the SourceModifiedLinesCollector on merge scenarios with the source of the modified files and jars, in folders
 * with jars/ and source/: a single one (--path) or the ones listed in a SMAT input file (--input, e.g. data_base.json of
 * the synthetic merges dataset). Jar paths of the input file may be relative to its folder or absolute in another
 * machine: the longest suffix of the path that exists under the file's folder is used. Scenarios whose folder is listed
 * more than once are ignored, since they are ambiguous. A single scenario has its folder name as merge commit.
 *
 * @provides: [output]/data/results-with-build-information.json and .csv (or the chosen exporters), and the merge jars
 * as builds; with --analyses, also runs GenerateSootInputFilesOutputProcessor and RunSootAnalysisOutputProcessor
 *
 * usage: ./gradlew run -DmainClass="services.dataCollectors.modifiedLinesCollector.sourceScenarios.Main"
 *          --args="(-i <data_base.json> | -p <scenario folder> [-n project]) [-o output] [-e json,csv] [-a]"
 */
class Main {

    // each one writes [output]/data/results-with-build-information.<its name>
    private static final Map<String, Closure<ModifiedLinesExporter>> EXPORTERS = [
            json: { String outputPath -> new JsonModifiedLinesExporter(outputPath) },
            csv : { String outputPath -> new CsvModifiedLinesExporter(outputPath) }]

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'Main (-i <input.json> | -p <scenario folder>) [options]')
        cli.i(longOpt: 'input', args: 1, 'SMAT input file with the scenarios (e.g. [data_base.json](https://github.com/ToniMaciel/Syntetic-Mergesdataset/blob/dataset-update/data_base.json))')
        cli.p(longOpt: 'path', args: 1, 'Folder of a single scenario, with jars/ and source/ (e.g. [Syntetic-Mergesdataset/Cli/Cli_1](https://github.com/ToniMaciel/Syntetic-Mergesdataset/tree/dataset-update/Cli/Cli_1))')
        cli.n(longOpt: 'name', args: 1, 'Project name of the scenario given by --path (default: the name of its parent folder)')
        cli.o(longOpt: 'output', args: 1, 'Output folder (default: output, the one read by services.outputProcessors.soot.Main)')
        cli.e(longOpt: 'exporters', args: 1, "Comma separated exporters of the results: ${EXPORTERS.keySet().join(',')} (default: all; " +
                "--analyses reads the csv)")
        cli.a(longOpt: 'analyses', 'Also generate the soot input files and run the static analyses')
        cli.h(longOpt: 'help', 'Show this help')
        def options = cli.parse(args)
        if (!options) {
            return
        }
        List<String> exporters = options.e ? (options.e as String).split(',')*.trim() : EXPORTERS.keySet() as List
        if (options.h || !options.i == !options.p || !EXPORTERS.keySet().containsAll(exporters)) {
            cli.usage()
            return
        }

        String outputPath = options.o ?: 'output'
        exporters.each { new File(outputPath, "data/results-with-build-information.${it}").delete() }
        def collector = new SourceModifiedLinesCollector(exporters.collect { EXPORTERS[it](outputPath) }, outputPath)

        if (options.p) {
            File folder = new File(options.p as String).absoluteFile
            collector.collectData(new Project((options.n ?: folder.parentFile.name) as String, folder.path),
                    new MergeCommit(folder.name, ['left', 'right'] as String[], 'base'))
        } else {
            collectInputScenarios(collector, new File(options.i as String))
        }

        if (options.a) {
            new GenerateSootInputFilesOutputProcessor().convertToSootScript(outputPath)
            new RunSootAnalysisOutputProcessor().executeAnalyses(outputPath)
        }
    }

    private static void collectInputScenarios(SourceModifiedLinesCollector collector, File input) {
        Map<File, List> scenariosPerFolder = new JsonSlurper().parse(input).groupBy { scenarioFolder(input.absoluteFile.parentFile, it.scenarioJars.merge as String) }
        scenariosPerFolder.each { File folder, List scenarios ->
            if (scenarios.size() > 1) {
                println "Ignoring ${folder}: it is listed by ${scenarios.size()} scenarios"
                return
            }
            if (!new File(folder, 'source/base').isDirectory()) {
                println "Ignoring ${folder}: it has no source/base"
                return
            }
            Map scenario = scenarios[0] as Map
            Map commits = scenario.scenarioCommits as Map
            collector.collectData(new Project(scenario.projectName as String, folder.path),
                    new MergeCommit(commits.merge, [commits.left, commits.right] as String[], commits.base))
        }
    }

    // the folder with jars/ and source/, e.g. <input folder>/Cli/Cli_1 for /home/user/dataset/Cli/Cli_1/jars/merge.jar
    private static File scenarioFolder(File inputFolder, String mergeJar) {
        File folder = new File(mergeJar).parentFile.parentFile
        if (folder.isAbsolute() && folder.isDirectory()) {
            return folder
        }
        List<String> names = folder.path.split(/[\/\\]/).findAll() as List<String>
        return (0..<names.size()).collect { new File(inputFolder, names.drop(it).join('/')) }.find { it.isDirectory() } ?: folder
    }
}
