package services.dataCollectors.modifiedLinesCollector

import project.Project
import util.FileManager
import util.ProcessRunner

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.InitializerDeclaration;

import static app.MiningFramework.arguments

/**
 * This class uses a combination o two diffing tools to provide the necessary diff output
 * it uses the semantic diff tool (diffj) to get which methods were modified and which lines
 * were modified in the method and it uses the textual diff tool to get type of the modifications
 * because for this analysis we need full line modification so we indicate if: the line was fully added,
 * fully removed or changed
 */
class ModifiedStaticBlocksHelper {
    
    private String diffJOption;
    private TextualDiffParser textualDiffParser = new TextualDiffParser();
    private DiffJParser modifiedStaticBlocksParser = new DiffJParser();
    private StaticBlockModifiedLinesMatcher modifiedStaticBlocksMatcher = new StaticBlockModifiedLinesMatcher();

    public ModifiedStaticBlocksHelper(String diffj) {
        this.diffJOption = diffj
    }

    public Set<ModifiedStaticBlock> getModifiedStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        Map<Integer, String> staticBlockedASTFile = parsedASTAllStaticBlock(targetFile);
       // List<String> diffJOutput = runDiffJ(ancestorFile, targetFile);
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);

        //Map<String, int[]> parsedDiffJResult = modifiedStaticBlocksParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        targetFile.delete()
        ancestorFile.delete()

        return modifiedStaticBlocksMatcher.matchModifiedStaticBlocksASTLines(staticBlockedASTFile, parsedTextualDiffResult);
    }
    private Map<Integer,String> parsedASTAllStaticBlock(File file){
        JavaParser javaParser = new JavaParser();
        def result = new HashMap<int, String>();
        int count = 0;
        CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
        compilationUnit.stream().forEach(staticBlocked -> {
            if(staticBlocked instanceof InitializerDeclaration) {
                result.put(count,staticBlocked?.getBody()?.asBlockStmt()?.toString())
                count++;
            }
        });
        return result;
    }
    private boolean  verifyMatchModifiedStaticBlo
    private List<String> runDiffJ(File ancestorFile, File targetFile) {
        printRunDiffJ(ancestorFile,targetFile );
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', this.diffJOption, "--brief", ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))


        def line = null;
        while ((line = reader.readLine()) != null) {
            println "${line}";
        }
         def output = reader.readLines()
        reader.close();

        return output
    }
    private void printRunDiffJ(File ancestorFile, File targetFile){
        File target = new File("c:\\UFPE\\resultStaticBlock.txt")
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', this.diffJOption, "--brief", ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())


        int i =1;
        diffJ.getInputStream().eachLine {

            int inIndex = it.indexOf("in ")
            if(inIndex != -1) {
                String signature = it.substring(inIndex + 3)
                if(signature.equals("static block")){
                    target << " ${it.toString()}\n"
                    println "static${i}()"
                }
            }
            i++
        }

    }
    private List<String> runTextualDiff (File ancestorFile, File targetFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }

}