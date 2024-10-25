package app

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.text.SimpleDateFormat

import static app.MiningFramework.arguments
import java.util.concurrent.BlockingQueue

import project.*
import interfaces.*
import exception.UnstagedChangesException
import util.*

import services.util.Utils;

class MiningWorker implements Runnable {
    private static Logger LOG = LogManager.getLogger(MiningWorker.class)

    private Set<DataCollector> dataCollectors
    private CommitFilter commitFilter
    private BlockingQueue<Project> projectList
    private String baseDir

    MiningWorker(Set<DataCollector> dataCollectors, CommitFilter commitFilter, BlockingQueue<Project> projectList, String baseDir) {
        this.dataCollectors = dataCollectors
        this.commitFilter = commitFilter
        this.projectList = projectList
        this.baseDir = baseDir
    }

    void run() {
        while (!projectList.isEmpty()) {
            try {
                Project project = projectList.remove()

                println "STARTING PROJECT: ${project.getName()}"

                if (project.isRemote()) {
                    cloneRepository(project, "${baseDir}/${project.getName()}")
                } else {
                    checkForUnstagedChanges(project);
                }

                def (mergeCommits, skipped) = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate(), arguments.getIncludePullRequestBranches())
                for (mergeCommit in mergeCommits) {
                    try {
                        if (commitFilter.applyFilter(project, mergeCommit)) {
                            println "${project.getName()} - Merge commit: ${mergeCommit.getSHA()}"

                            runDataCollectors(project, mergeCommit)
                        }
                    } catch (Exception e) {
                        println "${project.getName()} - ${mergeCommit.getSHA()} - ERROR"
                        e.printStackTrace();
                    }
                }

                updateSkippedCommitsSpreadsheet(project, skipped)

                if (arguments.isPushCommandActive()) // Will push.
                    pushResults(project, arguments.getResultsRemoteRepositoryURL())

                if (!arguments.getKeepProjects()) {
                    FileManager.delete(new File(project.getPath()))
                } else {
                    MergeHelper.returnToMaster(project)
                }

            } catch (NoSuchElementException e) {
                println e.printStackTrace()
            }
        }
    }

    private void runDataCollectors(Project project, MergeCommit mergeCommit) {
        for (dataCollector in dataCollectors) {
            dataCollector.collectData(project, mergeCommit)
        }
    }

    private void checkForUnstagedChanges(Project project) {
        String gitDiffOutput = ProcessRunner.runProcess(project.getPath(), "git", "diff").getText()

        if (gitDiffOutput.length() != 0) {
            throw new UnstagedChangesException(project.getName())
        }
    }

    private void cloneRepository(Project project, String target) {
        LOG.info("Cloning repository ${project.getName()} into ${target}")

        File projectDirectory = new File(target)
        if (projectDirectory.exists()) {
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
        Process process = ProcessRunner.startProcess(builder)
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
        LOG.info("Finished cloning repository ${project.getName()} into ${target}")

        if (arguments.getIncludePullRequestBranches()) {
            Process fetchProcess = ProcessRunner.runProcess(target, 'git', 'fetch', 'origin', 'refs/pull/*/head:refs/remotes/origin/pull/*')
            fetchProcess.waitFor()
            fetchProcess.getInputStream().eachLine(LOG::trace)
        }

        project.setPath(target)
    }


    private void pushResults(Project project, String remoteRepositoryURL) {
        Project resultsRepository = new Project('', remoteRepositoryURL)
        String targetPath = "/resultsRepository"
        cloneRepository(resultsRepository, targetPath)

        // Copy output files, add, commit and then push.
        FileManager.copyDirectory(arguments.getOutputPath(), "${targetPath}/output-${project.getName()}")
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

    private void updateSkippedCommitsSpreadsheet(Project project, List<String> skipped) {
        String spreadsheetName = 'skipped-merge-commits'
        String spreadsheetHeader = 'project,merge commit'

        File spreadsheet = FileManager.createSpreadsheet(Utils.getOutputPath(), spreadsheetName, spreadsheetHeader)
        skipped.each { mergeCommit ->
            FileManager.appendLineToFile(spreadsheet, "${project.getName()},${mergeCommit}")
        }
    }
}