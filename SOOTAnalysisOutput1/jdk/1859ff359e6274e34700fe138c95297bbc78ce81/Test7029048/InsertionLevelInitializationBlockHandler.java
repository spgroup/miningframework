import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test7029048 {

    static int passes = 0;

    static int errors = 0;

    private static final String LIBJVM = ExecutionEnvironment.LIBJVM;

    private static final String LD_LIBRARY_PATH = ExecutionEnvironment.LD_LIBRARY_PATH;

    private static final String LD_LIBRARY_PATH_32 = ExecutionEnvironment.LD_LIBRARY_PATH_32;

    private static final String LD_LIBRARY_PATH_64 = ExecutionEnvironment.LD_LIBRARY_PATH_64;

    private static final File libDir = new File(System.getProperty("sun.boot.library.path"));

    private static final File srcServerDir = new File(libDir, "server");

    private static final File srcLibjvmSo = new File(srcServerDir, LIBJVM);

    private static final File dstLibDir = new File("lib");

    private static final File dstLibArchDir = new File(dstLibDir, TestHelper.getJreArch());

    private static final File dstServerDir = new File(dstLibArchDir, "server");

    private static final File dstServerLibjvm = new File(dstServerDir, LIBJVM);

    private static final File dstClientDir = new File(dstLibArchDir, "client");

    private static final File dstClientLibjvm = new File(dstClientDir, LIBJVM);

    private static final File dstOtherArchDir;

    private static final File dstOtherServerDir;

    private static final File dstOtherServerLibjvm;

    private static final Map<String, String> env = new HashMap<>();

    static {
        if (TestHelper.isDualMode) {
            dstOtherArchDir = new File(dstLibDir, TestHelper.getComplementaryJreArch());
            dstOtherServerDir = new File(dstOtherArchDir, "server");
            dstOtherServerLibjvm = new File(dstOtherServerDir, LIBJVM);
        } else {
            dstOtherArchDir = null;
            dstOtherServerDir = null;
            dstOtherServerLibjvm = null;
        }
    }

    static String getValue(String name, List<String> in) {
        for (String x : in) {
            String[] s = x.split("=");
            if (name.equals(s[0].trim())) {
                return s[1].trim();
            }
        }
        return null;
    }

    static void run(boolean want32, String dflag, Map<String, String> env, int nLLPComponents, String caseID) {
        final boolean want64 = want32 == false;
        env.put(ExecutionEnvironment.JLDEBUG_KEY, "true");
        List<String> cmdsList = new ArrayList<>();
        if (want64 && TestHelper.isDualMode) {
            cmdsList.add(TestHelper.java64Cmd);
        } else {
            cmdsList.add(TestHelper.javaCmd);
        }
        if (dflag != null) {
            cmdsList.add(dflag);
        } else {
            cmdsList.add(want32 ? "-d32" : "-d64");
        }
        cmdsList.add("-server");
        cmdsList.add("-jar");
        cmdsList.add(ExecutionEnvironment.testJarFile.getAbsolutePath());
        String[] cmds = new String[cmdsList.size()];
        TestHelper.TestResult tr = TestHelper.doExec(env, cmdsList.toArray(cmds));
        analyze(tr, nLLPComponents, caseID);
    }

    static void run(Map<String, String> env, int nLLPComponents, String caseID) throws IOException {
        boolean want32 = TestHelper.is32Bit;
        run(want32, null, env, nLLPComponents, caseID);
    }

    static void analyze(TestHelper.TestResult tr, int nLLPComponents, String caseID) {
        String envValue = getValue(LD_LIBRARY_PATH, tr.testOutput);
        if (envValue == null) {
            System.out.println(tr);
            throw new RuntimeException("NPE, likely a program crash ??");
        }
        String[] values = envValue.split(File.pathSeparator);
        if (values.length == nLLPComponents) {
            System.out.println(caseID + " :OK");
            passes++;
        } else {
            System.out.println("FAIL: test7029048, " + caseID);
            System.out.println(" expected " + nLLPComponents + " but got " + values.length);
            System.out.println(envValue);
            System.out.println(tr);
            errors++;
        }
    }

    private static enum LLP_VAR {

        LLP_SET_NON_EXISTENT_PATH(0), LLP_SET_EMPTY_PATH(0), LLP_SET_WITH_JVM(3);

        private final int value;

        LLP_VAR(int i) {
            this.value = i;
        }
    }

    static void test7029048() throws IOException {
        String desc = null;
        for (LLP_VAR v : LLP_VAR.values()) {
            switch(v) {
                case LLP_SET_WITH_JVM:
                    TestHelper.copyFile(srcLibjvmSo, dstServerLibjvm);
                    TestHelper.copyFile(srcLibjvmSo, dstClientLibjvm);
                    if (TestHelper.isDualMode) {
                        TestHelper.copyFile(srcLibjvmSo, dstOtherServerLibjvm);
                    }
                    desc = "LD_LIBRARY_PATH should be set";
                    break;
                case LLP_SET_EMPTY_PATH:
                    if (!dstClientDir.exists()) {
                        Files.createDirectories(dstClientDir.toPath());
                    } else {
                        Files.deleteIfExists(dstClientLibjvm.toPath());
                    }
                    if (!dstServerDir.exists()) {
                        Files.createDirectories(dstServerDir.toPath());
                    } else {
                        Files.deleteIfExists(dstServerLibjvm.toPath());
                    }
                    if (TestHelper.isDualMode) {
                        if (!dstOtherServerDir.exists()) {
                            Files.createDirectories(dstOtherServerDir.toPath());
                        } else {
                            Files.deleteIfExists(dstOtherServerLibjvm.toPath());
                        }
                    }
                    desc = "LD_LIBRARY_PATH should not be set";
                    break;
                case LLP_SET_NON_EXISTENT_PATH:
                    if (dstLibDir.exists()) {
                        TestHelper.recursiveDelete(dstLibDir);
                    }
                    desc = "LD_LIBRARY_PATH should not be set";
                    break;
                default:
                    throw new RuntimeException("unknown case");
            }
            env.clear();
            env.put(LD_LIBRARY_PATH, dstServerDir.getAbsolutePath());
            run(env, v.value + 1, "Case 1: " + desc);
            env.clear();
            env.put(LD_LIBRARY_PATH, dstClientDir.getAbsolutePath());
            run(env, v.value + 1, "Case 2: " + desc);
            if (!TestHelper.isDualMode) {
                continue;
            }
            final File dst32ServerDir = TestHelper.is32Bit ? dstServerDir : dstOtherServerDir;
            final File dst64ServerDir = TestHelper.is64Bit ? dstServerDir : dstOtherServerDir;
            env.clear();
            env.put(LD_LIBRARY_PATH_32, dst32ServerDir.getAbsolutePath());
            env.put(LD_LIBRARY_PATH_64, dst64ServerDir.getAbsolutePath());
            run(TestHelper.is32Bit, null, env, v.value + 1, "Case 3: " + desc);
            if (TestHelper.dualModePresent()) {
                run(true, "-d64", env, v.value + 1, "Case 4A: " + desc);
                run(false, "-d32", env, v.value + 1, "Case 4B: " + desc);
            }
        }
        return;
    }

    public static void main(String... args) throws Exception {
        if (TestHelper.isWindows) {
            System.out.println("Warning: noop on windows");
            return;
        }
        ExecutionEnvironment.createTestJar();
        test7029048();
        if (errors > 0) {
            throw new Exception("Test7029048: FAIL: with " + errors + " errors and passes " + passes);
        } else if (TestHelper.dualModePresent() && passes < 15) {
            throw new Exception("Test7029048: FAIL: " + "all tests did not run, expected " + 15 + " got " + passes);
        } else if (TestHelper.isSolaris && passes < 9) {
            throw new Exception("Test7029048: FAIL: " + "all tests did not run, expected " + 9 + " got " + passes);
        } else if (TestHelper.isLinux && passes < 6) {
            throw new Exception("Test7029048: FAIL: " + "all tests did not run, expected " + 6 + " got " + passes);
        } else {
            System.out.println("Test7029048: PASS " + passes);
        }
    }
}