package services.outputProcessors.genericMerge

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.util.Utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class BuildRequester {
    private static Logger LOG = LogManager.getLogger(BuildRequester.class)

    static requestBuildForCommitSha(Project project, String commitSha) {
        def projectPath = Paths.get(project.getPath())

        String branchName = "${commitSha.take(7)}-mining-framework-build"
        def branchExistsCheck = Utils.runGitCommand(projectPath, "show-ref", "refs/head/${branchName}")
        if (branchExistsCheck == 0) {
            LOG.info("Skipping build request for commit ${commitSha} on project ${project.getName()} because the branch already exists")
            return
        }

        createBranchFromCommit(project, commitSha, branchName)
        createOrReplaceGithubActionsFile(project)
        stageAndCommitChanges(projectPath)

        def reportFile = new File(GenericMergeConfig.BUILD_REQUESTER_REPORT_PATH)
        reportFile.createNewFile()
        reportFile.append("${project.getName()},${branchName}\n")

        LOG.info("Successfully requested build for commit ${commitSha} on project ${project.getName()}")
    }

    static requestBuildWithRevision(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String mergeTool) {
        String toReplaceFile = "merge.${mergeTool.toLowerCase()}.java"

        String branchName = "${mergeCommit.getSHA().take(7)}-${mergeTool}-mining-framework-build"

        createBranchFromCommit(project, mergeCommit.getSHA(), branchName)
        replaceFilesInProject(project, mergeCommit, mergeScenarios, toReplaceFile)
        createOrReplaceGithubActionsFile(project)
        stageAndCommitChanges(Paths.get(project.getPath()))

        def reportFile = new File(GenericMergeConfig.BUILD_REQUESTER_REPORT_PATH)
        reportFile.createNewFile()
        reportFile.append("${project.getName()},${branchName}\n")
    }

    private static void createBranchFromCommit(Project project, String commitSha, String branchName) {
        Path projectPath = Paths.get(project.getPath())

        // Checkout to new branch
        Utils.runGitCommand(projectPath, 'checkout', '-b', branchName, commitSha)
    }

    private static void replaceFilesInProject(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String toReplaceFile) {
        mergeScenarios.stream()
                .filter(mergeScenario -> {
                    LOG.debug("The file ${getSource(mergeScenario, toReplaceFile)} exists? ${Files.exists(getSource(mergeScenario, toReplaceFile))}")
                    return Files.exists(getSource(mergeScenario, toReplaceFile))
                })
                .forEach(mergeScenario -> {
                    LOG.debug("Trying to copy " + getSource(mergeScenario, toReplaceFile) + " into " + getTarget(project, mergeCommit, mergeScenario))
                    Files.copy(getSource(mergeScenario, toReplaceFile), getTarget(project, mergeCommit, mergeScenario), StandardCopyOption.REPLACE_EXISTING)
                })
    }

    private static void createOrReplaceGithubActionsFile(Project project) {
        LOG.debug("Starting creation of github actions file")
        def githubActionsFilePath = "${Paths.get(project.getPath()).toAbsolutePath().toString()}/.github/workflows"
        LOG.debug("Location of github actions folder ${githubActionsFilePath}")

        def buildSystem = getBuildSystemForProject(project)
        LOG.debug("Using ${buildSystem.class.getSimpleName()} as build system for project ${project.getName()}")

        def githubActionsContent = """
name: Mining Framework Check
on: [push]
jobs:
    test:
        runs-on: ubuntu-latest
        strategy:
            matrix:
                java-version: [8, 11, 17]
        steps:
            - uses: actions/checkout@v4
            - name: Set up Java \${{ matrix.java-version }}
              uses: actions/setup-java@v1
              with:
                java-version: \${{ matrix.java-version }}
                distribution: 'corretto'
            - name: Run tests
              uses: nick-fields/retry@v3
              with:
                timeout_minutes: 30
                max_attempts: 5
                command: ${buildSystem.getTestCommand()}
"""
        Files.createDirectories(Paths.get(githubActionsFilePath))
        def file = new File("${githubActionsFilePath}/mining_framework.yml")
        file.createNewFile()
        file.write(githubActionsContent)
        LOG.debug("Finished creation of github actions file")
    }

    private static Path getSource(Path mergeScenario, String toReplaceFile) {
        return mergeScenario.resolve(toReplaceFile)
    }

    private static Path getTarget(Project project, MergeCommit mergeCommit, Path mergeScenario) {
        Path projectPath = Paths.get(project.getPath())
        Path filePath = Utils.commitFilesPath(project, mergeCommit).relativize(mergeScenario)
        return projectPath.resolve(filePath)
    }

    private static String stageAndCommitChanges(Path projectPath) {
        Utils.runGitCommand(projectPath, 'add', '.')
        Utils.runGitCommand(projectPath, 'commit', '-m', "Mining Framework Analysis")
    }

    private static interface BuildSystem {
        String getTestCommand()
    }

    private static class MavenBuildSystem implements BuildSystem {
        @Override
        String getTestCommand() {
            return "mvn clean test"
        }
    }

    private static class GradleBuildSystem implements BuildSystem {
        @Override
        String getTestCommand() {
            return "./gradlew clean test"
        }
    }

    private static class NoopBuildSystem implements BuildSystem {
        @Override
        String getTestCommand() {
            return "echo no test available"
        }
    }

    private static BuildSystem getBuildSystemForProject(Project project) {
        File mavenFile = new File("${project.getPath()}/pom.xml")
        File gradleFile = new File("${project.getPath()}/build.gradle")

        if (mavenFile.exists()) {
            return new MavenBuildSystem()
        } else if (gradleFile.exists()) {
            return new GradleBuildSystem()
        } else {
            return new NoopBuildSystem()
        }
    }
}
