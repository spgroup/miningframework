package services.outputProcessors.genericMerge

import interfaces.OutputProcessor
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import project.MergeCommit
import project.Project
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.dataCollectors.GenericMerge.model.MergeScenarioResult
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import services.util.Utils

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

class TriggerBuildAndTestsOutputProcessor implements OutputProcessor {
    private static Logger LOG = LogManager.getLogger(TriggerBuildAndTestsOutputProcessor.class)

    @Override
    void processOutput() {
        return
        Files.readAllLines(Paths.get(GenericMergeConfig.GENERIC_MERGE_REPORT_COMMITS_FILE_NAME))
                .stream()
                .filter(line -> !(line.trim().isEmpty()))
                .map(line -> MergeScenarioLine.fromLine(line.split(",")))
                .filter(scenario -> scenario.result == MergeScenarioResult.SUCCESS_WITHOUT_CONFLICTS && !scenario.allFilesMatch)
                .forEach(scenario -> triggerBuildForScenario(scenario))
    }

    private static void triggerBuildForScenario(MergeScenarioLine scenario) {
        LOG.debug("Starting preparation for triggering build on scenario in ${scenario.project.name} on commit ${scenario.mergeCommit.getSHA()}")

        // Trigger a build for left
        BuildRequester.requestBuildForCommitSha(scenario.project, scenario.mergeCommit.getLeftSHA())
        // Trigger a build for right
        BuildRequester.requestBuildForCommitSha(scenario.project, scenario.mergeCommit.getRightSHA())
        // Trigger a build for merge
        def mergeFiles = MergeScenarioCollector
                .collectMergeScenarios(scenario.project, scenario.mergeCommit)
                .stream()
                .filter(NonFastForwardMergeScenarioFilter::isNonFastForwardMergeScenario)
                .collect(Collectors.toList())
        BuildRequester.requestBuildWithRevision(scenario.project, scenario.mergeCommit, mergeFiles, scenario.tool)

        LOG.debug("Finished preparation for triggering build on scenario in ${scenario.project.name} on commit ${scenario.mergeCommit.getSHA()}")

        LOG.debug("Pushing analysis for ${scenario.project.name} and commit ${scenario.mergeCommit.getSHA()} to GitHub")
        def exitCode = Utils.runGitCommand(Paths.get(scenario.project.getPath()), 'push', '--set-upstream', 'origin', '--all')
        if (exitCode == 0) {
            LOG.debug("Successuflly pushed analysis for ${scenario.project.name} and commit ${scenario.mergeCommit.getSHA()} to GitHub")
        } else {
            LOG.warn("An error happened while pushing analysis for ${scenario.project.name} and commit ${scenario.mergeCommit.getSHA()} to GitHub")
        }
    }

    private static class MergeScenarioLine {
        final Project project
        final MergeCommit mergeCommit
        final MergeScenarioResult result
        final String tool
        final boolean allFilesMatch

        MergeScenarioLine(Project project, MergeCommit mergeCommit, MergeScenarioResult result, String tool, boolean allFilesMatch) {
            this.project = project
            this.mergeCommit = mergeCommit
            this.result = result
            this.tool = tool
            this.allFilesMatch = allFilesMatch
        }

        static fromLine(String[] line) {
            def project = new Project(line[0], line[1])
            def mergeCommit = new MergeCommit(line[5], new String[]{line[3], line[4]}, line[2])
            def result = MergeScenarioResult.valueOf(line[7])
            def tool = line[6]
            def allFilesMatch = line[8] == "true"
            return new MergeScenarioLine(project, mergeCommit, result, tool, allFilesMatch)
        }
    }
}
