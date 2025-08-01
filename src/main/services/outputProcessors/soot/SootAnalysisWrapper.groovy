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

    Process executeSoot(SootConfig config) {
        List<String> command = new ArrayList<>(Arrays.asList(
                "java", "-jar", getJarPath(),
                "-csv", config.getInputFilePath(),
                "-cp", config.getClassPath(),
                "-mode", config.getMode()
        ));

        config.getOptionalParams().forEach((flag, value) -> {
            command.add(flag);
            command.add(value);
        });

        return ProcessRunner.runProcess(".", command.toArray(new String[0]));
    }


    String getSootAnalysisVersionDisclaimer() {
        return "# This results were produced by soot-analysis v" + this.version + "\n";
    }

    public String getJarPath() {
        return this.dependenciesPath + "/soot-analysis-${version}-jar-with-dependencies.jar"
    }

}
