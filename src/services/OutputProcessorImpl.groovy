
package services

import main.interfaces.OutputProcessor

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*
import main.exception.*
import services.soot.SootRunner

import main.exception.ExternalScriptException

class OutputProcessorImpl implements OutputProcessor {
    
    private final String SCRIPT_RUNNER = "python3"
    private final String FETCH_JARS_PATH = "./scripts/fetch_jars.py"
    private final String PARSE_TO_SOOT_PATH = "./scripts/parse_to_soot.py"

    public void processOutput() {
        if (arguments.providedAccessKey()) {

            String inputPath = arguments.getInputPath()
            String outputPath = arguments.getOutputPath()
            String token = arguments.getAccessKey()

            fetchBuildsScript(inputPath, outputPath, token)
            convertToSootScript(outputPath)

            SootRunner runner = new SootRunner(outputPath)
            runner.processScenarios()
        }
    }

    private void fetchBuildsScript (String inputPath, String outputPath, String token) {
        println "Running fetch_jars script"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, FETCH_JARS_PATH, inputPath, outputPath, token)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        int exitStatus = process.waitFor()

        if (exitStatus != 0) {
            throw new ExternalScriptException(FETCH_JARS_PATH, exitStatus);
        }
    }

    private void convertToSootScript (String outputPath) {
        println "Running parse_to_soot script"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, PARSE_TO_SOOT_PATH, outputPath)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        int exitStatus = process.waitFor()

        if (exitStatus != 0) {
            throw new ExternalScriptException(PARSE_TO_SOOT_PATH, exitStatus);
        }
    }

}