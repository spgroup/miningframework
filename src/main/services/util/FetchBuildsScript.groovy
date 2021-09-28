package services.util

enum FetchBuildsScript {
    SingleBuildPerScenario("fetch_jars"),
    MultipleBuildsPerScenario("fetch_multiple_jar_per_scenario")

    private final String SCRIPT_PATH

    private FetchBuildsScript(String SCRIPT_PATH) {
        this.SCRIPT_PATH = "./scripts/${SCRIPT_PATH}.py"
    }

    String getScriptPath() {
        return this.SCRIPT_PATH
    }
}