package services.outputProcessors.soot

import exception.ExternalScriptException
import exception.InvalidArgsException
import services.outputProcessors.soot.arguments.ArgsParser
import services.outputProcessors.soot.arguments.Arguments
import util.ProcessRunner

class Main {

    private static final String RESULT_ANALYSIS_PATH = "../miningframework/scripts/experiment_static_analysis/generate_analysis_results.py"
    private static final String SCRIPT_RUNNER = "python3"

    static main(args) {
        SootAnalysisWrapper sootWrapper = new SootAnalysisWrapper("0.2.1-SNAPSHOT")
        String outputPath = "output"

        ArgsParser argsParser = new ArgsParser()
        Arguments appArguments;

        File consoleFile = null;
        FileOutputStream file = null;
        try {
            consoleFile = new File("outConsole.txt");
            consoleFile.createNewFile(); // if file already exists will do nothing
            file = new FileOutputStream(consoleFile, false);
            TeePrintStream tee = new TeePrintStream(file, System.out);
            System.setOut(tee);

            appArguments = argsParser.parse(args)

            if (appArguments.isHelp()) {
                argsParser.printHelp()
            } else {
                RunSootAnalysisOutputProcessor sootRunner = new RunSootAnalysisOutputProcessor();

                if (appArguments.getAllanalysis()) {
                    sootRunner.configureDetectionAlgorithmsTimeout(appArguments.getTimeout())
                } else {
                    sootRunner.setDetectionAlgorithms(configureDetectionAlgorithms(appArguments, sootWrapper))
                }

                sootRunner.executeAnalyses(outputPath)

                if (appArguments.isReport()) {
                    reportResults(outputPath)
                }

            }

        } catch (IOException e) {
            if (file != null) {
                file.close();
            }
            e.printStackTrace();
        } catch (InvalidArgsException e) {
            println e.message
            println 'Run the miningframework with --help to see the possible arguments'
        }

    }

    public static void reportResults(outputPath) {
        println "Running generate_analysis_results"
        ProcessBuilder builder = ProcessRunner.buildProcess(".", SCRIPT_RUNNER, RESULT_ANALYSIS_PATH, outputPath)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)

        Process process = ProcessRunner.startProcess(builder)
        int exitStatus = process.waitFor()

        if (exitStatus != 0) {
            throw new ExternalScriptException(RESULT_ANALYSIS_PATH, exitStatus);
        }
    }

    private static ArrayList<ConflictDetectionAlgorithm> configureDetectionAlgorithms(Arguments appArguments, SootAnalysisWrapper sootWrapper) {
        List<ConflictDetectionAlgorithm> detectionAlgorithms = new ArrayList<ConflictDetectionAlgorithm>();


        long timeout = appArguments.getTimeout()
        long depthLimit = appArguments.getDepthLimit();
        if (appArguments.getDfIntra()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DF Intra", "svfa-intraprocedural", sootWrapper, timeout, false,depthLimit ))
        }
        if (appArguments.getDfInter()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DF Inter", "svfa-interprocedural", sootWrapper, timeout, true,depthLimit))
        }
        if (appArguments.getCfIntra()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Confluence Intra", "dfp-confluence-intraprocedural", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getCfInter()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Confluence Inter", "dfp-confluence-interprocedural", sootWrapper, timeout, true,depthLimit))
        }
        if (appArguments.getOaIntra()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Intra", "oa", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getOaInter()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Inter", "ioa", sootWrapper, timeout, true,depthLimit))
        }
        if (appArguments.getOaIntraWithoutPA()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Intra Without Pointer Analysis", "oa-without-pa", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getOaInterWithoutPA()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Inter Without Pointer Analysis", "ioa-without-pa", sootWrapper, timeout, true,depthLimit))
        }

        if (appArguments.getDfpIntra()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DFP-Intra", "dfp-intra", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getDfpInter()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DFP-Inter", "dfp-inter", sootWrapper, timeout, true,depthLimit))
        }
        if (appArguments.getCd()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("CD", "cd", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getCde()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("CDe", "cd-e", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getPdg()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("PDG", "pdg", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getPdge()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("PDG-e", "pdg-e", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getPessimisticDataflow()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Pessimistic Dataflow", "pessimistic-dataflow", sootWrapper, timeout, false,depthLimit))
        }
        if (appArguments.getReachability()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Reachability", "reachability", sootWrapper, timeout, false,depthLimit))
        }

        return detectionAlgorithms;
    }
}

public class TeePrintStream extends PrintStream {
    private final PrintStream second;

    public TeePrintStream(OutputStream main, PrintStream second) {
        super(main);
        this.second = second;
    }

    /**
     * Closes the main stream.
     * The second stream is just flushed but <b>not</b> closed.
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close() {
        // just for documentation
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
        second.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        second.write(buf, off, len);
    }

    @Override
    public void write(int b) {
        super.write(b);
        second.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        second.write(b);
    }
}
