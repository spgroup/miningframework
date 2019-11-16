package services

import main.interfaces.DataCollector
import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager
import static main.app.MiningFramework.arguments

class BuildRequesterDynamicSemanticConflictImpl extends BuildRequester {

    @Override
    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            findAllCommitsFromMergeScenario(mergeCommit).each { commit ->
                String branchName = commit.take(5) + '_build_branch'
                
                checkoutCommitAndCreateBranch(project, branchName, commit).waitFor()
                
                File travisFile = new File("${project.getPath()}/.travis.yml")
                String[] ownerAndName = getRemoteProjectOwnerAndName(project)
                travisFile.delete()
                BuildSystem buildSystem = getBuildSystem(project)

                if (buildSystem != BuildSystem.None) {
                    travisFile << getNewTravisFile(commit, ownerAndName[0], ownerAndName[1], buildSystem)
                    commitChanges(project, "'Trigger build #${commit}'").waitFor()
                    pushBranch(project, branchName).waitFor()
                    
                    goBackToMaster(project).waitFor()
                    println "${project.getName()} - Build requesting finished!"
                }

            }

        }
    }

    private String[] findAllCommitsFromMergeScenario(MergeCommit mergeCommit){
        return [mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit.getRightSHA(), mergeCommit.getSHA()]
    }

}