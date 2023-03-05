import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.process.OutputAnalyzer;

public class TestCheckedEnsureLocalCapacity {

    static {
        System.loadLibrary("TestCheckedEnsureLocalCapacity");
    }

    private static native void ensureCapacity(Object o, int capacity, int copies);

    private static int[][] testArgs = { { 60, 45 }, { 1, 45 } };

    private static final String EXCEED_WARNING = "^WARNING: JNI local refs: \\d++, exceeds capacity:";

    private static final String WARNING = "^WARNING: ";

    public static void main(String[] args) throws Throwable {
        if (args.length == 2) {
            ensureCapacity(new Object(), Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            return;
        }
        ProcessTools.executeTestJvm("-Xcheck:jni", "TestCheckedEnsureLocalCapacity", Integer.toString(testArgs[0][0]), Integer.toString(testArgs[0][1])).shouldHaveExitValue(0).stdoutShouldNotMatch(EXCEED_WARNING).stdoutShouldNotMatch(WARNING).reportDiagnosticSummary();
        ProcessTools.executeTestJvm("-Xcheck:jni", "TestCheckedEnsureLocalCapacity", Integer.toString(testArgs[1][0]), Integer.toString(testArgs[1][1])).shouldHaveExitValue(0).stdoutShouldMatch(EXCEED_WARNING).reportDiagnosticSummary();
    }
}