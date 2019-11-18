package services.S3MHandlersAnalysis.implementations

import main.project.MergeCommit
import main.project.Project
import main.util.ProcessRunner
import services.S3MHandlersAnalysis.datacollection.MergeScenarioCollector

class CommitFilter implements main.interfaces.CommitFilter {

    @Override
    boolean applyFilter(Project project, MergeCommit mergeCommit) {
        return thereIsAtLeastOneMergeScenario(project, mergeCommit)
    }

    private static boolean thereIsAtLeastOneMergeScenario(Project project, MergeCommit mergeCommit) {
        Process gitDiffTree = ProcessRunner.runProcess(project.getPath(), "git", "diff-tree", "--no-commit-id", "--name-status", "-r", mergeCommit.getSHA(), mergeCommit.getAncestorSHA())
        List<String> modifiedFiles = gitDiffTree.getInputStream().readLines()

        return modifiedFiles.stream()
                .filter(MergeScenarioCollector::isModifiedFile)
                .filter(MergeScenarioCollector::isJavaFile)
                .count() > 0
    }
}
