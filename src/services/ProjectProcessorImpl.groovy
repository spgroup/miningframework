package services

import main.interfaces.ProjectProcessor
import java.util.regex.Pattern
import java.util.regex.Matcher

import static main.app.MiningFramework.arguments
import main.util.*
import main.project.*


class ProjectProcessorImpl implements ProjectProcessor {

    public ArrayList<Project> processProjects(ArrayList<Project> projects) {
        println projects
        return projects
    }

}