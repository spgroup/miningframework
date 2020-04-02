package services.projectProcessors

import exception.*
import interfaces.ProjectProcessor
import project.*
import util.*

import static app.MiningFramework.arguments

class ForkAndEnableTravisProcessor implements ProjectProcessor {

    private GithubHelper githubHelper
    private TravisHelper travisHelper

    @Override
    ArrayList<Project> processProjects(ArrayList<Project> projects) {
        ArrayList<Project> result = projects;
        if (arguments.providedAccessKey()) {
            println "Running ForkAndEnableTravisProcessor"
            githubHelper = new GithubHelper(arguments.getAccessKey())
            travisHelper = new TravisHelper(arguments.getAccessKey())

            ArrayList<Project> projectsForks = new ArrayList<Project>()
            for (project in projects) {
                if (project.isRemote()) {
                    def forkedProject = githubHelper.fork(project)
                    String path = "${githubHelper.URL}/${forkedProject.full_name}"
                    Project projectFork = new Project(project.getName(), path)

                    try {
                        keepTryingToEnableTravisProject(projectFork, 10);
                        
                        projectsForks.add(projectFork)
                    } catch (TravisHelperException e) {
                        println "Couldn't enable travis for project: ${projectFork}, skipping it"
                    }
                } else {
                    println "${project.getName()} is not remote and cant be forked"
                }
            }

            result = projectsForks
        } else {
            println "Skiping ForkAndEnableTravisProcessor: access key not provided"
        }
        return result
    }

    private void keepTryingToEnableTravisProject (Project project, int maxNumberOfTries) {
        /* This is a workaround to a limitation in travis api
        * You have to wait and sync multiple times to a project         
        * become available 
        */ 
        try {
            configureTravisProject(project)
        } catch (TravisHelperException e) {
            travisHelper.syncAndWait()
            if (maxNumberOfTries > 0) {
                keepTryingToEnableTravisProject(project, maxNumberOfTries - 1)
            } else {
                throw new TravisHelperException("Number of sync tries exceeded")
            }
        }
    } 

    private void configureTravisProject (Project project) {
        String[] ownerAndName = project.getOwnerAndName()
        Map travisProject = travisHelper.getProject(ownerAndName[0], ownerAndName[1])
        travisHelper.enableTravis(travisProject.id)
        travisHelper.addEnvironmentVariable(travisProject.id, "GITHUB_TOKEN", arguments.getAccessKey())
    }
}
 