
package services.outputProcessors.soot

import interfaces.OutputProcessor

import static app.MiningFramework.arguments

/**
 * @requires: that soot-analysis.jar is in the dependencies folder and that
 * FetchBuildsOutputProcessor and GenerateSootInputFilesOutputProcessor were ran
 * @provides: a [outputPath]/data/soot-results.csv file with the results for the soot algorithms ran
 */
class RunSootAnalysisOutputProcessor implements OutputProcessor {

    private final String RESULTS_FILE_PATH = "/data/results-with-build-information.csv"

    private final ConflictDetectionAlgorithm[] detectionAlgorithms = [
            // dataflow: direct dependency between contributions, intraprocedural and without transitivity
            new NonCommutativeConflictDetectionAlgorithm("dataflow"),
            // tainted: direct dependency between contributions, intraprocedural and with transitivity
            new NonCommutativeConflictDetectionAlgorithm("tainted"),
            // svfa: direct dependency between contributions, interprocedural and  with transitivity
            new NonCommutativeConflictDetectionAlgorithm("svfa", 30),
            // confluence: indirect dependency between contributions, intraprocedural and without transitivity
            new ConflictDetectionAlgorithm("confluence")
    ]

    void processOutput () {
        // check if file generated by FetchBuildsOutputProcessor exists
        println "Executing RunSootAnalysisOutputProcessor"
        executeAllAnalyses(arguments.getOutputPath())
    }

    void executeAllAnalyses(String outputPath) {
        File sootResultsFile = createOutputFile(outputPath)

        File resultsWithBuildsFile = new File(outputPath + RESULTS_FILE_PATH)
        if (resultsWithBuildsFile.exists()) {

            List<Scenario> sootScenarios = ScenarioReader.read(outputPath, RESULTS_FILE_PATH);

            for (scenario in sootScenarios) {
                println(scenario.getHasBuild())
                println(scenario.getCommitSHA())
                if (scenario.getHasBuild()) {
                    println "Running soot scenario ${scenario.toString()}"
                    List<String> results = [];

                    for (ConflictDetectionAlgorithm algorithm : detectionAlgorithms) {
                        String algorithmResult
                        try {
                            algorithmResult = algorithm.run(scenario);
                        } catch (ClassNotFoundInJarException e) {
                            println e.getMessage()
                            algorithmResult = "error"
                        }
                        results.add(algorithmResult)
                    }
                    sootResultsFile << "${scenario.toString()};${results.join(";")}\n"
                }
            }
        }
    }

    private File createOutputFile(String outputPath) {
        File sootResultsFile = new File(outputPath + "/data/soot-results.csv")

        if (sootResultsFile.exists()) {
            sootResultsFile.delete()
        }

        sootResultsFile << buildCsvHeader();
    
        return sootResultsFile
    }

    private String buildCsvHeader () {
        StringBuilder resultStringBuilder = new StringBuilder("project;class;method;merge commit");

        for (ConflictDetectionAlgorithm algorithm : detectionAlgorithms) {
            resultStringBuilder.append(";${algorithm.generateHeaderName()}");
        }
        resultStringBuilder.append("\n");

        return resultStringBuilder.toString();
    }

}