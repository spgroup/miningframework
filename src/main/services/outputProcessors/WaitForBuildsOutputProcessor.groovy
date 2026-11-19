package services.outputProcessors

import arguments.InputParser
import com.google.inject.Inject
import interfaces.OutputProcessor
import project.Project
import services.util.ci.CIPlatform
import util.GithubHelper

import static app.MiningFramework.arguments

class WaitForBuildsOutputProcessor implements OutputProcessor {

    private CIPlatform ciPlatform

    @Inject
    WaitForBuildsOutputProcessor(CIPlatform ciPlatform) {
        this.ciPlatform = ciPlatform
    }

    @Override
    void processOutput() {
        if (!arguments.providedAccessKey()) {
            return // no builds were requested
        }
        GithubHelper githubHelper = new GithubHelper(arguments.getAccessKey())
        List<Project> projects = InputParser.getProjectList(arguments.getInputPath())

        for (project in projects) {
            if (project.isRemote()) {
                println "Waiting for builds in project: ${project.getName()}"
                // the builds run on the fork, made by ForkAndEnableCIProcessor
                this.ciPlatform.waitForBuilds(githubHelper.getFork(project))
            }
        }
    }
}
