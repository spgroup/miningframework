package services.util

import util.ProcessRunner

import java.nio.file.Path

abstract class MergeToolRunner {

    protected String mergeToolName

    void collectResults(List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            Path leftFile = getContributionFile(filesQuadruplePath, 'left')
            Path baseFile = getContributionFile(filesQuadruplePath, 'base')
            Path rightFile = getContributionFile(filesQuadruplePath, 'right')

            createToolDirectory(filesQuadruplePath)
            runTool(leftFile, baseFile, rightFile)
        }
    }

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }

    protected void createToolDirectory(Path filesQuadruplePath) {
        filesQuadruplePath.resolve(mergeToolName).toFile().mkdir()
    }

    protected void runTool(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder processBuilder = buildProcess(leftFile, baseFile, rightFile)
        List<String> parameters = buildParameters(leftFile, baseFile, rightFile)
        processBuilder.command().addAll(parameters)

        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()

        Path filesQuadruplePath = baseFile.getParent()
        processOutput(filesQuadruplePath)
    }

    protected Path getOutputPath(Path filesQuadruplePath, String mergeFileName) {
        return filesQuadruplePath.resolve(mergeToolName).resolve("${mergeFileName}.java")
    }

    protected abstract ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile)
    protected abstract List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile)
    protected abstract void processOutput(Path filesQuadruplePath)

}