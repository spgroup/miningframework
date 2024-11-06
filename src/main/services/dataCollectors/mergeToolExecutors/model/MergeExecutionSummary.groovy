package services.dataCollectors.mergeToolExecutors.model


import java.nio.file.Path

class MergeExecutionSummary {
    public final Path file
    public final Path output
    public final MergeExecutionResult result
    public final long time

    MergeExecutionSummary(Path file, Path output, MergeExecutionResult result, long time) {
        this.file = file
        this.output = output
        this.result = result
        this.time = time
    }
}
