package services.outputProcessors.soot

class Main {

    static main(args) {
        String outputPath = "output"

        RunSootAnalysisOutputProcessor sootRunner = new RunSootAnalysisOutputProcessor();

        sootRunner.executeAllAnalyses(outputPath);
    }

}
