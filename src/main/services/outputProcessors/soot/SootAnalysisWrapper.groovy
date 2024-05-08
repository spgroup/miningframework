package services.outputProcessors.soot

import util.ProcessRunner

class SootAnalysisWrapper {
    private String version;
    private String dependenciesPath;

    /**
     * Assumes the path to the soot analysis executable as the dependencies folder in the root of the project.
     * @param version The version of the soot analysis tool.
     */
    SootAnalysisWrapper(String version) {
        this(version, "dependencies");
    }

    /**
     * Receives the path to the dependencies folder containing the soot-analysis jar as a parameter, in cases where the class is used as a library.
     * @param version The version of the soot analysis tool.
     * @param dependenciesPath The path to the dependencies folder containing the soot-analysis jar.
     */
    SootAnalysisWrapper(String version, String dependenciesPath) {
        this.version = version;
        this.dependenciesPath = dependenciesPath;
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

    public String getJarPath() {
        return this.dependenciesPath + "/soot-analysis-${version}-jar-with-dependencies.jar"
    }

}
