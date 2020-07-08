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
    private TextualDiffParser textualDiffParser = new TextualDiffParser();
    private DiffJParser modifiedMethodsParser = new DiffJParser();
    private MethodModifiedLinesMatcher modifiedMethodsMatcher = new MethodModifiedLinesMatcher();

    public ModifiedMethodsHelper(String diffj) {
        this.diffJOption = diffj
    }

    public Set<ModifiedMethod> getModifiedMethods(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        List<String> diffJOutput = runDiffJ(ancestorFile, targetFile);
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);

        targetFile.delete()
        ancestorFile.delete()

        Map<String, int[]> parsedDiffJResult = modifiedMethodsParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        return modifiedMethodsMatcher.matchModifiedMethodsAndLines(parsedDiffJResult, parsedTextualDiffResult);
    }

    private List<String> runDiffJ(File ancestorFile, File targetFile) {
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', this.diffJOption, "--brief", ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
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