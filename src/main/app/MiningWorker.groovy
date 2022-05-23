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
             //    List<MergeCommit> mergeCommits = project.getMergeCommits("", "05/29/2019")
                List<MergeCommit> mergeCommits = project.getMergeCommits(arguments.getSinceDate(), arguments.getUntilDate())
                obtainResultsForProject(project,mergeCommits)
                for (mergeCommit in mergeCommits) {
                    try {
                       /*if(

                          mergeCommit.getSHA().equals("b41ca6e3195dcf60a842632b37b90059b7cd9960") ||
                          mergeCommit.getSHA().equals("d86925ca0efc051259a35f917753080868614f0f") ||
                          mergeCommit.getSHA().equals("a9552af41bc4a415a79c551c9f8d7a16b8d82900") ||
                          mergeCommit.getSHA().equals("d4b14b8ec0013d0c35f361d1851e28a87456dab3")

                        ){*/
                       // if(mergeCommit.getSHA().equals("4f8fc5bc5b6b33537c53ddf89b57ba865c716460")){
                            if (commitFilter.applyFilter(project, mergeCommit)) {
                               println "${project.getName()} - Merge commit: ${mergeCommit.getSHA()}"

                                runDataCollectors(project, mergeCommit)
                            }
                      //  }
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
    private void obtainResultsForProject(Project project , List<MergeCommit> mergeCommits) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/1_results_merges_scenarios_"+project.getName()+".csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'Merge commit, Parent 1, Parent 2\n'
        }
        for(int i = mergeCommits.size()-1;i>=0;i--){
            MergeCommit mergeCommit = mergeCommits.get(i);
       // for (mergeCommit in mergeCommits) {
            obtainResultsForProjects << "${mergeCommit.getSHA()},${mergeCommit.getLeftSHA()},${mergeCommit.getRightSHA()}\n"
        }
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