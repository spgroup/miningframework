package services.outputProcessors.soot

import util.ProcessRunner

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

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
    private String callgraph;
    private boolean partialResultsOnTimeout;

    // Grace period to let the output thread finish reading buffered output after process is destroyed
    private static final long GRACE_PERIOD_MILLIS = 5000L;


    ConflictDetectionAlgorithm(String name,
                               String mode,
                               SootAnalysisWrapper sootWrapper,
                               long timeout,
                               boolean interprocedural = false,
                               long depthLimit = 5,
                               String callgraph = "SPARK",
                               boolean partialResultsOnTimeout = false) {
        this.name = name
        this.mode = mode
        this.sootWrapper = sootWrapper
        this.timeout = timeout
        this.interprocedural = interprocedural
        this.depthLimit = depthLimit
        this.callgraph = callgraph
        this.partialResultsOnTimeout = partialResultsOnTimeout
    }

    String getMode() {
        return mode
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

    void setPartialResultsOnTimeout(boolean partialResultsOnTimeout) {
        this.partialResultsOnTimeout = partialResultsOnTimeout
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

    String getCallgraph() {
        return callgraph
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
            sootConfig.addOption("-cg", this.getCallgraph());

            return runAndReportResult(sootConfig);
        } catch (ClassNotFoundInJarException e) {
            return "not-found";
        }
    }


    protected String runAndReportResult(SootConfig sootConfig) throws InterruptedException, IOException {
        // AtomicReference allows the output thread to safely publish its result to the main thread
        // Default is "false": if timeout fires before any [CONFLICT_FOUND] is seen, we record false
        AtomicReference<String> atomicResult = new AtomicReference<>("false")

        println "Using jar at " + sootConfig.getClassPath()

        File inputFile = new File(sootConfig.getInputFilePath());
        if (!inputFile.exists()) {
            println "This scenario has no changes";
            return "false";
        }

        Process sootProcess = sootWrapper.executeSoot(sootConfig);

        // Reading output and waiting for process must run in parallel to avoid blocking
        // when the output buffer fills up before the process finishes
        Thread processOutputThread = new Thread(new Runnable() {
            @Override
            void run() {
                atomicResult.set(hasSootFlow(sootProcess));
            }
        })
        processOutputThread.start();

        boolean executionCompleted = true;
        if (timeout > 0) {
            executionCompleted = sootProcess.waitFor(timeout, TimeUnit.SECONDS)
        }

        if (!executionCompleted) {
            println "Execution exceeded the timeout of ${timeout} seconds"
            // Destroy the process first so its output stream closes, allowing the reader thread to exit
            sootProcess.destroy();

            if (partialResultsOnTimeout) {
                // Wait for the reader thread to finish consuming any buffered output that was
                // already in the pipe before the process was destroyed
                processOutputThread.join(GRACE_PERIOD_MILLIS)
                String partial = atomicResult.get()
                println "Result at timeout: ${partial}"
                return partial + "-timeout"
            } else {
                processOutputThread.interrupt();
            }
            return "timeout";
        }

        processOutputThread.join();
        // Force destroy to prevent zombie processes that keep consuming memory
        sootProcess.destroy();

        return atomicResult.get();
    }

    private String hasSootFlow(Process sootProcess) {
        String result = "false"
        try {
            sootProcess.getInputStream().eachLine {
                println it;
                if (it.stripIndent() == "[CONFLICT_FOUND]") {
                    result = "true"
                }
            }
        } catch (IOException ignored) {
            // Stream closed because the process was destroyed (timeout case) — return whatever was found so far
        }
        return result
    }

}
