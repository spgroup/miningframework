
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


    SootRunner(String outputPath, String sootProjectPath) {
        this.outputPath = outputPath
        this.sootPath = sootProjectPath
    }

    public  void processScenarios() {
        List<SootScenario> sootScenarios = SootScenario.readScenarios(outputPath + RESULTS_FILE_PATH);

        for (scenario in sootScenarios) {

            String filePath = scenario.getLinesFile(outputPath)
            String filePathReverse = scenario.getLinesFile(outputPath)
            String className = scenario.getClassPath(outputPath)

            String analysisOutputLeftRight = runSootAnalysis(filePath, className)
            String analysisOutputRightLeft = runSootAnalysis(filePathReverse, className)
        }

    }


    public String runSootAnalysis (String filePath, String classPath) {
        String cliParameters = "-csv ${filePath} -cp ${classPath}"
        Process analysis = ProcessRunner
            .runProcess(sootPath, "mvn", "exec:java" ,"-Dexec.mainClass=br.unb.cic.analysis.Main", "-Dexec.args=${cliParameters}")
    
        return analysis.getText()
    }

}