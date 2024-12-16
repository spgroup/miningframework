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
        LOG.debug("Setting up credentials")
        setupCredentials(project)
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
        def process = ProcessRunner.runProcess(project.getPath(), 'git', 'remote', 'add', 'analysis', origin)
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
    }

    static private void setupCredentials(Project project) {
        def configEmail = ProcessRunner.runProcess(project.getPath(), 'git', 'config', 'user.email', '"joao.pedro.hsd@gmail.com"')
        configEmail.getInputStream().eachLine(LOG::trace)
        configEmail.getErrorStream().eachLine(LOG::warn)
        configEmail.waitFor()
        def configName = ProcessRunner.runProcess(project.getPath(), 'git', 'config', 'user.name', '"Joao Pedro"')
        configName.getInputStream().eachLine(LOG::trace)
        configName.getErrorStream().eachLine(LOG::warn)
        configName.waitFor()
    }

    static private void deleteBranch(Project project, String branchName) {
        def process = ProcessRunner.runProcess(project.getPath(), 'git', 'branch', '-D', branchName)
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
    }

    static private void checkoutCommitAndCreateBranch(Project project, String branchName, String commitSha) {
        def process = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', '-b', branchName, commitSha)
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
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
                    LOG.debug("Copying file ${file.resolve(this.fileName)} to ${destination}")
                    Files.copy(file.resolve(this.fileName), destination, StandardCopyOption.REPLACE_EXISTING)
                })
    }

    static private void pushBranch(Project project, String branchName) {
        def process = ProcessRunner.runProcess(project.getPath(), 'git', 'push', 'analysis', branchName, "-f")
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
    }

    static protected void commitChanges(Project project, String message) {
        def process = ProcessRunner.runProcess(project.getPath(), "git", "add", ".")
        process.getInputStream().eachLine(LOG::trace)
        process.getErrorStream().eachLine(LOG::warn)
        process.waitFor()
        def commit = ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
        commit.getInputStream().eachLine(LOG::trace)
        commit.getErrorStream().eachLine(LOG::warn)
        commit.waitFor()
    }
}
