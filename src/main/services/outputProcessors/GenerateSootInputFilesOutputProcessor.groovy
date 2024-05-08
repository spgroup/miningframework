
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
    private static final String PARSE_TO_SOOT_FILENAME = "parse_to_soot.py"
    private String parseToSootPath;

    /**
     * Default constructor.
     * Assumes the path to the script parse_to_soot.py as the 'scripts' directory in the root of the project.
     */
    public GenerateSootInputFilesOutputProcessor() {
        this("./scripts/" + PARSE_TO_SOOT_FILENAME);
    }

    /**
     * Receives the path to the scripts folder containing the parse_to_soot.py script as a parameter, in cases where the class is used as a library.
     * @param scriptsPath The path to the scripts folder containing the parse_to_soot.py script.
     */
    public GenerateSootInputFilesOutputProcessor(String scriptsPath) {
        this.parseToSootPath = new File(scriptsPath).getPath() + "/" + PARSE_TO_SOOT_FILENAME;
    }

    void processOutput() {
        if (arguments.providedAccessKey())
            convertToSootScript(arguments.getOutputPath())
    }

    void convertToSootScript (String outputPath) {
        println "Running parse_to_soot script"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, this.parseToSootPath, outputPath)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        int exitStatus = process.waitFor()

        if (exitStatus != 0) {
            throw new ExternalScriptException(this.parseToSootPath, exitStatus);
        }
    }

}