package services.projectProcessors

import com.google.inject.Inject
import interfaces.ProjectProcessor
import project.*
import services.util.ci.CIPlatform
import util.*

import static app.MiningFramework.arguments

/**
 * @requires: that the projects passed are on github, that the access key was passed and that
 * the ci platform is enabled for the github account
 * @provides: forks the passed github projects and enables them on travis with the access key as a environment
 * variable, returns the projects with the path updated to the fork url
 * */
class ForkAndEnableCIProcessor implements ProjectProcessor {

    private GithubHelper githubHelper
    private CIPlatform ciPlatform

    @Inject
    ForkAndEnableCIProcessor(CIPlatform ciPlatform) {
        this.ciPlatform = ciPlatform
    }

    @Override
    ArrayList<Project> processProjects(ArrayList<Project> projects) {
        ArrayList<Project> result = projects;
        if (arguments.providedAccessKey()) {
            println "Running ForkAndEnableCIProcessor"

            githubHelper = new GithubHelper(arguments.getAccessKey())

            ArrayList<Project> projectsForks = new ArrayList<Project>()
            for (project in projects) {
                if (project.isRemote()) {
                    def forkedProject = githubHelper.fork(project)
                    String forkPath = "${githubHelper.URL}/${forkedProject.full_name}"
                    Project projectFork = new Project(project.getName(), forkPath)

                    try {
                        ciPlatform.enableProject(projectFork)

                        projectsForks.add(projectFork)
                    } catch (Exception e) {
                        println "Couldn't enable ci for project: ${projectFork.getName()}, skipping it"
                    }
                } else {
                    println "${project.getName()} is not remote and cant be forked"
                }
            }

            result = projectsForks
        } else {
            println "Skiping ForkAndEnableCIProcessor: access key not provided"
        }
        return result
    }

}
 