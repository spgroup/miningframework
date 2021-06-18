package services.dataCollectors.S3MWithCSDiffCollector

import util.ProcessRunner
import services.util.MergeConflict
import services.util.MergeToolRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Diff3Runner extends MergeToolRunner {

    private static final String MERGE_FILE_NAME = "merge"

    Diff3Runner() {
        this.mergeToolName = 'Diff3'
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        Path filesQuadruplePath = baseFile.getParent()
        Path outputPath = getOutputPath(filesQuadruplePath, MERGE_FILE_NAME)
        
        ProcessBuilder processBuilder = new ProcessBuilder()
        processBuilder.redirectOutput(outputPath.toFile())

        return processBuilder
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = ['diff3', '-L', 'MINE', '-L', 'BASE', '-L', 'YOURS', '-m']
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())
        return parameters
    }

    protected void processOutput(Path filesQuadruplePath) {
        Path outputPath = getOutputPath(filesQuadruplePath, MERGE_FILE_NAME)
        MergeConflict.removeBaseFromConflicts(outputPath)
    }

}