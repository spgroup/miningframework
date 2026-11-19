package services.dataCollectors.modifiedLinesCollector.diffTools

import services.dataCollectors.modifiedLinesCollector.DiffJParser
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod
import util.ProcessRunner

/**
 * The diffj cli (a jar in the dependencies folder) as the method diff tool.
 */
class DiffJDiffTool implements MethodDiffTool {

    private final String diffJ
    private final String dependenciesPath
    private final DiffJParser parser = new DiffJParser()

    /**
     * @param diffJ the diffj jar file name, e.g. "diffj.jar"
     * @param dependenciesPath the folder containing the diffj jar
     */
    DiffJDiffTool(String diffJ = 'diffj.jar', String dependenciesPath = 'dependencies') {
        this.diffJ = diffJ
        this.dependenciesPath = dependenciesPath
    }

    Map<String, int[]> getModifiedMethodsLines(File ancestorFile, File targetFile) {
        return parser.parse(run(ancestorFile, targetFile, '--brief'))
    }

    List<ModifiedMethod> getAllModifiedMethods(File ancestorFile, File targetFile) {
        return parser.parseAllModifiedMethods(run(ancestorFile, targetFile, '--changed-methods-only'))
    }

    private List<String> run(File ancestorFile, File targetFile, String option) {
        Process diffJ = ProcessRunner.runProcess(dependenciesPath, 'java', '-jar', diffJ, option, ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))
        List<String> output = reader.readLines()
        reader.close()
        return output
    }
}
