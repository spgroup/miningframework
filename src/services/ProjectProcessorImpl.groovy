package services

import main.interfaces.ProjectProcessor
import java.util.regex.Pattern
import java.util.regex.Matcher

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*
import main.exception.*

class ProjectProcessorImpl implements ProjectProcessor {

    private GithubHelper githubHelper
    private TravisHelper travisHelper

    public ArrayList<Project> processProjects(ArrayList<Project> projects) {
        if (arguments.getAccessKey().length() > 0) {
            githubHelper = new GithubHelper(arguments.getAccessKey())
            travisHelper = new TravisHelper(arguments.getAccessKey())
            println "Processing projects"

            ArrayList<Project> projectsForks = new ArrayList<Project>()
            for (project in projects) {
                if (project.isRemote()) {
                    def forkedProject = githubHelper.fork(project)
                    String path = "${githubHelper.URL}/${forkedProject.full_name}"
                    Project projectFork = new Project(project.getName(), path)

                    projectsForks.add(projectFork)
                } else {
                    println "${project.getName()} is not remote and cant be forked"
                }
            }

            keepTryingToEnableTravisProjects(projectsForks)

            return projectsForks
        } 
        return projects
    }

    private void keepTryingToEnableTravisProjects (ArrayList<Project> projects) {
        try {
            for (project in projects) {
                enableTravisProject(project)
            }
        } catch (TravisHelperException e) {
            travisHelper.syncAndWait()        
            keepTryingToEnableTravisProjects(projects)
        }
    } 

    private void enableTravisProject (Project project) {
        String[] ownerAndName = project.getOwnerAndName()
        Map travisProject = travisHelper.getProject(ownerAndName[0], ownerAndName[1])
        travisHelper.enableTravis(travisProject.id)
    }
}
 