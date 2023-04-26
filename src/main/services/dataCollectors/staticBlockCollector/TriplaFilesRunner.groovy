package services.dataCollectors.staticBlockCollector

import project.MergeCommit
import project.Project
import util.ProcessRunner
import java.util.*;
import java.nio.file.Path
import java.nio.file.Paths

abstract class TriplaFilesRunner {

    protected String nameAlgoritm
    //itens - Number of times the median will be collected
    protected List<Integer> itens = Arrays.asList(1, 2, 3, 4, 5);

    void collectResults(Project project, MergeCommit mergeCommit,List<Path> filesQuadruplePaths) {
        filesQuadruplePaths.each { filesQuadruplePath ->
            Path leftFile = getContributionFile(filesQuadruplePath, 'left')
            Path baseFile = getContributionFile(filesQuadruplePath, 'base')
            Path rightFile = getContributionFile(filesQuadruplePath, 'right')
            if(!nameAlgoritm.equals("GitMergeFile")){
              for(Integer count : itens) {
                LoggerStatistics.logTimeInitial()
                runTool(leftFile, baseFile, rightFile)
                LoggerStatistics.updateSpreadsheet(count,project, mergeCommit, filesQuadruplePath, nameAlgoritm, LoggerStatistics.logTimeFinal())
              }
            }else{
                runTool(leftFile, baseFile, rightFile)
            }
        }
    }

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve("${contributionFileName}.java").toAbsolutePath()
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