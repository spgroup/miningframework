package services.dataCollectors.GenericMerge

import project.MergeCommit
import project.Project
import services.util.Utils
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class BuildRequester {
    static requestBuildWithRevision(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String mergeTool) {
        String toReplaceFile = "merge.${mergeTool}.java"

        String branchName = "${mergeCommit.getSHA().take(7)}-${mergeTool}"

        createBranchFromCommit(project, mergeCommit, branchName)
        replaceFilesInProject(project, mergeCommit, mergeScenarios, toReplaceFile)
        stageAndPushChanges(project, branchName, "Mining Framework Analysis")
    }

    private static void createBranchFromCommit(Project project, MergeCommit mergeCommit, String branchName) {
        Path projectPath = Paths.get(project.getPath())

        // Checkout to new branch
        Utils.runGitCommand(projectPath, 'checkout', '-b', branchName, mergeCommit.getSHA())
    }

    private static void replaceFilesInProject(Project project, MergeCommit mergeCommit, List<Path> mergeScenarios, String toReplaceFile) {
        mergeScenarios.stream()
                .forEach(mergeScenario -> {
                    try {
                        def process =  ProcessRunner.buildProcess("/usr/src/app",
                        "cp",
                                getSource(mergeScenario, toReplaceFile).toAbsolutePath().toString(),
                                getTarget(project, mergeCommit, mergeScenario).toAbsolutePath().toString()
                        )
                        println "Starting copy of file ${getSource(mergeScenario, toReplaceFile)} to ${getTarget(project, mergeCommit, mergeScenario)}"
                        process.start().waitFor()
                        println "Finished copy of file ${getSource(mergeScenario, toReplaceFile)} to ${getTarget(project, mergeCommit, mergeScenario)}"
                    } catch (e) {
                        println "Error while copying ${getSource(mergeScenario, toReplaceFile)} to ${getTarget(project, mergeCommit, mergeScenario)}"
                        println e.toString()
                    }
                })
    }

    private static Path getSource(Path mergeScenario, String toReplaceFile) {
        return mergeScenario.resolve(toReplaceFile)
    }

    private static Path getTarget(Project project, MergeCommit mergeCommit, Path mergeScenario) {
        Path projectPath = Paths.get(project.getPath())
        Path filePath = Utils.commitFilesPath(project, mergeCommit).relativize(mergeScenario)
        return projectPath.resolve(filePath)
    }

    private static void stageAndPushChanges(Project project, String branchName, String commitMessage) {
        Path projectPath = Paths.get(project.getPath())

        // Stage changes
        Utils.runGitCommand(projectPath, 'add', '.')

        // Commit changes
        Utils.runGitCommand(projectPath, 'commit', '-m', commitMessage)

        // Push changes
        Utils.runGitCommand(projectPath, 'push', '--set-upstream', 'origin', branchName, '--force-with-lease')
    }
}
