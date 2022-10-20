package services.outputProcessors.soot

import exception.InvalidArgsException
import services.outputProcessors.soot.arguments.ArgsParser
import services.outputProcessors.soot.arguments.Arguments

class Main {

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

    private static ArrayList<ConflictDetectionAlgorithm> configureDetectionAlgorithms(Arguments appArguments, SootAnalysisWrapper sootWrapper) {
        List<ConflictDetectionAlgorithm> detectionAlgorithms = new ArrayList<ConflictDetectionAlgorithm>();

        if (appArguments.getDfIntra()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DF Intra", "svfa-intraprocedural", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getDfInter()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DF Inter", "svfa-interprocedural", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getCfIntra()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Confluence Intra", "dfp-confluence-intraprocedural", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getCfInter()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Confluence Inter", "dfp-confluence-interprocedural", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getOaIntra()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Intra", "overriding-intraprocedural", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getOaInter()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("OA Inter", "overriding-interprocedural", sootWrapper, appArguments.getTimeout()))
        }

        if (appArguments.getDfpIntra()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DFP-Intra", "dfp-intra", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getDfpInter()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("DFP-Inter", "dfp-inter", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getCd()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("CD", "cd", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getCde()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("CDe", "cd-e", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getPdg()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("PDG", "pdg", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getPdge()) {
            detectionAlgorithms.add(new NonCommutativeConflictDetectionAlgorithm("PDG-e", "pdg-e", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getPessimisticDataflow()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Pessimistic Dataflow", "pessimistic-dataflow", sootWrapper, appArguments.getTimeout()))
        }
        if (appArguments.getReachability()) {
            detectionAlgorithms.add(new ConflictDetectionAlgorithm("Reachability", "reachability", sootWrapper, appArguments.getTimeout()))
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
