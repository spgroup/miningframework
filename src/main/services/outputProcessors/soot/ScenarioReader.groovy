package services.outputProcessors.soot

import com.xlson.groovycsv.PropertyMapper
import groovy.json.JsonSlurper
import services.outputProcessors.soot.entrypointStrategies.EntrypointStrategy

import static com.xlson.groovycsv.CsvParser.parseCsv
import static services.dataCollectors.modifiedLinesCollector.exporters.CsvModifiedLinesExporter.clean

class ScenarioReader {

    static List<Scenario> read (String outputPath, String fileName) {
        String filePath = "${outputPath}/${fileName}"

        List<Scenario> result = new ArrayList<Scenario>()
        String resultsFileText = new File(filePath).getText()

        def iterator = parseCsv(resultsFileText, separator:';')

        for (line in iterator) {
            String projectName = line["project"]
            String classPathName = line["className"]
            String methodSignature = line["method"]
            String commitSHA = line["merge commit"]
            String entrypoints = getEntrypointColumn(line);
            String scenarioDirectory = getCorrectScenarioDirectory(line, outputPath, projectName, commitSHA)

            boolean hasBuild = line["has_build"] == "true"

            def scenario = new Scenario(projectName, classPathName, methodSignature, commitSHA, scenarioDirectory, hasBuild, entrypoints);

            result.add(scenario)
        }
        return result
    }

    /**
     * Scenarios of a modified lines JSON, one per modified method, with the entrypoints the strategy chooses for its
     * merge scenario from the methods modified by left and by right: the ones with a line modified by each
     * (LinesToSootMethodResolver)
     */
    static List<Scenario> readJson(String outputPath, String jsonPath, EntrypointStrategy strategy) {
        List<Scenario> result = new ArrayList<Scenario>()

        for (Map merge : new JsonSlurper().parse(new File(jsonPath))) {
            String commitSHA = merge.commits.merge
            String scenarioDirectory = getScenarioDirectory(outputPath, merge.project, commitSHA)
            String buildJar = "${scenarioDirectory}/original-without-dependencies/merge/build.jar"
            List<Map> methods = merge.modifiedMethods

            Set<String> leftMethods = LinesToSootMethodResolver.resolve(buildJar, getModifiedLinesByClass(methods, "leftModifications"))
            Set<String> rightMethods = LinesToSootMethodResolver.resolve(buildJar, getModifiedLinesByClass(methods, "rightModifications"))
            String entrypoints = strategy.getEntrypoints(buildJar, leftMethods, rightMethods).toString()

            for (Map method : methods) {
                result.add(new Scenario(merge.project, clean(method.className), clean(method.method), commitSHA, scenarioDirectory, merge.hasBuild, entrypoints))
            }
        }
        return result
    }

    // e.g. {"a.C": [10, 12]}, as conflict-static-analysis reads the lines files
    static private Map<String, List<Integer>> getModifiedLinesByClass(List<Map> methods, String modifications) {
        Map<String, List<Integer>> lines = [:]
        for (Map method : methods) {
            lines.computeIfAbsent(method.className) { new ArrayList<Integer>() }.addAll(method[modifications])
        }
        return lines
    }

    static private  String getEntrypointColumn(Object line) {
        if (line instanceof Map && line.containsKey("entrypoints")) {
            return line["entrypoints"]?.toString()
        }
        return null;
    }

    static private  String getCorrectScenarioDirectory(Object line, String outputPath, String projectName,  String commitSHA) {
        try{
            String realisticCasePath = line["realistic case path"]
            return getScenarioDirectory(outputPath, projectName, commitSHA, realisticCasePath)
        }catch (Exception ignored){
            return getScenarioDirectory(outputPath, projectName, commitSHA)
        }

    }

    static private String getScenarioDirectory(String outputPath, String projectName, String commitSHA, String realisticCasePath) {
        return "${getScenarioDirectory(outputPath, projectName, commitSHA)}/${realisticCasePath}"
    }

    static private String getScenarioDirectory (String outputPath, String projectName, String commitSHA) {
        return "${(new File(outputPath)).getAbsolutePath()}/files/${projectName}/${commitSHA}"
    }
}
