package services.util.ci

import project.Project

interface CIPlatform {
    abstract void enableProject(Project project)
    abstract File getConfigurationFile(Project project)
    abstract String generateConfiguration(Project project, String identifier, String buildCommand)
}