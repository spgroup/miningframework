package services

import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager

class BuildCollector {

    static private final FILE_NAME = '.travis.yml'

    static public void collectBuild(Project project, MergeCommit mergeCommit) {
        String branchName = mergeCommit.getSHA().take(5) + '_build_branch'
        
        checkoutCommitAndCreateBranch(branchName, mergeCommit.getSHA()).waitFor()

        File travisFile = new File("${project.getPath()}/.travis.yml")
        if (travisFile.delete()) {
            
            travisFile << getNewTravisFile(mergeCommit.getSHA(), projectOwner, projectName)
            commitChanges("'Trigger build #${mergeCommit.getSHA()}'").waitFor()
            pushBranch(branchName).waitFor()
        }
        
        goBackToMaster().waitFor()
    }

    private Process checkoutCommitAndCreateBranch(String branchName, String commitSha) {
        return ProcessRunner
            .runProcess(PROJECT_PATH, 'git', 'checkout', '-b', branchName, commitSha)
    }

    private String getTravisFileContent() {
        Process cat = ProcessRunner.runProcess(project.getPath(), 'cat', FILE_NAME)
        if (cat.waitFor() == 0) {
            return cat.getInputStream().getText()
        }
        throw TravisHelperException(".travis.yml file does not exist in this commit")
    }

    private Process goBackToMaster() {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

    private Process pushBranch(String branchName) {
        return ProcessRunner.runProcess(project.getPath(), 'git', 'push','-u', 'origin', branchName)
    }

    private Process commitChanges(String message) {
        return ProcessRunner
        .runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
    }

    private String getRemoteUrl() {
        return ProcessRunner.
            runProcess(project.getPath(), "git", "config", "--get", "remote.origin.url").getText()
    }

    private getNewTravisFile(String commitSha, String owner, String projectName) {
        return """
sudo: required
language: java

jdk:
    - openjdk7

script:
  - mvn package

before_deploy:
    - cd /home/travis/build/${owner}/${projectName}/target
    - tar -zcvf result.tar.gz -*

deploy:
  provider: releases
  api_key:
    secure: \$GITHUB_TOKEN
  file: result.tar.gz
  name: ${commitSha}
  file_glob: true
  overwrite: true
  skip_cleanup: true
  on:
    tags: true
    all_branches: true
            """
    }

}