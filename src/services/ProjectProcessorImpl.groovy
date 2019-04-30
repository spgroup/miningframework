package services

import main.interfaces.ProjectProcessor
import java.util.regex.Pattern
import java.util.regex.Matcher

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*


class ProjectProcessorImpl implements ProjectProcessor {

    public ArrayList<Project> processProjects(ArrayList<Project> projects) {
        GithubHelper githubHelper = new GithubHelper(arguments.getAccessKey())

        ArrayList<Project> projectForks = new ArrayList<Project>()

        for (project in projects) {
            if (project.isRemote()) {
                def forkedProject = githubHelper.fork(project)
                String path = "${githubHelper.URL}/${forkedProject.full_name}"
                Project projectFork = new Project(project.getName(), path)
                
                projectForks.add(projectFork)
            } else {
                println "${project.getName()} is not remote and cant be forked"
            }
        }

        return projectForks
    }
}
 