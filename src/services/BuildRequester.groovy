package services

import main.interfaces.DataCollector
import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager
import static main.app.MiningFramework.arguments

abstract class BuildRequester implements DataCollector {

    static protected final FILE_NAME = '.travis.yml'

    static protected final MAVEN_BUILD = 'mvn package -DskipTests'
    static protected final GRADLE_BUILD = './gradlew build -x test'

    
    abstract public void collectData(Project project, MergeCommit mergeCommit);

    abstract protected BuildSystem getBuildSystem (Project project);

    //abstract protected Process commitChanges(Project project, String message, String[] parameters);

    protected Process commitChanges(Project project, String message, String parameters) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", parameters).waitFor()

        return ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
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

    static private String getRemoteUrl(Project project) {
        return ProcessRunner.
            runProcess(project.getPath(), "git", "config", "--get", "remote.origin.url").getText()
    }

    protected void sendNewBuildRequest(Project project, File travisFile, String[] ownerAndName, BuildSystem buildSystem, String commit, String branchName, String mavenComand, String parameters){
        travisFile << getNewTravisFile(commit, ownerAndName[0], ownerAndName[1], buildSystem, mavenComand)
        commitChanges(project, "'Trigger build #${commit}'", parameters).waitFor()
        pushBranch(project, branchName).waitFor()
        goBackToMaster(project).waitFor()
        println "${project.getName()} - Build requesting finished!"
    }

    static protected getNewTravisFile(String commitSha, String owner, String projectName, BuildSystem buildSystem, String mavenBuildCommand) {
        String buildCommand = "";
        if (buildSystem == BuildSystem.Maven) {
            buildCommand = mavenBuildCommand
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