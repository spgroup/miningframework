package services.util.ci

import project.Project

interface CIPlatform {
    void enableProject(Project project)
    File getConfigurationFile(Project project)
    String generateConfiguration(Project project, String identifier, String buildCommand)
    void waitForBuilds(Project project)
}