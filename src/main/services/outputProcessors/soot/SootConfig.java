package services.outputProcessors.soot;

import java.util.HashMap;
import java.util.Map;

public class SootConfig {
    private final String inputFilePath;
    private final String classPath;
    private final String mode;
    private final Map<String, String> optionalParams;

    public SootConfig(String inputFilePath, String classPath, String mode) {
        this.inputFilePath = inputFilePath;
        this.classPath = classPath;
        this.mode = mode;
        this.optionalParams = new HashMap<>();
    }

    public String getInputFilePath() { return inputFilePath; }
    public String getClassPath() { return classPath; }
    public String getMode() { return mode; }
    public Map<String, String> getOptionalParams() { return optionalParams; }

    public void addOption(String flag, Object value) {
        if (value != null) {
            optionalParams.put(flag, value.toString());
        }
    }
}
