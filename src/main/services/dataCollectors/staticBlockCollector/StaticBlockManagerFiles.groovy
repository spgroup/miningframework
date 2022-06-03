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
        Path newHandlerOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[0])
        Path oldHandlerOutput = getContributionFile(this.filesQuadruplePath, MergesCollector.mergeApproaches[1])

        List<String> resultDiff =  runTextualDiff(newHandlerOutput, oldHandlerOutput)
        if(resultDiff !=null && resultDiff.size() > 0){
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