package services.util

import util.ProcessRunner

import java.nio.file.Path

abstract class MergeToolRunner {

    protected String mergeToolName

    String getMergeToolName() {
        return mergeToolName
    }

    void collectResults(List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            Path leftFile = getContributionFile(filesQuadruplePath, 'left')
            Path baseFile = getContributionFile(filesQuadruplePath, 'base')
            Path rightFile = getContributionFile(filesQuadruplePath, 'right')

            createToolDirectory(filesQuadruplePath)
            runTool(leftFile, baseFile, rightFile)
        }
    }

    protected void createToolDirectory(Path filesQuadruplePath) {
        filesQuadruplePath.resolve(mergeToolName).toFile().mkdir()
    }

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }

    protected Path getOutputPath(Path filesQuadruplePath, String mergeFileName) {
        return filesQuadruplePath.resolve(mergeToolName).resolve("${mergeFileName}.java")
    }

    protected void runProcess(ProcessBuilder processBuilder) {
        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()
    }

    protected abstract void runTool(Path leftFile, Path baseFile, Path rightFile)

}