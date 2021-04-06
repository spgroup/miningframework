package services.outputProcessors

import arguments.InputParser
import com.google.inject.Inject
import interfaces.OutputProcessor
import project.Project
import services.util.ci.CIPlatform

import static app.MiningFramework.arguments

class WaitForBuildsOutputProcessor implements OutputProcessor {

    private CIPlatform ciPlatform

    @Inject
    WaitForBuildsOutputProcessor(CIPlatform ciPlatform) {
        this.ciPlatform = ciPlatform
    }

    @Override
    void processOutput() {
        List<Project> projects = InputParser.getProjectList(arguments.getInputPath())

        for (project in projects) {
            println "Waiting for builds in project: ${project.getName()}"
            this.ciPlatform.waitForBuilds(project)
        }
    }
}
