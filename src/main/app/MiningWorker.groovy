package main.app

import java.text.SimpleDateFormat
import java.io.File

import main.interfaces.*
import static main.app.MiningFramework.arguments
import java.util.concurrent.BlockingQueue


import main.arguments.*
import main.project.*
import main.interfaces.*
import main.exception.InvalidArgsException
import main.exception.UnstagedChangesException
import main.exception.UnexpectedPostScriptException
import main.exception.NoAccessKeyException
import main.util.*

class MiningWorker implements Runnable {

    private ExperimentalDataCollector dataCollector;
    private StatisticsCollector statCollector;
    private CommitFilter commitFilter;
    private BlockingQueue<Project> projectList;
    private String baseDir;

    public MiningWorker(ExperimentalDataCollector dataCollector, StatisticsCollector statCollector, CommitFilter commitFilter, BlockingQueue<Project> projectList, String baseDir) {
        this.dataCollector = dataCollector
        this.statCollector = statCollector
        this.commitFilter = commitFilter
        this.projectList = projectList
        this.baseDir = baseDir
    }

    void run () {
        while (!projectList.isEmpty()) {

            Project project = projectList.take()

            printProjectInformation (project)

            if (project.isRemote()) {
                cloneRepository(project, baseDir)
            } else {
                checkForUnstagedChanges(project);
            }

            // Since date and until date as arguments (dd/mm/yyyy).
            List<MergeCommit> mergeCommits = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate()) 
            for (mergeCommit in mergeCommits) {


                if (applyFilter(project, mergeCommit)) {
                    printMergeCommitInformation(project, mergeCommit)

                    collectStatistics(project, mergeCommit)
                    collectExperimentalData(project, mergeCommit)
                }
            }

            if(arguments.isPushCommandActive()) // Will push.
                pushResults(project, arguments.getResultsRemoteRepositoryURL())

            endProjectAnalysis (project)
        }
    } 

    private void checkForUnstagedChanges(Project project) {
        String gitDiffOutput = ProcessRunner.runProcess(project.getPath(), "git", "diff").getText()

        if (gitDiffOutput.length() != 0) {
            throw new UnstagedChangesException(project.getName())
        }
    }

    private void cloneRepository(Project project, String target) {        
        println "Cloning repository ${project.getName()} into ${target}"

        File projectDirectory = new File(target)
        if(projectDirectory.exists()) {
            FileManager.delete(projectDirectory)
        }
        projectDirectory.mkdirs()
        
        String url = project.getPath()

        if (arguments.providedAccessKey()) {
            String token = arguments.getAccessKey();
            String[] projectOwnerAndName = project.getOwnerAndName()
            url = "https://${token}@github.com/${projectOwnerAndName[0]}/${projectOwnerAndName[1]}"
        }

        ProcessBuilder builder = ProcessRunner.buildProcess('./', 'git', 'clone', url, target)
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    
        Process process = ProcessRunner.startProcess(builder)
        process.waitFor()
  
        project.setPath(target)
    }

    private boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return commitFilter.applyFilter(project, mergeCommit)
    }

    private void collectStatistics(Project project, MergeCommit mergeCommit) {
        statCollector.collectStatistics(project, mergeCommit)
    }

    private void collectExperimentalData(Project project, MergeCommit mergeCommit) {
        dataCollector.collectExperimentalData(project, mergeCommit)
    }

    private void printProjectInformation(Project project) {
        println "STARTING PROJECT: ${project.getName()}"
    }

    private void printMergeCommitInformation(Project project, MergeCommit mergeCommit) {
        println "${project.getName()} - Merge commit: ${mergeCommit.getSHA()}"
    }

    private void endProjectAnalysis(Project project) {
        File projectDirectory = new File(project.getPath())
    }

    private void pushResults(Project project, String remoteRepositoryURL) {
        Project resultsRepository = new Project('', remoteRepositoryURL)
        printPushInformation(remoteRepositoryURL)
        String targetPath = "${LOCAL_RESULTS_REPOSITORY_PATH}/resultsRepository"
        cloneRepository(resultsRepository, targetPath)

        // Copy output files, add, commit and then push.
        FileManager.copyDirectory(getOutputPath(), "${targetPath}/output-${project.getName()}")
        Process gitAdd = ProcessRunner.runProcess(targetPath, 'git', 'add', '.')
        gitAdd.waitFor()

        def nowDate = new Date()
        def sdf = new SimpleDateFormat("dd/MM/yyyy")
        Process gitCommit = ProcessRunner
            .runProcess(targetPath, 'git', 'commit', '-m', "Analysed project ${project.getName()} - ${sdf.format(nowDate)}")
        gitCommit.waitFor()
        gitCommit.getInputStream().eachLine {
            println it
        }

        Process gitPush = ProcessRunner.runProcess(targetPath, 'git', 'push', '--force-with-lease')
        gitPush.waitFor()
        gitPush.getInputStream().eachLine {
            println it
        }

        FileManager.delete(new File(targetPath))
    }
}