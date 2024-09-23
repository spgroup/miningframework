package services.dataCollectors.GenericMerge.model


import services.dataCollectors.GenericMerge.FileSyntacticDiff

import java.nio.file.Path

class MergeScenarioExecutionSummary {
    public final String tool
    public final Path scenario
    public final Path output
    public final MergeScenarioResult result
    public final long time
    public final boolean equivalentToOracle

    MergeScenarioExecutionSummary(Path scenario, Path output, MergeScenarioResult result, long time, String tool) {
        this.scenario = scenario
        this.output = output
        this.result = result
        this.time = time
        this.tool = tool
        this.equivalentToOracle = FileSyntacticDiff.areFilesSyntacticallyEquivalent(scenario.resolve("merge.java"), output)
    }

    String getTool() {
        return tool
    }

    boolean isEquivalentToOracle() {
        return equivalentToOracle
    }
}
