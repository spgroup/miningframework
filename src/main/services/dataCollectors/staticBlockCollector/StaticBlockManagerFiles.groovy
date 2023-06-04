package services.dataCollectors.staticBlockCollector

import util.FileManager
import util.ProcessRunner
import java.nio.file.Path

class StaticBlockManagerFiles {
  
   public Path filesQuadruplePath
   
    StaticBlockManagerFiles(List<Path> filesQuadruplePaths ) {
       for(Path filesQuadruplePath : filesQuadruplePaths) {
           this.filesQuadruplePath = filesQuadruplePath
           forwentMergeScenariosWithBehaviorDifferent()
       }
	}

private  boolean forwentMergeScenariosWithBehaviorDifferent() {
        Path insertionLevelOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[0])
        Path simpleOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[1])
        Path gitMergeOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[2])
        boolean flag = false
        List<String> resultDiff =  runTextualDiff(insertionLevelOutput, simpleOutput)
        List<String> resultDiffGitMI =  runTextualDiff(insertionLevelOutput, gitMergeOutput)
        List<String> resultDiffGitMS =  runTextualDiff(simpleOutput, gitMergeOutput)
        if(resultDiff !=null && resultDiff.size() > 0) {
            return true;
        }else if((resultDiffGitMI !=null && resultDiffGitMI.size() > 0) || (resultDiffGitMS !=null && resultDiffGitMS.size() > 0)){
            return true;
        }else{

            FileManager.delete(new File(this.filesQuadruplePath.toString()))

            this.filesQuadruplePath = null;
            return false;
        }
    }
    private Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }
    private static List<String> runTextualDiff(Path newHandlersFile, Path oldHandlersFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,newHandlersFile.toString(), oldHandlersFile.toString())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }
}