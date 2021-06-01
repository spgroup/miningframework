package services.projectProcessors

import interfaces.ProjectProcessor
import project.Project

class DummyProjectProcessor implements ProjectProcessor {
    @Override
    ArrayList<Project> processProjects(ArrayList<Project> projects) {
        return projects
    }
}
