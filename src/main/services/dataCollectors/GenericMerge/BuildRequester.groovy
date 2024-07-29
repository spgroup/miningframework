package services.dataCollectors.GenericMerge

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.util.Utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class BuildRequester {
    private static Logger LOG = LogManager.getLogger(BuildRequester.class)

    static requestBuildWithRevision(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String mergeTool) {
        String toReplaceFile = "merge.${mergeTool.toLowerCase()}.java"

        String branchName = "${mergeCommit.getSHA().take(7)}-${mergeTool}"

        createBranchFromCommit(project, mergeCommit, branchName)
        replaceFilesInProject(project, mergeCommit, mergeScenarios, toReplaceFile)
        createOrReplaceGithubActionsFile(project)
        def commitSha = stageAndPushChanges(project, branchName, "Mining Framework Analysis")

        def reportFile = new File(GenericMergeConfig.BUILD_REQUESTER_REPORT_PATH)
        reportFile.createNewFile()
        reportFile.append("${project.getName()},${branchName},${mergeTool},${commitSha}\n")
    }

    private static void createBranchFromCommit(Project project, MergeCommit mergeCommit, String branchName) {
        Path projectPath = Paths.get(project.getPath())

        // Checkout to new branch
        Utils.runGitCommand(projectPath, 'checkout', '-b', branchName, mergeCommit.getSHA())
    }

    private static void replaceFilesInProject(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String toReplaceFile) {
        mergeScenarios.stream()
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
    build:
        runs-on: ubuntu-latest
        steps:
            - uses: actions/checkout@v2
            - uses: actions/setup-java@v1
              with:
                java-version: 11
            - run: ${buildSystem.getBuildCommand()}
    test:
        runs-on: ubuntu-latest
        steps:
            - uses: actions/checkout@v2
            - uses: actions/setup-java@v1
              with:
                java-version: 11
            - run: ${buildSystem.getTestCommand()}
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

    private static String stageAndPushChanges(Project project, String branchName, String commitMessage) {
        Path projectPath = Paths.get(project.getPath())

        // Stage changes
        Utils.runGitCommand(projectPath, 'add', '.')

        // Commit changes
        Utils.runGitCommand(projectPath, 'commit', '-m', commitMessage)
        def commitSha = Utils.runGitCommand(projectPath, 'rev-parse', 'HEAD')
        LOG.debug("Created commit with hash ${commitSha}")

        // Push changes
        Utils.runGitCommand(projectPath, 'push', '--set-upstream', 'origin', branchName, '--force-with-lease')

        return commitSha.get(0)
    }

    private static interface BuildSystem {
        String getBuildCommand()

        String getTestCommand()
    }

    private static class MavenBuildSystem implements BuildSystem {
        @Override
        String getBuildCommand() {
            return "mvn package -Dskiptests"
        }

        @Override
        String getTestCommand() {
            return "mvn test"
        }
    }

    private static class GradleBuildSystem implements BuildSystem {
        @Override
        String getBuildCommand() {
            return "./gradlew assemble"
        }

        @Override
        String getTestCommand() {
            return "./gradlew test"
        }
    }

    private static class NoopBuildSystem implements BuildSystem {
        @Override
        String getBuildCommand() {
            return "echo no build available"
        }

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
