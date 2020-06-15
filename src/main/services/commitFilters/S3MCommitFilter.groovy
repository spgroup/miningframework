package services.commitFilters

import interfaces.CommitFilter
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import util.ProcessRunner

class S3MCommitFilter implements CommitFilter {

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
