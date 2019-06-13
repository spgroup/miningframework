package test.util

import main.interfaces.ProjectProcessor

import main.project.*

class EmptyProjectProcessor implements ProjectProcessor {

    public ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projects
    }
}
 