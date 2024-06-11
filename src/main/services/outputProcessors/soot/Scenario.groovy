package services.outputProcessors.soot

import util.JarHelper

import static com.xlson.groovycsv.CsvParser.parseCsv

class Scenario {

    private String projectName;
    private String className;
    private String methodSignature;
    private String commitSHA;
    private boolean hasBuild;
    private String entrypoints;

    private String scenarioDirectory;

    Scenario(projectName, className, methodSignature, commitSHA, scenarioDirectory, hasBuild, String entrypoints) {
        this.projectName = projectName;
        this.className = className;
        this.methodSignature = methodSignature;
        this.commitSHA = commitSHA;
        this.scenarioDirectory = scenarioDirectory;
        this.hasBuild = hasBuild;
        this.entrypoints = entrypoints;
    }
    
    boolean getHasBuild() {
        return this.hasBuild;
    }

    String getCommitSHA () {
        return this.commitSHA;
    }

    String toString() {
        return "${projectName};${className};${methodSignature};${commitSHA}"
    }

    String getLinesFilePath () {
        return "${this.scenarioDirectory}/changed-methods/${className}/${methodSignature}/left-right-lines.csv"
    }

    String getLinesReverseFilePath () {
        return "${this.scenarioDirectory}/changed-methods/${className}/${methodSignature}/right-left-lines.csv"
    }
    
    String getClassPath() {
        File file = new File("${this.scenarioDirectory}/original-without-dependencies/merge");
        File[] buildJars = file.listFiles()

        String buildJarPath = getJarThatHasClass(buildJars)
        if (buildJarPath == null) {
            throw new ClassNotFoundInJarException(this.className)
        }
        return buildJarPath
    }

    String getEntrypoints() {
        return entrypoints
    }

    private String getJarThatHasClass(File[] buildJars) {
        File resultJar = null;

        if (buildJars != null) {
            for (int i = 0 ; i < buildJars.length && resultJar == null; i++) {
                boolean classIsInJar = JarHelper.classExistsInJarFile(buildJars[i], this.className)
                if (classIsInJar) {
                    resultJar = buildJars[i]
                }
            }
        }

        return resultJar != null ? resultJar.getAbsolutePath() : null
    }

    private boolean isWindows() {
        return System.getProperty("os.name") == "Windows"   
    }

}