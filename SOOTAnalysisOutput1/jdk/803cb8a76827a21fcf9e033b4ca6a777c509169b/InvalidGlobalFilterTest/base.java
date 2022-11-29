import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import java.io.File;
import java.io.ObjectInputFilter;

public class InvalidGlobalFilterTest {

    private static final String serialPropName = "jdk.serialFilter";

    public static void main(final String[] args) throws Exception {
        final String[] invalidPatterns = { ".*", ".**", "!", "/java.util.Hashtable", "java.base/", "/" };
        for (final String invalidPattern : invalidPatterns) {
            final ProcessBuilder processBuilder = ProcessTools.createJavaProcessBuilder("-D" + serialPropName + "=" + invalidPattern, "-Djava.util.logging.config.file=" + System.getProperty("test.src") + File.separator + "logging.properties", ObjectInputFilterConfigLoader.class.getName());
            final OutputAnalyzer outputAnalyzer = ProcessTools.executeProcess(processBuilder);
            try {
                outputAnalyzer.shouldNotHaveExitValue(0);
                outputAnalyzer.stderrShouldContain("java.lang.ExceptionInInitializerError");
            } finally {
                System.err.println("Diagnostics from process " + outputAnalyzer.pid() + ":");
                outputAnalyzer.reportDiagnosticSummary();
            }
        }
    }

    private static final class ObjectInputFilterConfigLoader {

        public static void main(final String[] args) throws Exception {
            System.out.println("JVM was launched with " + serialPropName + " system property set to " + System.getProperty(serialPropName));
            ObjectInputFilter.Config.getSerialFilter();
        }
    }
}
