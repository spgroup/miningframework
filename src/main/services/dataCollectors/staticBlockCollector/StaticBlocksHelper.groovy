package services.dataCollectors.staticBlockCollector

import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.ModifiedLine
import services.dataCollectors.modifiedLinesCollector.TextualDiffParser
import util.FileManager
import util.ProcessRunner
import static app.MiningFramework.arguments;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;

/**
 * This class uses a abstract Syntactic Tree to collect the initialization blocks
 * it uses the semantic diff tool (diffj) to get which blocks were modified and which lines
 * were modified in the initialization block and it uses the textual diff tool to get type of the modifications
 * because for this analysis we need full line modification so we indicate if: the line was fully added,
 * fully removed or changed
 */
class StaticBlocksHelper {
    
    private String diffJOption;
    private TextualDiffParser textualDiffParser = new TextualDiffParser();
    private StaticBlockModifiedLinesMatcher modifiedStaticBlocksMatcher = new StaticBlockModifiedLinesMatcher();

    public StaticBlocksHelper(String diffj) {
        this.diffJOption = diffj
    }

    public Set<StaticBlock> getModifiedStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        Map<Integer, String> ancestorIniatilizationBlockASTFile = parsedOnlyGlobalStaticBlockAST(ancestorFile)
        Map<Integer, String> staticBlockedASTFile = parsedOnlyGlobalStaticBlockAST(targetFile)

        if (ancestorIniatilizationBlockASTFile == null || staticBlockedASTFile == null){
            targetFile.delete()
            ancestorFile.delete()
            def staticBlockSet = new HashSet<StaticBlock>();
            return staticBlockSet
        }

        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);
        if(staticBlockedASTFile?.size() >0 || ancestorIniatilizationBlockASTFile?.size() > 0) {
            SpreadsheetBuilder.obtainResultsSpreadsheetForProject(project, mergeCommit, SpreadsheetBuilder.FILTER_COMMON_FILES_INITIALIZATION_BLOCK_BOTH_BRANCHES);
        }
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        targetFile.delete()
        ancestorFile.delete()

        return modifiedStaticBlocksMatcher.matchModifiedStaticBlocksAST(ancestorIniatilizationBlockASTFile,staticBlockedASTFile, parsedTextualDiffResult,filePath);
    }

    private Map<String,String> parsedOnlyGlobalStaticBlockAST(File file){
        JavaParser javaParser = new JavaParser();
        def result = new HashMap<int, String>();
        if(file !=null) {
            try {
                CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
                if(compilationUnit.getTokenRange().toString().contains("fatal: Not a valid object")){
                    return null;
                }
                int i = 1;
                for (TypeDeclaration type : compilationUnit.getTypes()) {
                    List<BodyDeclaration> members = type.getMembers();
                    for (BodyDeclaration member : members) {
                        if (member.isInitializerDeclaration()) {
                            int begin = member?.getRange().get().begin.line
                            int end = member?.getRange().get().end.line
                            result.put(convertStrIntIdentifier(i++, begin, end), member?.getBody()?.asBlockStmt()?.toString())
                        }
                    }
                }
            }catch (Exception e ) {
                    println e
            }
        }
        return result;
    }

    private String convertStrIntIdentifier(int ident, int begin, int end){
        return ident + ";" +String.valueOf(begin) + "-" + String.valueOf(end)
    }

    private List<String> runTextualDiff (File ancestorFile, File targetFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }
}
