package services.dataCollectors.staticBlockCollector

import util.ProcessRunner
import services.util.MergeConflict
import services.dataCollectors.staticBlockCollector.TriplaFilesRunner

import java.nio.file.Path
import java.nio.file.Paths

class SimpleInitializationBlockHandlerRunner extends TriplaFilesRunner {
    static final Path S3M_PATH = Paths.get("dependencies/s3m.jar")
    private static final String MERGE_FILE_NAME = "merge"
   // static final String oldHandlerOutput = "oldHandlerOutput";
	
    SimpleInitializationBlockHandlerRunner() {
        this.nameAlgoritm = 'SimpleInitializationBlockHandler'
    }

    protected  ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder S3M = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        return  S3M
    }
    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = ['java', '-jar', getNameAsString(S3M_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), '-o', getOutputPath(baseFile.getParent(), nameAlgoritm).toString() , '--handle-initialization-blocks','true','--handle-initialization-blocks-multiple-blocks','false']
       
        return parameters
    }
    private static String getNameAsString(Path path) {
        return path.getFileName().toString()
    }
}