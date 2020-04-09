package services.outputProcessors.soot

class SootNonCommutativeAlgorithm extends SootAlgorithm {

    SootNonCommutativeAlgorithm(String name) {
        super(name)
    }

    @Override
    String generateHeaderName() {
        return "left right ${this.name};right left ${this.name}"
    }

    @Override
    String run (SootScenario scenario) {
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
        return "SootNonCommutativeAlgorithm{name = ${this.name}}";
    }
}
