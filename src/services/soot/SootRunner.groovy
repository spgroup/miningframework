
package services.soot

import main.interfaces.OutputProcessor

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*
import main.exception.*
import java.nio.file.Files 
import java.nio.file.Paths
import java.nio.file.Path

import java.util.regex.Matcher
import java.util.regex.Pattern

class SootRunner {

    private String outputPath

    private final String RESULTS_FILE_PATH = "/data/results-with-builds.csv"
    private final String DATAFLOW_MODE = "dataflow"
    private final String REACHABILITY_MODE = "reachability"

    SootRunner(String outputPath) {
        this.outputPath = outputPath
    }

    public  void processScenarios() {
        File sootResultsFile = createOutputFile()
        List<SootScenario> sootScenarios = SootScenario.readScenarios(outputPath + RESULTS_FILE_PATH);
        
        for (scenario in sootScenarios) {
            println "Running soot scenario ${scenario.getCommitSHA()}"
            String filePath = scenario.getLinesFile(outputPath)
            String filePathReverse = scenario.getLinesFile(outputPath)
            String classPath = scenario.getClassPath(outputPath)

            println "Running left right dataflow analysis"
            Process analysisLeftRightDataflow = runSootAnalysis(filePath, classPath, DATAFLOW_MODE)
            String leftRightDataflow = hasSootFlow(analysisLeftRightDataflow)

            println "Running right left dataflow analysis"
            Process analysisRightLeftDataflow = runSootAnalysis(filePathReverse, classPath, DATAFLOW_MODE)
            String rightLeftDataflow = hasSootFlow(analysisRightLeftDataflow)

            println "Running left right reachability analysis"
            Process analysisLeftRightReachability = runSootAnalysis(filePath, classPath, REACHABILITY_MODE)
            String leftRightReachability = hasSootFlow(analysisLeftRightReachability)

            println "Running right left reachability analysis"
            Process analysisRightLeftReachability = runSootAnalysis(filePathReverse, classPath, REACHABILITY_MODE)
            String rightLeftReachability = hasSootFlow(analysisRightLeftReachability)

            sootResultsFile << "${scenario.toString()};${leftRightDataflow};${rightLeftDataflow};${leftRightReachability};${rightLeftReachability}\n"
        }

    }

    private File createOutputFile() {
        File sootResultsFile = new File(outputPath + "/data/soot-results.csv")

        if (sootResultsFile.exists()) {
            sootResultsFile.delete()
        }

        sootResultsFile << "project;class;method;merge commit;dataflow left right;dataflow right left;reachability left right;reachability right left\n"
    
        return sootResultsFile
    }

    private String hasSootFlow (Process sootProcess) {
        String result = "error"

        sootProcess.getInputStream().eachLine {
            println it
            if (it.stripIndent().startsWith("Number of conflicts:")) {
                result = "true"
            } else if (it.stripIndent() == "No conflicts detected") {
                result = "false"
            }
        }
        return result
    }

    private Process runSootAnalysis (String filePath, String classPath, String mode) {
        return ProcessRunner
            .runProcess(".", "java", "-jar" ,"dependencies/soot-analysis.jar", "-csv", filePath, "-cp", classPath, "-mode", mode)
    }
}