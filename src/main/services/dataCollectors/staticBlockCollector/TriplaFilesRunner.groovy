package services.dataCollectors.staticBlockCollector

import project.MergeCommit
import project.Project
import util.ProcessRunner
import util.TypeNameHelper

import java.nio.file.Path
import java.nio.file.Paths

abstract class TriplaFilesRunner {

    protected String nameAlgoritm


    void collectResults(List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            Path leftFile = getContributionFile(filesQuadruplePath, 'left')
            Path baseFile = getContributionFile(filesQuadruplePath, 'base')
            Path rightFile = getContributionFile(filesQuadruplePath, 'right')

          // createToolDirectory(filesQuadruplePath)
            runTool(leftFile, baseFile, rightFile)
        }
    }

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
    }

    protected void createToolDirectory(Path filesQuadruplePath) {
        filesQuadruplePath.resolve(nameAlgoritm).toFile().mkdir()
    }

    protected void runTool(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder processBuilder = buildProcess(leftFile, baseFile, rightFile)
        List<String> parameters = buildParameters(leftFile, baseFile, rightFile)
        processBuilder.command().addAll(parameters)

        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()
    }

    protected Path getOutputPath(Path filesQuadruplePath, String mergeFileName) {
        return filesQuadruplePath.resolve("${mergeFileName}.java")
    }

    protected abstract ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile)
    protected abstract List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile)

}