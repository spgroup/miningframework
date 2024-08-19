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
            String scenarioDirectory = getCorrectScenarioDirectory(line, outputPath, projectName, commitSHA)

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
