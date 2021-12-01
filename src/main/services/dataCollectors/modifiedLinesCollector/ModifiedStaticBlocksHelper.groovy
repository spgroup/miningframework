package services.dataCollectors.modifiedLinesCollector

import project.MergeCommit
import project.Project
import util.FileManager
import util.ProcessRunner
import static app.MiningFramework.arguments;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.InitializerDeclaration;


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
    private File filteredScenariosIniatilizationBlock = null;

    public ModifiedStaticBlocksHelper(String diffj) {
        this.diffJOption = diffj
    }

    public Set<ModifiedStaticBlock> getModifiedStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA,MergeCommit mergeCommit) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        Map<Integer, String> ancestorIniatilizationBlockASTFile = parsedASTAllStaticBlock(ancestorFile)
        Map<Integer, String> staticBlockedASTFile = parsedASTAllStaticBlock(targetFile)

        // List<String> diffJOutput = runDiffJ(ancestorFile, targetFile);
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);
       // if(staticBlockedASTFile?.size() >0)
        createDataFilesExperimentalStaticBlock(project,ancestorSHA,targetSHA,filePath,mergeCommit,quantityInializationBlock(ancestorIniatilizationBlockASTFile,staticBlockedASTFile))
        //Map<String, int[]> parsedDiffJResult = modifiedStaticBlocksParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        targetFile.delete()
        ancestorFile.delete()

        return modifiedStaticBlocksMatcher.matchModifiedStaticBlocksASTLines(staticBlockedASTFile, parsedTextualDiffResult);
    }
   private int quantityInializationBlock(Map<Integer, String> ancestorIniatilizationBlockASTFile, Map<Integer, String> staticBlockedASTFile){
       if(staticBlockedASTFile.size() >= ancestorIniatilizationBlockASTFile.size()){
           return staticBlockedASTFile.size();
       }else {
           return ancestorIniatilizationBlockASTFile.size();
       }
   }
   private void createDataFilesExperimentalStaticBlock(Project project,String sha, String ancestorSHA, String targetFile, MergeCommit mergeCommit,int qtdStaticBlock) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        filteredScenariosIniatilizationBlock = new File(dataFolder.getAbsolutePath() + "/results-Iniatilizationlock.csv")
        if (!filteredScenariosIniatilizationBlock.exists()) {
            filteredScenariosIniatilizationBlock << 'project; merge commit ;ancestorSHA; left; right; hasIniatializationBlock;  qtd_static\n'
        }

       filteredScenariosIniatilizationBlock << "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${qtdStaticBlock}\n"
    }
    private Map<String,String> parsedASTAllStaticBlock(File file){
        JavaParser javaParser = new JavaParser();
        def result = new HashMap<int, String>();
       if(file !=null) {
           try {
               CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
               if (compilationUnit != null) {
                   int i=1;
                   compilationUnit.stream().forEach(staticBlocked -> {
                       if (staticBlocked instanceof InitializerDeclaration) {
                           int begin = staticBlocked?.getRange().get().begin.line
                           int end = staticBlocked?.getRange().get().end.line
                           result.put(convertStrIntIdentifier(i++ , begin , end) , staticBlocked?.getBody()?.asBlockStmt()?.toString())
                       }
                   });
               }
           }catch(Exception e){
               println e
           }
       }
        return result;
    }
    private String convertStrIntIdentifier(int ident, int begin, int end){
        return ident + ";" +String.valueOf(begin) + "-" + String.valueOf(end)
    }


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