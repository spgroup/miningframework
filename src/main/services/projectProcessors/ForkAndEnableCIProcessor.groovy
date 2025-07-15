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
 * @provides: forks the passed github projects and enables them on tbe CI platform with the access key as an environment
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
                    int maxAttempts = 3
                    int attempt = 0
                    boolean success = false

                    while (attempt < maxAttempts && !success) {
                        try {
                            def forkedProject = githubHelper.fork(project)
                            String forkPath = "${githubHelper.URL}/${forkedProject.full_name}"
                            Project projectFork = new Project(project.getName(), forkPath)

                            ciPlatform.enableProject(projectFork)

                            projectsForks.add(projectFork)
                            success = true
                        } catch (Exception e) {
                            attempt++
                            if (attempt < maxAttempts) {
                                println "Attempt ${attempt} failed for project: ${project.getName()}, retrying in 10 seconds..."
                                sleep(10_000)
                            } else {
                                println "Couldn't enable ci for project: ${project.getName()} after ${maxAttempts} attempts, skipping it"
                            }
                        }
                    }
                } else {
                    println "${project.getName()} is not remote and can't be forked"
                }
            }

            result = projectsForks
        } else {
            println "Skiping ForkAndEnableCIProcessor: access key not provided"
        }
        return result
    }

}
 