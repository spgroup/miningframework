package services.dataCollectors.staticBlockCollector

import project.MergeCommit
import project.Project
import services.dataCollectors.modifiedLinesCollector.DiffJParser
import services.dataCollectors.modifiedLinesCollector.ModifiedLine
import services.dataCollectors.modifiedLinesCollector.TextualDiffParser
import util.FileManager
import util.ProcessRunner
import static app.MiningFramework.arguments;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.InitializerDeclaration;


/**
 * This class uses a combination o two diffing tools to provide the necessary diff output
 * it uses the semantic diff tool (diffj) to get which methods were modified and which lines
 * were modified in the method and it uses the textual diff tool to get type of the modifications
 * because for this analysis we need full line modification so we indicate if: the line was fully added,
 * fully removed or changed
 */
class StaticBlocksHelper {
    
    private String diffJOption;
    private TextualDiffParser textualDiffParser = new TextualDiffParser();
    private DiffJParser modifiedStaticBlocksParser = new DiffJParser();
    private StaticBlockModifiedLinesMatcher modifiedStaticBlocksMatcher = new StaticBlockModifiedLinesMatcher();
    private File filteredScenariosIniatilizationBlock = null;

    public StaticBlocksHelper(String diffj) {
        this.diffJOption = diffj
    }

    public Set<StaticBlock> getModifiedStaticBlocks(Project project, String filePath, String ancestorSHA, String targetSHA, MergeCommit mergeCommit) {
        File ancestorFile = FileManager.getFileInCommit(project, filePath, ancestorSHA)
        File targetFile = FileManager.getFileInCommit(project, filePath, targetSHA)

        Map<Integer, String> ancestorIniatilizationBlockASTFile = parsedASTAllStaticBlock(ancestorFile)
        Map<Integer, String> staticBlockedASTFile = parsedASTAllStaticBlock(targetFile)

        // List<String> diffJOutput = runDiffJ(ancestorFile, targetFile);
        List<String> textualDiffOutput = runTextualDiff(ancestorFile, targetFile);
        if(staticBlockedASTFile?.size() >0 || ancestorIniatilizationBlockASTFile.size() > 0) {
            obtainResultsForProject(project, mergeCommit)
            createDataFilesExperimentalStaticBlock(project, filePath, mergeCommit, quantityInializationBlock(ancestorIniatilizationBlockASTFile, staticBlockedASTFile))
        }
        //Map<String, int[]> parsedDiffJResult = modifiedStaticBlocksParser.parse(diffJOutput);
        List<ModifiedLine> parsedTextualDiffResult = textualDiffParser.parse(textualDiffOutput);

        targetFile.delete()
        ancestorFile.delete()

        return modifiedStaticBlocksMatcher.matchModifiedStaticBlocksASTLines(ancestorIniatilizationBlockASTFile,staticBlockedASTFile, parsedTextualDiffResult);
    }
   private int quantityInializationBlock(Map<Integer, String> ancestorIniatilizationBlockASTFile, Map<Integer, String> staticBlockedASTFile){
       if(staticBlockedASTFile.size() >= ancestorIniatilizationBlockASTFile.size()){
           return staticBlockedASTFile.size();
       }else {
           return ancestorIniatilizationBlockASTFile.size();
       }
   }
    private void obtainResultsForProject(Project project , MergeCommit mergeCommit) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/3_results_common_files_iniatilizationblock_both_branches_" + project.getName() + ".csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'Merge commit; Ancestor; left; right;\n'
        }
        if(printMergeCommitInitializationBlock(mergeCommit, obtainResultsForProjects.getAbsolutePath()))
         obtainResultsForProjects  << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};\n"
    }
   private void createDataFilesExperimentalStaticBlock(Project project,String targetFile, MergeCommit mergeCommit,int qtdStaticBlock) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        filteredScenariosIniatilizationBlock = new File(dataFolder.getAbsolutePath() + "/merge_into_files_containing_initialization_blocks"+ project.getName() +".csv")
        if (!filteredScenariosIniatilizationBlock.exists()) {
            filteredScenariosIniatilizationBlock << 'project; merge commit ;ancestorSHA; left; right; pathFilesWithIniTBlock;  qtd_static\n'
        }
       if(printContentFileInitializationBlock(targetFile,mergeCommit))
          filteredScenariosIniatilizationBlock << "${project.getName()};${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};${targetFile};${qtdStaticBlock}\n"
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
    private boolean printContentFileInitializationBlock(String targetFile, MergeCommit mergeCommit){
        if(filteredScenariosIniatilizationBlock.exists()){

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filteredScenariosIniatilizationBlock.getAbsolutePath())))
            def line = null;
            while ((line = reader.readLine()) != null) {
               // println "${line}";
                String str = line.split(";")[4]
                String str1 = line.split(";")[5]
                 if(targetFile.equals(str1) &&  mergeCommit.getRightSHA().equals(str) ){
                  return false
                }
            }
        }
        return true;
    }
    private boolean printMergeCommitInitializationBlock( MergeCommit mergeCommit, String absolutePath){
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)))
            def line = null;
            while ((line = reader.readLine()) != null) {
                String str = line.split(";")[0]
                if(mergeCommit.getSHA().equals(str) ){
                    return false
                }
            }
        return true;
    }
    private List<String> runTextualDiff (File ancestorFile, File targetFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,ancestorFile.getAbsolutePath(), targetFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }

}