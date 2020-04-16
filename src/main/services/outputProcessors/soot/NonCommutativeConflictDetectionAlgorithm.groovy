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

    NonCommutativeConflictDetectionAlgorithm(String name) {
        super(name)
    }

    NonCommutativeConflictDetectionAlgorithm(String name, long timeout) {
        super(name, timeout)
    }

    @Override
    String generateHeaderName() {
        return "left right ${this.name};right left ${this.name}"
    }

    @Override
    String run (Scenario scenario) {
        String filePath = scenario.getLinesFilePath()
        String classPath = scenario.getClassPath()
        String filePathReverse = scenario.getLinesReverseFilePath()

        println "Running left right " + toString();
        String leftRightResult = super.runAndReportResult(filePath, classPath)
        println "Running right left " + toString();
        String rightLeftResult = super.runAndReportResult(filePathReverse, classPath)

        return "${leftRightResult};${rightLeftResult}";
    }

    @Override
    public String toString() {
        return "NonCommutativeConflictDetectionAlgorithm{name = ${this.name}}";
    }
}
