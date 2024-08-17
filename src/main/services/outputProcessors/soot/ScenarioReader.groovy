package services.outputProcessors.soot

import com.xlson.groovycsv.PropertyMapper

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

            String scenarioDirectory = getScenarioDirectory(outputPath, projectName, commitSHA)

            if (line.hasProperty("realistic case path")) {
                String realisticCasePath = line["realistic case path"]
                scenarioDirectory = getScenarioDirectory(outputPath, projectName, commitSHA, realisticCasePath)
            }

            boolean hasBuild = line["has_build"] == "true"

            def scenario = new Scenario(projectName, classPathName, methodSignature, commitSHA, scenarioDirectory, hasBuild, entrypoints);

            result.add(scenario)
        }
        return result
    }

    static private  String getEntrypointColumn(Object line) {
        try{
            return  line["entrypoints"]?.toString()
        }catch (Exception ignored){
            return null;
        }

    }

    static private String getScenarioDirectory(String outputPath, String projectName, String commitSHA, String realisticCasePath) {
        return "${getScenarioDirectory(outputPath, projectName, commitSHA)}/${realisticCasePath}"
    }

    static private String getScenarioDirectory (String outputPath, String projectName, String commitSHA) {
        return "${(new File(outputPath)).getAbsolutePath()}/files/${projectName}/${commitSHA}"
    }
}
