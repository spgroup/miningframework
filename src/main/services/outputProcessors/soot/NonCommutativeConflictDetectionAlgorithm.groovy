package services.outputProcessors.soot


/**
 * Runs a soot algorithm twice, once with:
 * left -> source
 * right -> sink
 * and once with:
 * left -> sink
 * right -> source
 * This is used for algorithms that are non commutative, that means different conflicts can be found running from
 * left to right and right to left
 */
class NonCommutativeConflictDetectionAlgorithm extends ConflictDetectionAlgorithm {

    NonCommutativeConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout) {
        super(name, mode, sootWrapper, timeout)
    }

    NonCommutativeConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout, boolean interprocedural) {
        super(name, mode, sootWrapper, timeout, interprocedural)
    }

    NonCommutativeConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout, boolean interprocedural, long depthLimit) {
        super(name, mode, sootWrapper, timeout, interprocedural, depthLimit);
    }

    @Override
    String generateHeaderName() {
        return "left right ${this.name};right left ${this.name}"
    }

    @Override
    String run(Scenario scenario) {
        try {
            SootConfig configLeftRight = buildSootConfig(scenario.getLinesFilePath(), scenario);
            SootConfig configRightLeft = buildSootConfig(scenario.getLinesReverseFilePath(), scenario);

            println "Running left right " + toString();
            String leftRightResult = super.runAndReportResult(configLeftRight);

            println "Running right left " + toString();
            String rightLeftResult = super.runAndReportResult(configRightLeft);

            return "${leftRightResult};${rightLeftResult}";
        } catch (ClassNotFoundInJarException e) {
            return "not-found;not-found";
        }
    }

    private SootConfig buildSootConfig(String filePath, Scenario scenario) {
        SootConfig config = new SootConfig(filePath, scenario.getClassPath(), this.mode);
        config.addOption("-entrypoints", scenario.getEntrypoints());
        config.addOption("-depthLimit", this.getDepthLimit());
        return config;
    }

    @Override
    public String toString() {
        return "NonCommutativeConflictDetectionAlgorithm{name = ${this.name}}";
    }
}
