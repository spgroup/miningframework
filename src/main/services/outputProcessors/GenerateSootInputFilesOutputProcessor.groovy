
package services.outputProcessors


import exception.ExternalScriptException
import interfaces.OutputProcessor
import util.ProcessRunner

import static app.MiningFramework.arguments

/**
 * @requires: python3 installed on the machine and that the outputProcessor FetchBuildsOutputProcessor was ran before
 * @provides: files soot.csv and soot-reverse.csv on the [outputPath]/files/[projectName]/[mergeCommit]/ folder with the
 * format expected by the soot-analysis tool for specifying scenarios source and sink
 */
class GenerateSootInputFilesOutputProcessor implements OutputProcessor {
    
    private final String SCRIPT_RUNNER = "python3"
    private final String PARSE_TO_SOOT_PATH = "./scripts/parse_to_soot.py"

    void processOutput() {
        if (arguments.providedAccessKey())
            convertToSootScript(arguments.getOutputPath())
    }

    void convertToSootScript (String outputPath) {
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