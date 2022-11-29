import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.lang.module.ModuleDescriptor;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jdk.internal.module.ModuleInfoWriter;
import jdk.testlibrary.ProcessTools;

public class ClassLoaderTest {

    private static final String SRC = System.getProperty("test.src");

    private static final Path TEST_CLASSES = Paths.get(System.getProperty("test.classes"));

    private static final Path ARTIFACT_DIR = Paths.get("jars");

    private static final Path VALID_POLICY = Paths.get(SRC, "valid.policy");

    private static final Path INVALID_POLICY = Paths.get(SRC, "malformed.policy");

    private static final Path CL_JAR = ARTIFACT_DIR.resolve("cl.jar");

    private static final Path MCL_JAR = ARTIFACT_DIR.resolve("mcl.jar");

    private static final Path C_JAR = ARTIFACT_DIR.resolve("c.jar");

    private static final Path MC_JAR = ARTIFACT_DIR.resolve("mc.jar");

    private static final Path AMC_JAR = ARTIFACT_DIR.resolve("amc.jar");

    private static final String MISSING_MODULE = "Module cl not found, required by mc";

    private static final String POLICY_ERROR = "java.security.policy: error parsing file";

    private static final String SYSTEM_CL_MSG = "jdk.internal.loader.ClassLoaders$AppClassLoader";

    private static final String CUSTOM_CL_MSG = "cl.TestClassLoader";

    private final boolean useSCL;

    private final String smMsg;

    private final String autoAddModArg;

    private final String addmodArg;

    private final String expectedStatus;

    private final String expectedMsg;

    private final List<String> commonArgs;

    public ClassLoaderTest(Path policy, boolean useSCL) {
        this.useSCL = useSCL;
        List<String> argList = new LinkedList<>();
        argList.add("-Duser.language=en");
        argList.add("-Duser.region=US");
        boolean malformedPolicy = false;
        if (policy == null) {
            smMsg = "Without SecurityManager";
        } else {
            malformedPolicy = policy.equals(INVALID_POLICY);
            argList.add("-Djava.security.manager");
            argList.add("-Djava.security.policy=" + policy.toFile().getAbsolutePath());
            smMsg = "With SecurityManager";
        }
        if (useSCL) {
            autoAddModArg = "";
            addmodArg = "";
        } else {
            argList.add("-Djava.system.class.loader=cl.TestClassLoader");
            autoAddModArg = "--add-modules=cl";
            addmodArg = "--add-modules=mcl";
        }
        if (malformedPolicy) {
            expectedStatus = "FAIL";
            expectedMsg = POLICY_ERROR;
        } else if (useSCL) {
            expectedStatus = "PASS";
            expectedMsg = SYSTEM_CL_MSG;
        } else {
            expectedStatus = "PASS";
            expectedMsg = CUSTOM_CL_MSG;
        }
        commonArgs = Collections.unmodifiableList(argList);
    }

    public static void main(String[] args) throws Exception {
        Path policy;
        if (args[0].equals("-noPolicy")) {
            policy = null;
        } else if (args[0].equals("-validPolicy")) {
            policy = VALID_POLICY;
        } else if (args[0].equals("-invalidPolicy")) {
            policy = INVALID_POLICY;
        } else {
            throw new RuntimeException("Unknown policy arg: " + args[0]);
        }
        boolean useSystemLoader = true;
        if (args.length > 1) {
            if (args[1].equals("-customSCL")) {
                useSystemLoader = false;
            } else {
                throw new RuntimeException("Unknown custom loader arg: " + args[1]);
            }
        }
        ClassLoaderTest test = new ClassLoaderTest(policy, useSystemLoader);
        setUp();
        test.processForPolicyFile();
    }

