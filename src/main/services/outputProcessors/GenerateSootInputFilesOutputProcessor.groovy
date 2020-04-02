
package services.outputProcessors


import exception.ExternalScriptException
import interfaces.OutputProcessor
import util.ProcessRunner

import static app.MiningFramework.arguments

class GenerateSootInputFilesOutputProcessor implements OutputProcessor {
    
    private final String SCRIPT_RUNNER = "python3"
    private final String PARSE_TO_SOOT_PATH = "./scripts/parse_to_soot.py"

    void processOutput() {
        if (arguments.providedAccessKey())
            convertToSootScript(arguments.getOutputPath())
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