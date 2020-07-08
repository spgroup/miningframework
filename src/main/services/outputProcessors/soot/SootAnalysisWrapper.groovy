package services.outputProcessors.soot

import util.ProcessRunner

class SootAnalysisWrapper {
    private String version;

    SootAnalysisWrapper(String version) {
        this.version = version;
    }

    Process executeSoot(String inputFilePath, String classPath, String mode) {
        return ProcessRunner.runProcess(".",
                "java", "-jar" , getJarPath(),
                "-csv", inputFilePath,
                "-cp", classPath,
                "-mode", mode);
    }

    String getSootAnalysisVersionDisclaimer() {
        return "# This results were produced by soot-analysis v" + this.version + "\n";
    }

    private String getJarPath() {
        return "dependencies/soot-analysis-${version}-jar-with-dependencies.jar"
    }

}
