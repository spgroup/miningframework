package sun.misc;

import java.io.PrintStream;

public class Version {

    private static final String launcher_name = "@@launcher_name@@";

    private static final String java_version = "@@java_version@@";

    private static final String java_runtime_name = "@@java_runtime_name@@";

    private static final String java_runtime_version = "@@java_runtime_version@@";

    static {
        init();
    }

    public static void init() {
        System.setProperty("java.version", java_version);
        System.setProperty("java.runtime.version", java_runtime_version);
        System.setProperty("java.runtime.name", java_runtime_name);
    }

    private static boolean versionsInitialized = false;

    private static int jvm_major_version = 0;

    private static int jvm_minor_version = 0;

    private static int jvm_micro_version = 0;

    private static int jvm_update_version = 0;

    private static int jvm_build_number = 0;

    private static String jvm_special_version = null;

    private static int jdk_major_version = 0;

    private static int jdk_minor_version = 0;

    private static int jdk_micro_version = 0;

    private static int jdk_update_version = 0;

    private static int jdk_build_number = 0;

    private static String jdk_special_version = null;

    public static void print() {
        print(System.err);
    }

    public static void println() {
        print(System.err);
        System.err.println();
    }

    public static void print(PrintStream ps) {
        ps.println(launcher_name + " version \"" + java_version + "\"");
        ps.println(java_runtime_name + " (build " + java_runtime_version + ")");
        String java_vm_name = System.getProperty("java.vm.name");
        String java_vm_version = System.getProperty("java.vm.version");
        String java_vm_info = System.getProperty("java.vm.info");
        ps.println(java_vm_name + " (build " + java_vm_version + ", " + java_vm_info + ")");
    }

    public static synchronized int jvmMajorVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jvm_major_version;
    }

    public static synchronized int jvmMinorVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jvm_minor_version;
    }

    public static synchronized int jvmMicroVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jvm_micro_version;
    }

    public static synchronized int jvmUpdateVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jvm_update_version;
    }

    public static synchronized String jvmSpecialVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        if (jvm_special_version == null) {
            jvm_special_version = getJvmSpecialVersion();
        }
        return jvm_special_version;
    }

    public static native String getJvmSpecialVersion();

    public static synchronized int jvmBuildNumber() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jvm_build_number;
    }

    public static synchronized int jdkMajorVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jdk_major_version;
    }

    public static synchronized int jdkMinorVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jdk_minor_version;
    }

    public static synchronized int jdkMicroVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jdk_micro_version;
    }

    public static synchronized int jdkUpdateVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jdk_update_version;
    }

    public static synchronized String jdkSpecialVersion() {
        if (!versionsInitialized) {
            initVersions();
        }
        if (jdk_special_version == null) {
            jdk_special_version = getJdkSpecialVersion();
        }
        return jdk_special_version;
    }

    public static native String getJdkSpecialVersion();

    public static synchronized int jdkBuildNumber() {
        if (!versionsInitialized) {
            initVersions();
        }
        return jdk_build_number;
    }

    private static boolean jvmVersionInfoAvailable;

    private static synchronized void initVersions() {
        if (versionsInitialized) {
            return;
        }
        jvmVersionInfoAvailable = getJvmVersionInfo();
        if (!jvmVersionInfoAvailable) {
            CharSequence cs = System.getProperty("java.vm.version");
            if (cs.length() >= 5 && Character.isDigit(cs.charAt(0)) && cs.charAt(1) == '.' && Character.isDigit(cs.charAt(2)) && cs.charAt(3) == '.' && Character.isDigit(cs.charAt(4))) {
                jvm_major_version = Character.digit(cs.charAt(0), 10);
                jvm_minor_version = Character.digit(cs.charAt(2), 10);
                jvm_micro_version = Character.digit(cs.charAt(4), 10);
                cs = cs.subSequence(5, cs.length());
                if (cs.charAt(0) == '_' && cs.length() >= 3 && Character.isDigit(cs.charAt(1)) && Character.isDigit(cs.charAt(2))) {
                    int nextChar = 3;
                    try {
                        String uu = cs.subSequence(1, 3).toString();
                        jvm_update_version = Integer.valueOf(uu).intValue();
                        if (cs.length() >= 4) {
                            char c = cs.charAt(3);
                            if (c >= 'a' && c <= 'z') {
                                jvm_special_version = Character.toString(c);
                                nextChar++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return;
                    }
                    cs = cs.subSequence(nextChar, cs.length());
                }
                if (cs.charAt(0) == '-') {
                    cs = cs.subSequence(1, cs.length());
                    String[] res = cs.toString().split("-");
                    for (String s : res) {
                        if (s.charAt(0) == 'b' && s.length() == 3 && Character.isDigit(s.charAt(1)) && Character.isDigit(s.charAt(2))) {
                            jvm_build_number = Integer.valueOf(s.substring(1, 3)).intValue();
                            break;
                        }
                    }
                }
            }
        }
        getJdkVersionInfo();
        versionsInitialized = true;
    }

    private static native boolean getJvmVersionInfo();

    private static native void getJdkVersionInfo();
}
