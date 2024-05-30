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

    NonCommutativeConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper) {
        super(name, mode, sootWrapper)
    }

    NonCommutativeConflictDetectionAlgorithm(String name, String mode, SootAnalysisWrapper sootWrapper, long timeout) {
        super(name, mode, sootWrapper, timeout)
    }

    @Override
    String generateHeaderName() {
        return "left right ${this.name};right left ${this.name}"
    }

    @Override
    String run (Scenario scenario) {
        try {
            String filePath = scenario.getLinesFilePath()
            String classPath = scenario.getClassPath()
            String filePathReverse = scenario.getLinesReverseFilePath()
            String entrypoints = scenario.getEntrypoints()

            println "Running left right " + toString();
            String leftRightResult = super.runAndReportResult(filePath, classPath, entrypoints)
            println "Running right left " + toString();
            String rightLeftResult = super.runAndReportResult(filePathReverse, classPath, entrypoints)

            return "${leftRightResult};${rightLeftResult}";
        } catch (ClassNotFoundInJarException e) {
            return "not-found;not-found"
        }

    }

    @Override
    public String toString() {
        return "NonCommutativeConflictDetectionAlgorithm{name = ${this.name}}";
    }
}
