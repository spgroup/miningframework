package services

import main.interfaces.DataCollector
import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager
import static main.app.MiningFramework.arguments

class BuildRequesterSemanticConflict extends BuildRequester {


    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            String branchName = mergeCommit.getSHA().take(5) + '_build_branch'
            
            checkoutCommitAndCreateBranch(project, branchName, mergeCommit.getSHA()).waitFor()
            
            File travisFile = new File("${project.getPath()}/.travis.yml")
            String[] ownerAndName = getRemoteProjectOwnerAndName(project)
            travisFile.delete()
            BuildSystem buildSystem = getBuildSystem(project)

            if (buildSystem != BuildSystem.None) {
                sendNewBuildRequest(project, travisFile, ownerAndName, buildSystem, commit, branchName, MAVEN_BUILD, "\"pom.xml\"")
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

}