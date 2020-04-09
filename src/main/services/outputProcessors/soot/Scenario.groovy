package services.outputProcessors.soot

import static com.xlson.groovycsv.CsvParser.parseCsv

class SootScenario {

    private String projectName;
    private String className;
    private String methodSignature;
    private String commitSHA;

    private String scenarioDirectory;

    SootScenario (projectName, className, methodSignature, commitSHA, scenarioDirectory) {
        this.projectName = projectName;
        this.className = className;
        this.methodSignature = methodSignature;
        this.commitSHA = commitSHA;
        this.scenarioDirectory = scenarioDirectory;
    }

    String getCommitSHA () {
        return this.commitSHA;
    }

    String toString() {
        return "${projectName};${className};${methodSignature};${commitSHA}"
    }

    String getLinesFilePath () {
        return "${this.scenarioDirectory}/soot.csv"
    }

    String getLinesReverseFilePath () {
        return "${this.scenarioDirectory}/soot-reverse.csv"
    }
    
    String getClassPath() {
        File file = new File("${this.scenarioDirectory}/build");

        String separator = ":"
        if (isWindows()) {
            separator = ";"
        }

        def buildJars = file.listFiles()
        StringBuilder result = new StringBuilder()

        for (int i = 0 ; i < buildJars.length ; i++) {
            result.append(buildJars[i])
            if (buildJars.length - 1 > i) {
                result.append(separator)
            }
        }

        return result.toString()
    }

    private boolean isWindows() {
        return System.getProperty("os.name") == "Windows"   
    }

}