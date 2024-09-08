package services.dataCollectors.GenericMerge.model

enum MergeScenarioResult {
    SUCCESS_WITHOUT_CONFLICTS("SUCCESS_WITHOUT_CONFLICTS"),
    SUCCESS_WITH_CONFLICTS("SUCCESS_WITH_CONFLICTS"),
    TOOL_ERROR("TOOL_ERROR");

    String value;

    MergeScenarioResult(String value) {
        this.value = value;
    }
}
