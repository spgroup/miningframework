package services.outputProcessors.soot

import util.JarHelper

import static com.xlson.groovycsv.CsvParser.parseCsv

class Scenario {

    private String projectName;
    private String className;
    private String methodSignature;
    private String commitSHA;

    private String scenarioDirectory;

    Scenario(projectName, className, methodSignature, commitSHA, scenarioDirectory) {
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
        File[] buildJars = file.listFiles()

        String buildJarPath = getJarThatHasClass(buildJars)
        if (buildJarPath == null) {
            throw new ClassNotFoundInJarException(this.className)
        }
        return buildJarPath
    }

    private String getJarThatHasClass(File[] buildJars) {
        File resultJar = null;
        for (int i = 0 ; i < buildJars.length && resultJar == null; i++) {
            boolean classIsInJar = JarHelper.classExistsInJarFile(buildJars[i], this.className)
            if (classIsInJar) {
                resultJar = buildJars[i]
            }
        }

        return resultJar != null ? resultJar.getAbsolutePath() : null
    }

    private boolean isWindows() {
        return System.getProperty("os.name") == "Windows"   
    }

}