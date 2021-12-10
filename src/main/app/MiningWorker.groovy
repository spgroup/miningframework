package app

import java.text.SimpleDateFormat

import static app.MiningFramework.arguments
import java.util.concurrent.BlockingQueue

import project.*
import interfaces.*
import exception.UnstagedChangesException
import util.*

class MiningWorker implements Runnable {

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
                //List<MergeCommit> mergeCommits = project.getMergeCommits("2009-03-25", "2009-10-14")
                List<MergeCommit> mergeCommits = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate())
                for (mergeCommit in mergeCommits) {
                    obtainResultsForProject(project,mergeCommit)
                    try {
                        if(
                        mergeCommit.getSHA().equals("4753091384ea51c56cc09a0f96666ea9a4c2e03e") ||
                        mergeCommit.getSHA().equals("041f9b9fecc8f591f77c0e66ba8f95a1ad0ff146") ||
                        mergeCommit.getSHA().equals("56f158694a3533de50bea5889ad892a80242612a") ||
                        mergeCommit.getSHA().equals("120d8b53950318d11bd1a2aa65da1c01eba53583") ||
                        mergeCommit.getSHA().equals("706aa7ad95f9461b58afb480dab6441b861fd24d") ||
                        mergeCommit.getSHA().equals("211231d12b2d5e998421ab7a332f955800d5b601") ||
                                mergeCommit.getSHA().equals("3eea078e83644c1728ac62a42ec06e26c7288051") ||
                                mergeCommit.getSHA().equals("5e26af25e0313093256a49e2e1bc66a797cd4038") ||
                                mergeCommit.getSHA().equals("71e78b45b025fbbd8fd8e9c1519a078a59039eeb") ||
                                mergeCommit.getSHA().equals("7886b73c1c1ec484f36844d9ee8472e68d9dc4b7") ||
                                mergeCommit.getSHA().equals("9801bd961dcde1815ae3a590b769cd7bbef17481") ||
                                mergeCommit.getSHA().equals("a621099bf1a9082b115c173a9d9416e8027db298") ||
                                mergeCommit.getSHA().equals("ab4cd01a8f244e49c4a3548d9160f6610f0edc94") ||
                                mergeCommit.getSHA().equals("f6373f7ada14a7914f4ae08b6af4c1b27d343c21") ||
                                mergeCommit.getSHA().equals("fc517765d752ee8098ea48c052f6a709c5b451c9")
                        ){
                       // if(mergeCommit.getSHA().equals("5e26af25e0313093256a49e2e1bc66a797cd4038")){
                            if (commitFilter.applyFilter(project, mergeCommit)) {
                               println "${project.getName()} - Merge commit: ${mergeCommit.getSHA()}"

                                runDataCollectors(project, mergeCommit)
                            }
                        }
                    } catch (Exception e) {
                        println "${project.getName()} - ${mergeCommit.getSHA()} - ERROR"
                        e.printStackTrace();
                    }
                }

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
        println "Cloning repository ${project.getName()} into ${target}"

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
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)

        Process process = ProcessRunner.startProcess(builder)
        process.waitFor()

        project.setPath(target)
    }
    private void obtainResultsForProject(Project project , MergeCommit mergeCommit) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/commits_"+project.getName()+".csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'Merge commit; Ancestor; Parent 1; Parent 2\n'
        }
        obtainResultsForProjects  << "${mergeCommit.getSHA()};${mergeCommit.getAncestorSHA()};${mergeCommit.getLeftSHA()};${mergeCommit.getRightSHA()};\n"
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
}