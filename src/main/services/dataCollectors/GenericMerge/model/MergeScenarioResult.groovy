package services.dataCollectors.GenericMerge.model

enum MergeScenarioResult {
    SUCCESS_WITHOUT_CONFLICTS("SUCCESS_WITHOUT_CONFLICTS"),
    SUCCESS_WITH_CONFLICTS("SUCCESS_WITH_CONFLICTS"),
    TOOL_ERROR("TOOL_ERROR");

    String value

    private MergeScenarioResult(String value) {
        this.value = value
    }

    static from(String value) {
        return new MergeScenarioResult(value)
    }
}
