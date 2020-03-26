
package services

import main.interfaces.OutputProcessor

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*
import main.exception.*
import services.soot.SootRunner

class OutputProcessorImpl implements OutputProcessor {
    
    private final String SCRIPT_RUNNER = "python3"
    private final String FETCH_JARS_PATH = "./scripts/fetch_multiple_jar_per_scenario.py"
    private final String PARSE_TO_SOOT_PATH = "./scripts/parse_to_soot.py"

    public void processOutput() {
        if (arguments.providedAccessKey()) {

            String inputPath = arguments.getInputPath()
            String outputPath = arguments.getOutputPath()
            String token = arguments.getAccessKey()

            fetchBuildsScript(inputPath, outputPath, token)
        }
    }

    private void fetchBuildsScript (String inputPath, String outputPath, String token) {
        println "Running fetch_jars script"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, FETCH_JARS_PATH, inputPath, outputPath, token)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        process.waitFor()
    }

    private void convertToSootScript (String outputPath) {
        println "Running parse_to_soot script"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, PARSE_TO_SOOT_PATH, outputPath)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        process.waitFor()
    }

}