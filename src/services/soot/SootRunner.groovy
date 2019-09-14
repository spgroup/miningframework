
package services.soot

import main.interfaces.OutputProcessor

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*
import main.exception.*
import java.nio.file.Files 
import java.nio.file.Paths
import java.nio.file.Path

class SootRunner {

    private String outputPath
    private String sootPath 

    private final String RESULTS_FILE_PATH = "/data/results-with-builds.csv"
    private final String DATAFLOW_MODE = "dataflow"
    private final String REACHABILITY_MODE = "reachability"

    SootRunner(String outputPath) {
        this.outputPath = outputPath
    }

    public  void processScenarios() {
        String resultHead = "project;class;method;commit;dataflow left right;dataflow right left;reachability left right;reachability right left\n"

        List<SootScenario> sootScenarios = SootScenario.readScenarios(outputPath + RESULTS_FILE_PATH);

        for (scenario in sootScenarios) {

            String filePath = scenario.getLinesFile(outputPath)
            String filePathReverse = scenario.getLinesFile(outputPath)
            String classPath = scenario.getClassPath(outputPath)

            println "Running left right dataflow analysis"
            Process analysisLeftRightDataflow = runSootAnalysis(filePath, classPath, DATAFLOW_MODE)
            boolean leftRightDataflow = hasSootFlow(analysisLeftRightDataflow)
            println "Result: ${leftRightDataflow}"

            println "Running right left dataflow analysis"
            Process analysisRightLeftDataflow = runSootAnalysis(filePathReverse, classPath, DATAFLOW_MODE)
            boolean rightLeftDataflow = hasSootFlow(analysisRightLeftDataflow)
            println "Result: ${rightLeftDataflow}"

            println "Running left right reachability analysis"
            Process analysisLeftRightReachability = runSootAnalysis(filePath, classPath, DATAFLOW_MODE)
            boolean leftRightReachability = hasSootFlow(analysisLeftRightReachability)
            println "Result: ${leftRightReachability}"


            println "Running right left reachability analysis"
            Process analysisRightLeftReachability = runSootAnalysis(filePathReverse, classPath, DATAFLOW_MODE)
            boolean rightLeftReachability = hasSootFlow(analysisRightLeftReachability)
            println "Result: ${rightLeftReachability}"

        }

    }

    public boolean hasSootFlow (Process sootProcess) {
        boolean result = true
        sootProcess.getInputStream().eachLine {
            if (it.stripIndent() == "No conflicts detected") {
                result = false
            }
        }
        return result
    }

    public Process runSootAnalysis (String filePath, String classPath, String mode) {
        return ProcessRunner
            .runProcess(".", "java", "-jar" ,"dependencies/soot-analysis.jar", "-csv", filePath, "-cp", classPath, "-mode", mode)
    }

    static main (args) {
        SootRunner runner = new SootRunner("output")

        runner.processScenarios()
    }
}