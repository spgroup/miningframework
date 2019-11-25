package services

import main.interfaces.DataCollector
import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager
import static main.app.MiningFramework.arguments

class BuildRequester implements DataCollector {

    static protected final FILE_NAME = '.travis.yml'

    static protected final MAVEN_BUILD = 'mvn package -DskipTests'
    static protected final GRADLE_BUILD = './gradlew build -x test'

    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            String branchName = mergeCommit.getSHA().take(5) + '_build_branch'
            
            checkoutCommitAndCreateBranch(project, branchName, mergeCommit.getSHA()).waitFor()
            
            File travisFile = new File("${project.getPath()}/.travis.yml")
            String[] ownerAndName = getRemoteProjectOwnerAndName(project)
            travisFile.delete()
            BuildSystem buildSystem = getBuildSystem(project)

            if (buildSystem != BuildSystem.None) {
                travisFile << getNewTravisFile(mergeCommit.getSHA(), ownerAndName[0], ownerAndName[1], buildSystem)
                commitChanges(project, "'Trigger build #${mergeCommit.getSHA()}'").waitFor()
                pushBranch(project, branchName).waitFor()
                
                goBackToMaster(project).waitFor()
                println "${project.getName()} - Build requesting finished!"
            }

        }
    }

    protected BuildSystem getBuildSystem (Project project) {
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
        String remoteUrl = ProcessRunner
            .runProcess(project.getPath(), 'git', 'config', '--get', 'remote.origin.url').getText()
        String[] splitedValues = remoteUrl.split('/')
        return [splitedValues[splitedValues.size() - 2], splitedValues[splitedValues.size() - 1]]
    }

    static private Process goBackToMaster(Project project) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

    static private Process pushBranch(Project project, String branchName) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'push','-u', 'origin', branchName)
    }

    static protected Process commitChanges(Project project, String message) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", ".travis.yml").waitFor()

        return ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
    }

    static private String getRemoteUrl(Project project) {
        return ProcessRunner.
            runProcess(project.getPath(), "git", "config", "--get", "remote.origin.url").getText()
    }

    static protected getNewTravisFile(String commitSha, String owner, String projectName, BuildSystem buildSystem) {
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
  api_key:
    secure: \$GITHUB_TOKEN
  file: result.tar.gz
  name: fetchjar-${commitSha}
  file_glob: true
  overwrite: true
  skip_cleanup: true
  on:
    all_branches: true 
            """
    }

}