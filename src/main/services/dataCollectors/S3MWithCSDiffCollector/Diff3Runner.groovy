package services.dataCollectors.S3MWithCSDiffCollector

import util.ProcessRunner
import services.util.MergeToolRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Diff3Runner extends MergeToolRunner {

    private static String MERGE_FILE_NAME = "textual.java"

    void collectResults(List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            runDiff3(filesQuadruplePath)
        }
    }

    private void runDiff3(Path filesQuadruplePath) {
        Path leftFile = getContributionFile(filesQuadruplePath, 'left')
        Path baseFile = getContributionFile(filesQuadruplePath, 'base')
        Path rightFile = getContributionFile(filesQuadruplePath, 'right')

        ProcessBuilder processBuilder = buildDiff3Process(leftFile, baseFile, rightFile)
        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()
    }

    private ProcessBuilder buildDiff3Process(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = buildDiff3Parameters(leftFile, baseFile, rightFile)

        ProcessBuilder processBuilder = new ProcessBuilder()
        processBuilder.command().addAll(parameters)

        Path outputPath = getOutputPath(baseFile.getParent())
        processBuilder.redirectOutput(outputPath.toFile())

        return processBuilder
    }

    private List<String> buildDiff3Parameters(Path leftFile, Path baseFile, Path rightFile) {
        List<String> parameters = ['diff3', '-m']
        parameters.addAll(leftFile.toString(), baseFile.toString(), rightFile.toString())
        return parameters
    }

    private Path getOutputPath(Path filesQuadruplePath) {
        return filesQuadruplePath.resolve(MERGE_FILE_NAME)
    }

}