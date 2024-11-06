package services.dataCollectors.mergeToolExecutors

import services.dataCollectors.mergeToolExecutors.model.MergeExecutionResult
import util.ProcessRunner

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class GitMergeFileMergeToolDataCollector extends BaseMergeToolExecutorDataCollector {
    @Override
    protected MergeExecutionResult executeTool(Path file, Path outputFile) {
        // We copy the left file, because git merge-file runs in place, replacing the contents of left file
        Files.copy(file.resolve("left.java"), outputFile, StandardCopyOption.REPLACE_EXISTING)

        def processBuilder = ProcessRunner.buildProcess(System.getProperty("user.dir"),
                "git",
                "merge-file",
                outputFile.toString(),
                file.resolve("base.java").toString(),
                file.resolve("right.java").toString())
        def exitCode = ProcessRunner.startProcess(processBuilder).waitFor()

        return exitCode == 0 ? MergeExecutionResult.SUCCESS_WITHOUT_CONFLICTS : MergeExecutionResult.SUCCESS_WITH_CONFLICTS
    }

    @Override
    String getToolName() {
        return 'git_merge_file'
    }
}
