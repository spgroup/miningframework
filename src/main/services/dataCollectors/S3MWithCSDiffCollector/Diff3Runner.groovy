package services.dataCollectors.S3MWithCSDiffCollector

import util.ProcessRunner
import services.util.MergeConflict
import services.util.MergeToolRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Diff3Runner extends MergeToolRunner {

    private static final String MERGE_FILE_NAME = "merge"

    Diff3Runner(String mergeToolName) {
        this.mergeToolName = mergeToolName
    }

    protected void runTool(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder processBuilder = buildProcess(leftFile, baseFile, rightFile)
        runProcess(processBuilder)

        Path outputPath = getOutputPath(baseFile.getParent(), MERGE_FILE_NAME)
        MergeConflict.removeBaseFromConflicts(outputPath)
    }

    private ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = buildParameters(leftFile, baseFile, rightFile)

        ProcessBuilder processBuilder = new ProcessBuilder()
        processBuilder.command().addAll(parameters)

        Path outputPath = getOutputPath(baseFile.getParent(), MERGE_FILE_NAME)
        processBuilder.redirectOutput(outputPath.toFile())

        return processBuilder
    }

    private List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = ['diff3', '-L', 'MINE', '-L', 'BASE', '-L', 'YOURS', '-m']
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())
        return parameters
    }

}