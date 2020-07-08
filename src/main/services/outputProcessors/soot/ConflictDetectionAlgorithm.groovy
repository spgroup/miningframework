package services.outputProcessors.soot

import util.ProcessRunner

import java.util.concurrent.TimeUnit

/**
 * Runs a soot algorithm with:
 * left -> sink
 * right -> source
 * This case is used with algorithms that are commutative, that means that running them from left to right is the same
 * thing  as running them from right to left
 */
class ConflictDetectionAlgorithm {

    private String name;
    private Long timeout;
    private SootAnalysisWrapper sootWrapper;

    ConflictDetectionAlgorithm(String name, SootAnalysisWrapper sootWrapper) {
        this.name = name
        this.timeout = null;
        this.sootWrapper = sootWrapper;
    }

    ConflictDetectionAlgorithm(String name, SootAnalysisWrapper sootWrapper, long timeout) {
        this.name = name;
        this.timeout = timeout;
        this.sootWrapper = sootWrapper;
    }

    String getName() {
        return name
    }

    @Override
    String toString() {
        return "ConflictDetectionAlgorithm{" +
                "name='" + name + '\'' +
                '}';
    }

    String generateHeaderName() {
        return this.name;
    }

    String run (Scenario scenario) {
        println "Running ${toString()}"
        String filePath = scenario.getLinesFilePath()
        String classPath = scenario.getClassPath()

        return runAndReportResult(filePath, classPath);
    }

    private String runAndReportResult (String filePath, String classPath) {
        String result;
        println "Using jar at " + classPath

        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            println "This scenario has no changes";
            return "false";
        }

        Process sootProcess = sootWrapper.executeSoot(filePath, classPath, this.name);

        boolean executionCompleted = true;
        if (timeout != null) {
            executionCompleted = sootProcess.waitFor(timeout, TimeUnit.SECONDS)
        }

        if (executionCompleted) {
            result = hasSootFlow(sootProcess);
        } else {
            println "Execution exceeded timeout: " + timeout + " seconds";
            result = "timeout";
        }

        // force destroy process
        // if we don't use this command some processes will keep running and consuming a lot of memory
        // even after the analysis execution ends
        sootProcess.destroy();

        return result;
    }

    private String hasSootFlow(Process sootProcess) {
        String result = "error"

        sootProcess.getInputStream().eachLine {
            println it;
            if (it.stripIndent().startsWith("Number of conflicts:")) {
                result = "true"
            } else if (it.stripIndent() == "No conflicts detected") {
                result = "false"
            }
        }
        return result
    }
}
