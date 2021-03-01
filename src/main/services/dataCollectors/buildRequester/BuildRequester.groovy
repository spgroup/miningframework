package services.dataCollectors.buildRequester

import com.google.inject.Inject
import interfaces.DataCollector
import project.*
import services.util.ci.CIPlatform
import util.GithubHelper
import util.ProcessRunner

import java.text.SimpleDateFormat

import static app.MiningFramework.arguments


/**
 * @requires: that the access key argument is passed and that the project has one of the following build systems:
 * Maven or Gradle and that the project is a github project (the project doesn't need to have a travis configuration file)
 * otherwise it will not be executed
 * @provides: creates a branch with a name following the format: [merge commit's reduced sha]_build_branch_[timestamp] with a custom travis file
 * and pushes it to the project, triggering a travis build, that will deploy the jars to the github repository's releases section
 */
class BuildRequester implements DataCollector {
    private CIPlatform ciPlatform

    @Inject
    BuildRequester(CIPlatform ciPlatform) {
        this.ciPlatform = ciPlatform
    }

    enum BuildSystem {
        Maven,
        Gradle,
        None
    }

    static private final MAVEN_BUILD = 'mvn -DskipTests=true package'
    static private final GRADLE_BUILD = './gradlew assemble testClasses'

    void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            if (!buildAlreadyExists(project, mergeCommit)) {
                String branchName = generateBranchName(mergeCommit)

                checkoutCommitAndCreateBranch(project, branchName, mergeCommit.getSHA()).waitFor()

                File configurationFile = ciPlatform.getConfigurationFile(project)
                configurationFile.delete()
                BuildSystem buildSystem = getBuildSystem(project)

                if (buildSystem != BuildSystem.None) {
                    String buildCommand = getBuildCommand(buildSystem)

                    configurationFile.getParentFile().mkdirs()
                    configurationFile << ciPlatform.generateConfiguration(project, mergeCommit.getSHA(), buildCommand)

                    commitChanges(project, configurationFile, "'Trigger build #${mergeCommit.getSHA()}'").waitFor()
                    pushBranch(project, branchName).waitFor()
                    
                    goBackToMaster(project).waitFor()
                    println "${project.getName()} - Build requesting finished!"
                }                
            } else {
                println "${project.getName()} - Build requesting skiped: build already exists"
            }
        } else {
            println "${project.getName()} - Build requesting skiped: access key not provided"
        }
    }

    private String generateBranchName(MergeCommit mergeCommit) {
        return mergeCommit.getSHA().take(5) + "_build_branch_${getCurrentTimestamp()}"
    }

    private boolean buildAlreadyExists(Project project, MergeCommit mergeCommit) {
        GithubHelper githubHelper = new GithubHelper(arguments.getAccessKey())

        def releases = githubHelper.getRepositoryReleases(project);

        def mergeCommitRelease = releases.find { release -> release.name.endsWith(mergeCommit.getSHA()) }

        return mergeCommitRelease != null
    }

    private BuildSystem getBuildSystem (Project project) {
        File mavenFile = new File("${project.getPath()}/pom.xml")
        File gradleFile = new File("${project.getPath()}/build.gradle")

        if (mavenFile.exists()) {
            return BuildSystem.Maven
        } else if (gradleFile.exists()) {
            return BuildSystem.Gradle
        } else {
            return BuildSystem.None
        }
    }

    private String getBuildCommand(BuildSystem buildSystem) {
        switch (buildSystem) {
            case BuildSystem.Maven:
                return MAVEN_BUILD
            case BuildSystem.Gradle:
                return GRADLE_BUILD
            default:
                return ""
        }
    }

    static private Process checkoutCommitAndCreateBranch(Project project, String branchName, String commitSha) {
        return ProcessRunner
            .runProcess(project.getPath(), 'git', 'checkout', '-b', branchName, commitSha)
    }

    static private Process goBackToMaster(Project project) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

    static private Process pushBranch(Project project, String branchName) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'push', 'origin', branchName)
    }

    static private Process commitChanges(Project project, File file, String message) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", file.getAbsolutePath()).waitFor()

        return ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
    }

    static private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    }

}