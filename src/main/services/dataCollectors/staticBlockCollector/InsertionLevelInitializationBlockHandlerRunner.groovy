package services.dataCollectors.staticBlockCollector

import util.ProcessRunner
import services.util.MergeConflict
import services.dataCollectors.staticBlockCollector.TriplaFilesRunner

import java.nio.file.Path
import java.nio.file.Paths

class InsertionLevelInitializationBlockHandlerRunner extends TriplaFilesRunner {
    static final Path S3M_PATH = Paths.get("dependencies/s3m.jar")

    InsertionLevelInitializationBlockHandlerRunner() {
        this.nameAlgoritm = 'InsertionLevelInitializationBlockHandler'
    }

    protected  ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder S3M = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        return  S3M
    }
    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        String pathOutput = getOutputPath(baseFile.getParent(), nameAlgoritm).toString()
        List<String> parameters = ['java', '-jar', getNameAsString(S3M_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), '-o', pathOutput, '--handle-initialization-blocks','false','--handle-initialization-blocks-multiple-blocks','true']
       
        return parameters
    }
    private static String getNameAsString(Path path) {
        return path.getFileName().toString()
    }
}