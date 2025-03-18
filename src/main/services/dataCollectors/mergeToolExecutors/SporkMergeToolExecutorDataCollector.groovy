package services.dataCollectors.mergeToolExecutors


import java.nio.file.Path

class SporkMergeToolExecutorDataCollector extends BaseMergeToolExecutorDataCollector {
    private static final String SPORK_JAR_PATH = "${System.getProperty("user.dir")}/dependencies/spork.jar"

    @Override
    protected List<String> getArgumentsForTool(Path file, Path outputFile) {
        def list = new ArrayList<String>()
        list.add("ls")
        list.add("-jar")
        list.add(SPORK_JAR_PATH)
        list.add("-e")
        list.add(file.resolve("left.java").toString())
        list.add(file.resolve("base.java").toString())
        list.add(file.resolve("right.java").toString())
        list.add("--output=${outputFile.toAbsolutePath().toString()}".toString())
        return list
    }

    @Override
    String getToolName() {
        return 'spork'
    }
}
