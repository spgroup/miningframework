package services.outputProcessors.soot

/**
 * Runs a soot algorithm twice, once with:
 * left -> source
 * right -> sink
 * and once with:
 * left -> sink
 * right -> source
 * This is used for algorithms that are non-commutative, meaning that
 * running left→right may produce different conflicts than right→left.
 */
class NonCommutativeConflictDetectionAlgorithm extends ConflictDetectionAlgorithm {

    NonCommutativeConflictDetectionAlgorithm(String name,
                                             String mode,
                                             SootAnalysisWrapper sootWrapper,
                                             long timeout,
                                             boolean interprocedural = false,
                                             long depthLimit = 5,
                                             String callgraph = "SPARK") {
        super(name, mode, sootWrapper, timeout, interprocedural, depthLimit, callgraph)
    }

    @Override
    String generateHeaderName() {
        return "left right ${this.name};right left ${this.name}"
    }

    @Override
    String run(Scenario scenario) {
        try {
            println "Running ${toString()}"

            // LEFT → RIGHT
            String filePathLeftRight = scenario.getLinesFilePath()
            SootConfig configLeftRight = buildSootConfig(filePathLeftRight, scenario)

            println "Running left → right ${toString()}"
            String leftRightResult = runAndReportResult(configLeftRight)

            // RIGHT → LEFT
            String filePathRightLeft = scenario.getLinesReverseFilePath()
            SootConfig configRightLeft = buildSootConfig(filePathRightLeft, scenario)

            println "Running right → left ${toString()}"
            String rightLeftResult = runAndReportResult(configRightLeft)

            return "${leftRightResult};${rightLeftResult}"
        } catch (ClassNotFoundInJarException e) {
            return "not-found;not-found";
        }
    }

    private SootConfig buildSootConfig(String filePath, Scenario scenario) {
        SootConfig config = new SootConfig(filePath, scenario.getClassPath(), super.getMode());

        config.addOption("-entrypoints", scenario.getEntrypoints());
        config.addOption("-depthLimit", this.getDepthLimit());
        config.addOption("-cg", getCallgraph())

        return config;
    }

    @Override
    public String toString() {
        return "NonCommutativeConflictDetectionAlgorithm{name = ${super.getName()}}";
    }
}
