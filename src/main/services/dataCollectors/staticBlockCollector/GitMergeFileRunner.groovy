package services.dataCollectors.staticBlockCollector

import util.ProcessRunner
import services.util.MergeConflict
import services.dataCollectors.staticBlockCollector.TriplaFilesRunner

import java.nio.file.Path

class GitMergeFileRunner extends TriplaFilesRunner {

    private static final String MERGE_FILE_NAME = "merge"

    GitMergeFileRunner() {
        this.nameAlgoritm = 'GitMergeFile'
    }

    protected ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        Path filesQuadruplePath = baseFile.getParent()
        Path outputPath = getOutputPath(filesQuadruplePath, MERGE_FILE_NAME)

        ProcessBuilder processBuilder = new ProcessBuilder()
        processBuilder.redirectOutput(outputPath.toFile())

        return processBuilder
    }

    protected List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = [ 'git', 'merge-file', '-L', 'MINE', '-L', 'BASE', '-L', 'YOURS', '-p' ]
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())
        return parameters
    }

}