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

    ConflictDetectionAlgorithm(String name) {
        this.name = name
        this.timeout = null;
    }

    ConflictDetectionAlgorithm(String name, long timeout) {
        this.name = name;
        this.timeout = timeout;
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

        Process sootProcess = runProcess(filePath, classPath);

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

    private Process runProcess (String inputFilePath, String classPath) {
        return ProcessRunner
                .runProcess(".", "java", "-jar" ,"dependencies/soot-analysis.jar", "-csv", inputFilePath, "-cp", classPath, "-mode", this.name)
    }

    private String hasSootFlow (Process sootProcess) {
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
