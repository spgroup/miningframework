package services.dataCollectors.GenericMerge

class GenericMergeConfig {
    public static final BASE_EXPERIMENT_PATH = System.getProperty("miningframework.generic_merge.base_experiment_path", "/usr/src/app")

    public static final MERGE_TOOL_EXECUTORS_TO_USE = System.getProperty("miningframework.generic_merge.merge_tool_executors_to_use", "generic_merge,jdime").split(",")

    public static final BUILD_REQUESTER_REPORT_PATH = "${BASE_EXPERIMENT_PATH}/output/reports/generic-merge-execution-build-requests.csv"

    public static final GENERIC_MERGE_REPORT_PATH = "${BASE_EXPERIMENT_PATH}/output/reports"
    public static final GENERIC_MERGE_REPORT_FILE_NAME = "${GENERIC_MERGE_REPORT_PATH}/generic-merge-execution.csv"
    public static final GENERIC_MERGE_REPORT_COMMITS_FILE_NAME = "${GENERIC_MERGE_REPORT_PATH}/generic-merge-execution-commits.csv"

    public static final String GENERIC_MERGE_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/generic-merge"
    public static final String JDIME_BINARY_PATH = "${BASE_EXPERIMENT_PATH}/tools/jdime/install/JDime/bin"

    public static final int NUMBER_OF_EXECUTIONS = 5
}
