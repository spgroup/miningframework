package services.dataCollectors

import interfaces.DataCollector
import project.*
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

    static private final FILE_NAME = '.travis.yml'

    enum BuildSystem {
        Maven,
        Gradle,
        None
    }

    static private final MAVEN_BUILD = 'mvn package -DskipTests'
    static private final GRADLE_BUILD = './gradlew build -x test'

    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            String[] ownerAndName = getRemoteProjectOwnerAndName(project)
            String projectOwner = ownerAndName[0]
            String projectName = ownerAndName[1]

            if (!buildAlreadyExists(projectOwner, projectName, mergeCommit)) {
                String branchName = generateBranchName(mergeCommit)

                checkoutCommitAndCreateBranch(project, branchName, mergeCommit.getSHA()).waitFor()
                
                File travisFile = getTravisFile(project)
                travisFile.delete()
                BuildSystem buildSystem = getBuildSystem(project)

                if (buildSystem != BuildSystem.None) {
                    travisFile << getNewTravisFile(mergeCommit.getSHA(), ownerAndName[0], ownerAndName[1], buildSystem)
                    commitChanges(project, "'Trigger build #${mergeCommit.getSHA()}'").waitFor()
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

    private File getTravisFile(Project project) {
        return new File("${project.getPath()}/${FILE_NAME}")
    }

    private String generateBranchName(MergeCommit mergeCommit) {
        return mergeCommit.getSHA().take(5) + "_build_branch_${getCurrentTimestamp()}"
    }

    private boolean buildAlreadyExists(String projectOwner, String projectName, MergeCommit mergeCommit) {
        GithubHelper githubHelper = new GithubHelper(arguments.getAccessKey())

        def releases = githubHelper.getRepositoryReleases(projectOwner, projectName);

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

    static private Process checkoutCommitAndCreateBranch(Project project, String branchName, String commitSha) {
        return ProcessRunner
            .runProcess(project.getPath(), 'git', 'checkout', '-b', branchName, commitSha)
    }

    static private String[] getRemoteProjectOwnerAndName(Project project) {
        String remoteUrl = getRemoteUrl(project)
        String[] splitedValues = remoteUrl.split('/')
        return [splitedValues[splitedValues.size() - 2], splitedValues[splitedValues.size() - 1]]
    }

    static private String getRemoteUrl(Project project) {
        String remoteUrl = ProcessRunner.runProcess(project.getPath(), 'git', 'config', '--get', 'remote.origin.url').getText()

        return remoteUrl.trim()
    } 

    static private Process goBackToMaster(Project project) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

    static private Process pushBranch(Project project, String branchName) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'push', 'origin', branchName)
    }

    static private Process commitChanges(Project project, String message) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", ".travis.yml").waitFor()

        return ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
    }


    static private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    }

    static private getNewTravisFile(String commitSha, String owner, String projectName, BuildSystem buildSystem) {
        String buildCommand = "";
        if (buildSystem == BuildSystem.Maven) {
            buildCommand = MAVEN_BUILD
        } else if (buildSystem == BuildSystem.Gradle) {
            buildCommand = GRADLE_BUILD
        }
    
        String trimmedProjectName = projectName.replace('\n', '')
        return """
sudo: required
language: java

jdk:
  - openjdk8

script:
  - ${buildCommand}

before_deploy:
    - mkdir MiningBuild
    - find . -name '*.jar' -exec cp {} ./MiningBuild \\;
    - cd /home/travis/build/${owner}/${trimmedProjectName}/MiningBuild
    - tar -zcvf result.tar.gz *
deploy:
  provider: releases
  api_key: \$GITHUB_TOKEN
  file: result.tar.gz
  name: fetchjar-${commitSha}
  file_glob: true
  overwrite: false
  skip_cleanup: true
  on:
    all_branches: true 
            """
    }

}