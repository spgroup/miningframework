package services.soot

import static com.xlson.groovycsv.CsvParser.parseCsv

class SootScenario {

    private String projectName;
    private String className;
    private String methodSignature;
    private String commitSHA;

    SootScenario (projectName, className, methodSignature, commitSHA) {
        this.projectName = projectName;
        this.className = className;
        this.methodSignature = methodSignature;
        this.commitSHA = commitSHA;
    } 

    public String getCommitSHA () {
        return this.commitSHA;
    }
    
    public String toString() {
        return "${projectName};${className};${methodSignature};${commitSHA}"
    } 

    public String getScenarioDirectory (outputPath) {
        return "${getOutputAbsolutePath(outputPath)}/files/${projectName}/${commitSHA}"
    }

    public String getLinesFile (outputPath) {
        return "${getScenarioDirectory(outputPath)}/soot.csv"
    }

    public String getLinesReverseFile (outputPath) {
        return "${getScenarioDirectory(outputPath)}/soot-reverse.csv"
    }
    
    public String getClassPath (outputPath) {
        File file = new File("${this.getScenarioDirectory(outputPath)}/build")

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

    private String getOutputAbsolutePath (outputPath) {
        return (new File(outputPath)).getAbsolutePath()
    }

    static List<SootScenario> readScenarios (filePath) {
            
        List<SootScenario> result = new ArrayList<SootScenario>()
        String resultsFileText = new File(filePath).getText()

        def iterator = parseCsv(resultsFileText, separator:';')

        for (line in iterator) {
            String projectName = line["project"]
            String classPathName = line["className"]
            String methodSignature = line["method"]
            String commitSHA = line["merge commit"]
            
            def scenario = new SootScenario(projectName, classPathName, methodSignature, commitSHA);
        
            result.add(scenario)
        }

        return result
    }
}