package util

import interfaces.ProjectProcessor

import project.*

class EmptyProjectProcessor implements ProjectProcessor {

    public ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projects
    }
}
 