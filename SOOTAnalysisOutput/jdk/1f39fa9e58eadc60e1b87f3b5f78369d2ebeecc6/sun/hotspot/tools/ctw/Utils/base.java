package sun.hotspot.tools.ctw;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.util.regex.Pattern;

public class Utils {

    public static final boolean TIERED_COMPILATION = Boolean.parseBoolean(getVMOption("TieredCompilation", "false"));

    public static final boolean BACKGROUND_COMPILATION = Boolean.parseBoolean(getVMOption("BackgroundCompilation", "false"));

    public static final int TIERED_STOP_AT_LEVEL;

    public static final Integer CI_COMPILER_COUNT = Integer.valueOf(getVMOption("CICompilerCount", "1"));

    public static final int INITIAL_COMP_LEVEL;

    public static final Pattern PATH_SEPARATOR = Pattern.compile(File.pathSeparator, Pattern.LITERAL);

    public static final int DEOPTIMIZE_ALL_CLASSES_RATE = Integer.getInteger("DeoptimizeAllClassesRate", -1);

    public static final long COMPILE_THE_WORLD_STOP_AT = Long.getLong("CompileTheWorldStopAt", Long.MAX_VALUE);

    public static final long COMPILE_THE_WORLD_START_AT = Long.getLong("CompileTheWorldStartAt", 1);

    public static final boolean COMPILE_THE_WORLD_PRELOAD_CLASSES;

    public static final boolean IS_VERBOSE = Boolean.getBoolean("sun.hotspot.tools.ctw.verbose");

    public static final String LOG_FILE = System.getProperty("sun.hotspot.tools.ctw.logfile");

    static {
        if (Utils.TIERED_COMPILATION) {
            INITIAL_COMP_LEVEL = 1;
        } else {
            String vmName = System.getProperty("java.vm.name");
            String vmInfo = System.getProperty("java.vm.info");
            boolean isEmulatedClient = (vmInfo != null) && vmInfo.contains("emulated-client");
            if (Utils.endsWithIgnoreCase(vmName, " Server VM") && !isEmulatedClient) {
                INITIAL_COMP_LEVEL = 4;
            } else if (Utils.endsWithIgnoreCase(vmName, " Client VM") || Utils.endsWithIgnoreCase(vmName, " Minimal VM") || isEmulatedClient) {
                INITIAL_COMP_LEVEL = 1;
            } else {
                throw new RuntimeException("Unknown VM: " + vmName);
            }
        }
        TIERED_STOP_AT_LEVEL = Integer.parseInt(getVMOption("TieredStopAtLevel", String.valueOf(INITIAL_COMP_LEVEL)));
    }

    static {
        String tmp = System.getProperty("CompileTheWorldPreloadClasses");
        if (tmp == null) {
            COMPILE_THE_WORLD_PRELOAD_CLASSES = true;
        } else {
            COMPILE_THE_WORLD_PRELOAD_CLASSES = Boolean.parseBoolean(tmp);
        }
    }

    public static final String CLASSFILE_EXT = ".class";

    private Utils() {
    }

    public static boolean endsWithIgnoreCase(String string, String suffix) {
        if (string == null || suffix == null) {
            return false;
        }
        int length = suffix.length();
        int toffset = string.length() - length;
        if (toffset < 0) {
            return false;
        }
        return string.regionMatches(true, toffset, suffix, 0, length);
    }

    public static String getVMOption(String name) {
        String result;
        HotSpotDiagnosticMXBean diagnostic = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        result = diagnostic.getVMOption(name).getValue();
        return result;
    }

    public static String getVMOption(String name, String defaultValue) {
        String result;
        try {
            result = getVMOption(name);
        } catch (NoClassDefFoundError e) {
            result = defaultValue;
        }
        return result == null ? defaultValue : result;
    }

    public static boolean isClassFile(String filename) {
        return endsWithIgnoreCase(filename, CLASSFILE_EXT) && (filename.indexOf('.') == (filename.length() - CLASSFILE_EXT.length()));
    }

    public static String fileNameToClassName(String filename) {
        assert isClassFile(filename);
        final char nameSeparator = '/';
        int nameStart = filename.charAt(0) == nameSeparator ? filename.indexOf(nameSeparator, 1) + 1 : 0;
        return filename.substring(nameStart, filename.length() - CLASSFILE_EXT.length()).replace(nameSeparator, '.');
    }
}