    private void processForPolicyFile() throws Exception {
        final String regLoaderLoc = CL_JAR.toFile().getAbsolutePath();
        final String modLoadrLoc = MCL_JAR.toFile().getAbsolutePath();
        final String regClientLoc = C_JAR.toFile().getAbsolutePath();
        final String modClientLoc = MC_JAR.toFile().getAbsolutePath();
        final String autoModCloc = AMC_JAR.toFile().getAbsolutePath();
        final String separator = File.pathSeparator;
        System.out.println("Case:- Modular Client and " + ((useSCL) ? "SystemClassLoader" : "Modular CustomClassLoader") + " " + smMsg);
        execute("--module-path", modClientLoc + separator + modLoadrLoc, "-m", "mc/c.TestClient");
        System.out.println("Case:- Modular Client and " + ((useSCL) ? "SystemClassLoader" : "Unknown modular CustomClassLoader") + " " + smMsg);
        execute(new String[] { "--module-path", autoModCloc, "-cp", regLoaderLoc, "-m", "mc/c.TestClient" }, "FAIL", MISSING_MODULE);
        System.out.println("Case:- Unknown modular Client and " + ((useSCL) ? "SystemClassLoader" : "Modular CustomClassLoader") + " " + smMsg);
        execute("-cp", regClientLoc, "--module-path", modLoadrLoc, addmodArg, "c.TestClient");
        System.out.println("Case:- Unknown modular Client and " + ((useSCL) ? "SystemClassLoader" : "Unknown modular CustomClassLoader") + " " + smMsg);
        execute("-cp", regClientLoc + separator + regLoaderLoc, "c.TestClient");
        System.out.println("Case:- Regular Client and " + ((useSCL) ? "SystemClassLoader" : "Unknown modular CustomClassLoader") + " inside --module-path " + smMsg);
        execute("--module-path", regClientLoc + separator + regLoaderLoc, autoAddModArg, "-m", "c/c.TestClient");
        System.out.println("Case:- Modular Client and " + ((useSCL) ? "SystemClassLoader" : "Modular CustomClassLoader") + " in -cp " + smMsg);
        execute("-cp", modClientLoc + separator + modLoadrLoc, "c.TestClient");
    }

    private void execute(String... args) throws Exception {
        execute(args, this.expectedStatus, this.expectedMsg);
    }

    private void execute(String[] args, String status, String msg) throws Exception {
        String[] safeArgs = Stream.concat(commonArgs.stream(), Stream.of(args)).filter(s -> {
            if (s.contains(" ")) {
                throw new RuntimeException("No spaces in args");
            }
            return !s.isEmpty();
        }).toArray(String[]::new);
        String out = ProcessTools.executeTestJvm(safeArgs).getOutput();
        if ("PASS".equals(status) && out.contains(msg)) {
            System.out.println("PASS: Expected Result: " + msg);
        } else if ("FAIL".equals(status) && out.contains(msg)) {
            System.out.printf("PASS: Expected Failure: " + msg);
        } else if (out.contains("Exception") || out.contains("Error")) {
            System.out.printf("OUTPUT: %s", out);
            throw new RuntimeException("FAIL: Unknown Exception.");
        } else {
            System.out.printf("OUTPUT: %s", out);
            throw new RuntimeException("FAIL: Unknown Test case found");
        }
    }

    private static void setUp() throws Exception {
        JarUtils.createJarFile(CL_JAR, TEST_CLASSES, "cl/TestClassLoader.class");
        JarUtils.createJarFile(C_JAR, TEST_CLASSES, "c/TestClient.class");
        Files.copy(CL_JAR, MCL_JAR, StandardCopyOption.REPLACE_EXISTING);
        updateModuleDescr(MCL_JAR, ModuleDescriptor.newModule("mcl").exports("cl").requires("java.base").build());
        Files.copy(C_JAR, MC_JAR, StandardCopyOption.REPLACE_EXISTING);
        updateModuleDescr(MC_JAR, ModuleDescriptor.newModule("mc").exports("c").requires("java.base").requires("mcl").build());
        Files.copy(C_JAR, AMC_JAR, StandardCopyOption.REPLACE_EXISTING);
        updateModuleDescr(AMC_JAR, ModuleDescriptor.newModule("mc").exports("c").requires("java.base").requires("cl").build());
    }

    private static void updateModuleDescr(Path jar, ModuleDescriptor mDescr) throws Exception {
        if (mDescr != null) {
            Path dir = Files.createTempDirectory("tmp");
            Path mi = dir.resolve("module-info.class");
            try (OutputStream out = Files.newOutputStream(mi)) {
                ModuleInfoWriter.write(mDescr, out);
            }
            System.out.format("Adding 'module-info.class' to jar '%s'%n", jar);
            JarUtils.updateJarFile(jar, dir);
        }
    }
}