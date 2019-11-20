package services.S3MHandlersAnalysis.util

import main.app.MiningFramework
import main.project.MergeCommit
import main.project.Project
import main.util.GithubHelper
import main.util.HttpHelper
import main.util.TravisHelper
import services.S3MHandlersAnalysis.Handlers

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

final class BuildRequester {

    private static enum BuildSystem {
        Maven,
        Gradle,
        None
    }

    private static final String projectOwnerName = getOwnerName()
    private static final String travisAPIToken = getTravisToken()

    private static Map<String, String> buildScripts = ['Maven': 'mvn package', 'Gradle': './gradlew build']

    /**
     * Replaces the files in a project by its correspondent merge results in a new branch and triggers a Travis build from a push
     * @param project
     * @param mergeCommit
     * @param mergeScenarios
     * @param mergeAlgorithmIndex
     * @return the link for the Travis build triggered by this method
     */
    static String requestBuildWithRevision(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, int mergeAlgorithmIndex) {
        String toReplaceFile = Handlers.mergeResultPaths[mergeAlgorithmIndex]
        String mergeAlgorithm = Handlers.mergeAlgorithms[mergeAlgorithmIndex]

        String branchName = "${mergeCommit.getSHA().take(7)}-${mergeAlgorithm}"

        createBranchFromCommit(project, mergeCommit, branchName)
        replaceFilesInProject(project, mergeCommit, mergeScenarios, toReplaceFile)
        replaceTravisFile(project)
        stageAndPushChanges(project, branchName)

        Thread.sleep(2000) // sleep to give time to Travis to compute
        return getBuildLink(project, branchName)
    }

    private static String getBuildLink(Project project, String branchName) {
        String buildID = getBuildAttribute(project, 'id', branchName)
        return "https://travis-ci.com/${projectOwnerName}/${project.getName()}/builds/${buildID}"
    }

    static String getBuildAttribute(Project project, String attribute, String branchName) {
        String url = "https://api.travis-ci.com/repo/${projectOwnerName}%2F${project.getName()}/branch/${branchName}"
        HttpURLConnection connection = new URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Travis-API-Version", "3")
        connection.setRequestProperty("Authorization", "token ${travisAPIToken}")

        if (connection.getResponseCode() != 200) {
            Thread.sleep(3000) // sleep for some seconds and try again
            return getBuildAttribute(project, attribute, branchName)
        }

        def last_build = HttpHelper.responseToJSON(connection.getInputStream())['last_build']
        if (last_build == null) {
            Thread.sleep(3000) // sleep for some seconds and try again
            return getBuildAttribute(project, attribute, branchName)
        }
        return last_build[attribute]
    }

    private static String getOwnerName() {
        String githubToken = MiningFramework.arguments.getAccessKey()
        GithubHelper githubHelper = new GithubHelper(githubToken)

        return githubHelper.getUser()['login']
    }

    private static String getTravisToken() {
        String githubToken = MiningFramework.arguments.getAccessKey()
        TravisHelper travisHelper = new TravisHelper(githubToken)

        return travisHelper.getToken()
    }

    private static void createBranchFromCommit(Project project, MergeCommit mergeCommit, String branchName) {
        Path projectPath = Paths.get(project.getPath())

        // Checkout to new branch
        Utils.runGitCommand(projectPath, 'checkout', '-b', branchName, mergeCommit.getSHA())
    }

    private static void replaceFilesInProject(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String toReplaceFile) {
        mergeScenarios.stream()
                .forEach(mergeScenario -> Files.copy(getSource(mergeScenario, toReplaceFile), getTarget(project, mergeCommit, mergeScenario), StandardCopyOption.REPLACE_EXISTING))
    }

    private static Path getSource(Path mergeScenario, String toReplaceFile) {
        return mergeScenario.resolve(toReplaceFile)
    }

    private static Path getTarget(Project project, MergeCommit mergeCommit, Path mergeScenario) {
        Path projectPath = Paths.get(project.getPath())
        Path filePath = Utils.commitFilesPath(project, mergeCommit).relativize(mergeScenario)
        return projectPath.resolve(filePath)
    }

    private static void replaceTravisFile(Project project) {
        Path projectPath = Paths.get(project.getPath())

        Path travisFile = projectPath.resolve('.travis.yml')
        BuildSystem buildSystem = getBuildSystem(projectPath)

        if (buildSystem != BuildSystem.None && !Files.exists(travisFile)) {
            travisFile.toFile() << buildNewTravisFile(buildSystem)
        }
    }

    private static BuildSystem getBuildSystem(Path projectPath) {
        if (Files.exists(projectPath.resolve('pom.xml'))) {
            return BuildSystem.Maven
        }

        if (Files.exists(projectPath.resolve('build.gradle'))) {
            return BuildSystem.Gradle
        }

        return BuildSystem.None
    }

    private static String buildNewTravisFile(BuildSystem buildSystem) {
        return """
language: java

jdk:
  - openjdk8

script:
  - ${buildScripts[buildSystem.name()]}
"""
    }

    private static void stageAndPushChanges(Project project, String branchName) {
        Path projectPath = Paths.get(project.getPath())

        // Stage changes
        Utils.runGitCommand(projectPath, 'add', '.')

        // Commit changes
        Utils.runGitCommand(projectPath, 'commit', '-m', 'S3M Handlers Analysis new branch')

        // Push changes
        Utils.runGitCommand(projectPath, 'push', '--set-upstream', 'origin', branchName, '--force-with-lease')
    }


}
