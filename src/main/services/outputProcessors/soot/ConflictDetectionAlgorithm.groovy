package services.outputProcessors.soot

import util.ProcessRunner

import java.util.concurrent.TimeUnit

class SootAlgorithm {

    private String name;
    private final long TIMEOUT = 30;

    SootAlgorithm (String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    @Override
    String toString() {
        return "SootAlgorithm{" +
                "name='" + name + '\'' +
                '}';
    }

    String generateHeaderName() {
        return this.name;
    }

    String run (SootScenario scenario) {
        println "Running ${toString()}"
        String filePath = scenario.getLinesFilePath()
        String classPath = scenario.getClassPath()

        return runAndReportResult(filePath, classPath);
    }

    private String runAndReportResult (String filePath, String classPath) {
        String result;

        Process sootProcess = runProcess(filePath, classPath);
        boolean executionCompleted = sootProcess.waitFor(TIMEOUT, TimeUnit.SECONDS)
        if (executionCompleted) {
            result = hasSootFlow(sootProcess);
        } else {
            println "Execution exceeded timeout: " + TIMEOUT + " seconds";
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
