package services.dataCollectors.modifiedLinesCollector

import project.Project
import util.FileManager
import util.ProcessRunner

/**
 * This class uses a combination o two diffing tools to provide the necessary diff output
 * it uses the semantic diff tool (diffj) to get which methods were modified and which lines
 * were modified in the method and it uses the textual diff tool to get type of the modifications
 * because for this analysis we need full line modification so we indicate if: the line was fully added,
 * fully removed or changed
 */
class ModifiedMethodsHelper {
    
    private String diffJOption;
    private String dependenciesPath; // Path to the folder containing the DiffJ executable
    private TextualDiffParser textualDiffParser = new TextualDiffParser();
    private DiffJParser modifiedMethodsParser = new DiffJParser();
    private MethodModifiedLinesMatcher modifiedMethodsMatcher = new MethodModifiedLinesMatcher();

    /**
     * Assumes the path to diffj as the 'dependencies' directory in the root of the project.
     * @param diffj Represents the diffJ file name.
     */
    public ModifiedMethodsHelper(String diffj) {
        this(diffj, "dependencies");
    }

    /**
     * Receives the path to diffj as a parameter, in cases where the class is used as a library.
     * @param diffj Represents the diffJ file name.
     * @param dependenciesPath The path to the folder containing the DiffJ executable.
     */
    public ModifiedMethodsHelper(String diffj, String dependenciesPath) {
        this.diffJOption = diffj;
        this.dependenciesPath = dependenciesPath;
    }

    Set<ModifiedMethod> getAllModifiedMethods(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        List<String> diffJOutput = runDiffJ(ancestorFile, targetFile, "--changed-methods-only");

        targetFile.delete()
        ancestorFile.delete()
        return modifiedMethodsParser.parseAllModifiedMethods(diffJOutput);
    }

    Set<ModifiedMethod> getModifiedMethods(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        List<String> diffJOutput = runDiffJ(ancestorFile, targetFile, "--brief");
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);

        targetFile.delete()
        ancestorFile.delete()

        Map<String, int[]> parsedDiffJResult = modifiedMethodsParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        return modifiedMethodsMatcher.matchModifiedMethodsAndLines(parsedDiffJResult, parsedTextualDiffResult);
    }

    List<ModifiedLine> getModifiedLines(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        List<String> diffJOutput = runDiffJ(ancestorFile, targetFile, "--brief");
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);

        targetFile.delete()
        ancestorFile.delete()

        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        return parsedTextualDiffResult
    }

    private List<String> runDiffJ(File ancestorFile, File targetFile, String option) {
        Process diffJ = ProcessRunner.runProcess(this.dependenciesPath, 'java', '-jar', this.diffJOption, option, ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }

    private List<String> runTextualDiff (File ancestorFile, File targetFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }

}