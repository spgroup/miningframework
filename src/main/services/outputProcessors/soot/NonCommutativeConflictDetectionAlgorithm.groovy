package services.outputProcessors.soot

class NonCommutativeConflictDetectionAlgorithm extends ConflictDetectionAlgorithm {

    NonCommutativeConflictDetectionAlgorithm(String name) {
        super(name)
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
