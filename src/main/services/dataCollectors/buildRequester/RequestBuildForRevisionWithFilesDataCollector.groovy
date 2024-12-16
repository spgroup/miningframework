package services.dataCollectors.buildRequester

import interfaces.DataCollector
import org.apache.logging.log4j.LogManager
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static app.MiningFramework.arguments

class RequestBuildForRevisionWithFilesDataCollector implements DataCollector {
    private static LOG = LogManager.getLogger(RequestBuildForRevisionWithFilesDataCollector.class)

    private String fileName

    RequestBuildForRevisionWithFilesDataCollector(String fileName) {
        this.fileName = fileName
    }

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        def branchName = "mining-framework-analysis-${project.getName()}-${mergeCommit.getSHA()}-${fileName}"
        LOG.debug("Attaching origin to project")
        attachOrigin(project)
        LOG.debug("Deleting and creating branch")
        deleteBranch(project, branchName)
        LOG.debug("Checking out branch")
        checkoutCommitAndCreateBranch(project, branchName, mergeCommit.getSHA())
        LOG.debug("Copying files")
        copyFilesIntoRevision(project, mergeCommit)
        GithubActionsHelper.createGitHubActionsFile(project)
        LOG.debug("Comitting files")
        commitChanges(project, "Mining Framework Analysis")
        LOG.debug("Pushing analysis")
        pushBranch(project, branchName)
    }

    static private void attachOrigin(Project project) {
        def token = arguments.getAccessKey()
        def origin = "https://${token}@github.com/jpedroh/mining-framework-analysis"
        ProcessRunner.runProcess(project.getPath(), 'git', 'remote', 'add', 'analysis', origin).waitFor()
    }

    static private void deleteBranch(Project project, String branchName) {
        ProcessRunner.runProcess(project.getPath(), 'git', 'branch', '-D', branchName).waitFor()
    }

    static private void checkoutCommitAndCreateBranch(Project project, String branchName, String commitSha) {
        ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', '-b', branchName, commitSha).waitFor()
    }

    private copyFilesIntoRevision(Project project, MergeCommit mergeCommit) {
        def scenarioFiles = MergeScenarioCollector.collectNonFastForwardMergeScenarios(project, mergeCommit)
        scenarioFiles.stream()
                .filter(file -> {
                    if (Files.notExists(file.resolve(this.fileName))) {
                        LOG.debug("Skipping copy of file ${file.resolve(this.fileName).toAbsolutePath().toString()} because it does not exist")
                        return false
                    }
                    return true
                })
                .forEach(file -> {
                    def destination = Paths.get(project.getPath()).resolve(file.toString().substring(file.toString().indexOf(mergeCommit.getSHA()) + 1 + mergeCommit.getSHA().length()))
                    LOG.debug("Copying file ${file} to ${destination}")
                    Files.copy(file.resolve(this.fileName), destination, StandardCopyOption.REPLACE_EXISTING)
                })
    }

    static private void pushBranch(Project project, String branchName) {
        ProcessRunner.runProcess(project.getPath(), 'git', 'push', 'analysis', branchName, "-f").waitFor()
    }

    static protected void commitChanges(Project project, String message) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", ".").waitFor()
        ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}").waitFor()
    }
}
