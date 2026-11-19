package services.outputProcessors.soot

import com.xlson.groovycsv.PropertyMapper
import groovy.json.JsonSlurper
import services.outputProcessors.soot.entrypointStrategies.EntrypointStrategy
import services.outputProcessors.soot.methodResolvers.MethodResolver
import services.outputProcessors.soot.methodResolvers.MethodResolvers

import static com.xlson.groovycsv.CsvParser.parseCsv

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
     * The merge scenarios of a modified lines JSON, each analysed as a whole, with the entrypoints the strategy chooses
     * from the methods modified by left and by right: the ones with a line modified by each (MethodResolvers, by default
     * in the bytecode, BytecodeMethodResolver)
     */
    static List<MergeScenario> readJson(String outputPath, String jsonPath, EntrypointStrategy strategy, MethodResolver resolver = MethodResolvers.create()) {
        List<MergeScenario> result = new ArrayList<MergeScenario>()
        List<Map> merges = new JsonSlurper().parse(new File(jsonPath))

        for (int i = 0; i < merges.size(); i++) {
            Map merge = merges[i]
            String commitSHA = merge.commits.merge
            String scenarioDirectory = getScenarioDirectory(outputPath, merge.project, commitSHA)
            String mergeSource = "${scenarioDirectory}/source/merge"
            List<Map> methods = merge.modifiedMethods
            if (methods.isEmpty()) {
                continue
            }
            // the same jar as read and the analysis: the one of merge/ that has the class (Scenario.getClassPath)
            String buildJar = new Scenario(merge.project, methods[0].className, null, commitSHA, scenarioDirectory, merge.hasBuild, null).getClassPath()

            Set<String> leftMethods = resolver.resolve(buildJar, mergeSource, getModifiedLinesByClass(methods, "leftModifications"))
            Set<String> rightMethods = resolver.resolve(buildJar, mergeSource, getModifiedLinesByClass(methods, "rightModifications"))
            List<String> entrypointsList = strategy.getEntrypoints(buildJar, leftMethods, rightMethods)
            String entrypoints = entrypointsList.toString()
            println "[${i + 1}/${merges.size()}] ${merge.project} ${commitSHA}: methods modified by left ${leftMethods.size()}, by right ${rightMethods.size()} -> ${entrypointsList.size()} entrypoints"

            result.add(new MergeScenario(merge.project, methods[0].className, commitSHA, scenarioDirectory, merge.hasBuild, entrypoints, leftMethods, rightMethods))
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
