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
    private String mode;
    private long timeout;
    private SootAnalysisWrapper sootWrapper;
    private boolean interprocedural;
    private long depthLimit;


    ConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout) {
        this(name, mode, sootWrapper, timeout, false, 5);
    }

    ConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout, boolean interprocedural) {
        this(name, mode, sootWrapper, timeout, interprocedural, 5);
    }

    ConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout, boolean interprocedural, long depthLimit) {
        this.name = name;
        this.mode = mode;
        this.timeout = timeout;
        this.sootWrapper = sootWrapper;
        this.interprocedural = interprocedural;
        this.depthLimit = depthLimit;
    }

    String getName() {
        return name
    }

    void setTimeout(long timeout) {
        this.timeout = timeout
    }

    boolean getInterprocedural() {
        return interprocedural
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

    long getDepthLimit() {
        return depthLimit
    }

    String run(Scenario scenario) {
        try {
            println "Running ${toString()}"
            String filePath = scenario.getLinesFilePath()
            String classPath = scenario.getClassPath()

            SootConfig sootConfig = new SootConfig(
                    filePath,
                    classPath,
                    this.mode
            );

            sootConfig.addOption("-entrypoints", scenario.getEntrypoints());
            sootConfig.addOption("-depthLimit", this.getDepthLimit());

            return runAndReportResult(sootConfig);
        } catch (ClassNotFoundInJarException e) {
            return "not-found";
        }
    }


    protected String runAndReportResult(SootConfig sootConfig) throws InterruptedException, IOException {
        String result;
        println "Using jar at " + sootConfig.getClassPath()

        File inputFile = new File(sootConfig.getInputFilePath());
        if (!inputFile.exists()) {
            println "This scenario has no changes";
            return "false";
        }

        Process sootProcess = sootWrapper.executeSoot(sootConfig);

        // this is needed because if th waitFor command is called without reading the output
        // in some executions the output buffer might get full and block the process
        // so we execute both the output reading and the process waiting in parallel
        Thread processOutputThread = new Thread(new Runnable() {
            @Override
            void run() {
                result = hasSootFlow(sootProcess);
            }
        })
        processOutputThread.start(); // start processing the output

        boolean executionCompleted = true;
        if (timeout > 0) {
            // wait for the execution to end setting a timeout
            executionCompleted = sootProcess.waitFor(timeout, TimeUnit.SECONDS)
        }

        // if the timeout has been reached
        if (!executionCompleted) {
            processOutputThread.interrupt(); // cancel the output reading thread
            print ("Execution exceeded the timeout of " + timeout + " seconds")
            result = "timeout";
        } else {
            processOutputThread.join();
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
