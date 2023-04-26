import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Security;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Builder;
import jdk.internal.module.ModuleInfoWriter;
import jdk.test.lib.process.ProcessTools;

public class SecurityProviderModularTest {

    private static final Path TEST_CLASSES = Paths.get(System.getProperty("test.classes"));

    private static final Path ARTIFACT_DIR = Paths.get("jars");

    private static final Path SEC_FILE = Paths.get("java.extn.security");

    private static final String PS = File.pathSeparator;

    private static final String P_TYPE = "p.TestProvider";

    private static final String C_TYPE = "c.TestClient";

    private static final Path P_JAR = artifact("p.jar");

    private static final Path PD_JAR = artifact("pd.jar");

    private static final Path MP_JAR = artifact("mp.jar");

    private static final Path MPD_JAR = artifact("mpd.jar");

    private static final Path MSP_JAR = artifact("msp.jar");

    private static final Path MSPD_JAR = artifact("mspd.jar");

    private static final Path C_JAR = artifact("c.jar");

    private static final Path MC_JAR = artifact("mc.jar");

    private static final Path MCS_JAR = artifact("mcs.jar");

    private static final Path AMC_JAR = artifact("amc.jar");

    private static final Path AMCS_JAR = artifact("amcs.jar");

    private static final Map<String, String> MSG_MAP = new HashMap<>();

    static {
        MSG_MAP.put("NoAccess", "cannot access class p.TestProvider");
        MSG_MAP.put("Success", "Client: found provider TestProvider");
        MSG_MAP.put("NoProvider", "Provider TestProvider not found");
    }

    private final String addUNArg;

    private final String addNMArg;

    private final String cArg;

    private final String unnP;

    private final String modP;

    private final String unnC;

    private final String modC;

    private final String autoMC;

    private final String expModRes;

    private final String expAModRes;

    private final List<String> commonArgs;

    public SecurityProviderModularTest(String use, boolean metaDesc) {
        List<String> argList = new LinkedList<>();
        argList.add("-Duser.language=en");
        argList.add("-Duser.region=US");
        final boolean useSL = "SL".equals(use) || "SPN".equals(use);
        final boolean useCL = "CL".equals(use);
        final boolean useSPT = "SPT".equals(use);
        final boolean useSP = use.startsWith("SP");
        if (useSP) {
            createJavaSecurityFileExtn("SPN".equals(use));
            argList.add("-Djava.security.properties=" + toAbsPath(SEC_FILE));
        }
        commonArgs = Collections.unmodifiableList(argList);
        cArg = (useCL) ? P_TYPE : "TestProvider";
        addUNArg = (useSL) ? "" : ("--add-modules=" + ((metaDesc) ? "pd" : "p"));
        addNMArg = (useSL) ? "" : "--add-modules=mp";
        unnP = toAbsPath((metaDesc) ? PD_JAR : P_JAR);
        modP = toAbsPath(useSL ? (metaDesc ? MSPD_JAR : MSP_JAR) : (metaDesc ? MPD_JAR : MP_JAR));
        unnC = toAbsPath(C_JAR);
        modC = toAbsPath(useSL ? MCS_JAR : MC_JAR);
        autoMC = toAbsPath(useSL ? AMCS_JAR : AMC_JAR);
        expModRes = "Success";
        expAModRes = (useSPT | useCL) ? "Success" : (metaDesc) ? "Success" : "NoProvider";
        String loadByMsg = useSP ? "SecurityPropertyFile" : ((useCL) ? "ClassLoader" : "ServiceLoader");
        System.out.printf("%n*** Providers loaded through %s and includes" + " META Descriptor: %s ***%n%n", loadByMsg, metaDesc);
    }

    public static void main(String[] args) throws Exception {
        setUp();
        boolean metaDesc = Boolean.valueOf(args[1]);
        SecurityProviderModularTest test = new SecurityProviderModularTest(args[0], metaDesc);
        test.process(args[0]);
    }

    private void process(String use) throws Exception {
        System.out.printf("Case: Modular Client and Modular Provider");
        execute(String.format("--module-path %s%s%s -m mc/%s %s %s", modC, PS, modP, C_TYPE, use, cArg), expModRes);
        System.out.printf("Case: Modular Client and automatic Provider");
        execute(String.format("--module-path %s%s%s %s -m mc/%s %s %s", autoMC, PS, unnP, addUNArg, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Modular Client and unnamed Provider");
        execute(String.format("--module-path %s -cp %s -m mc/%s %s %s", autoMC, unnP, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Automatic Client and modular Provider");
        execute(String.format("--module-path %s%s%s %s -m c/%s %s %s", unnC, PS, modP, addNMArg, C_TYPE, use, cArg), expModRes);
        System.out.printf("Case: Automatic Client and automatic Provider");
        execute(String.format("--module-path %s%s%s %s -m c/%s %s %s", unnC, PS, unnP, addUNArg, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Automatic Client and unnamed Provider");
        execute(String.format("--module-path %s -cp %s -m c/%s %s %s", unnC, unnP, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Unnamed Client and modular Provider");
        execute(String.format("-cp %s --module-path %s %s %s %s %s", unnC, modP, addNMArg, C_TYPE, use, cArg), expModRes);
        System.out.printf("Case: Unnamed Client and automatic Provider");
        execute(String.format("-cp %s --module-path %s %s %s %s %s", unnC, unnP, addUNArg, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Unnamed Client and unnamed Provider");
        execute(String.format("-cp %s%s%s %s %s %s", unnC, PS, unnP, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Unnamed Client and Unnamed Provider in modulepath");
        execute(String.format("--module-path %s%s%s %s -m c/%s %s %s", unnC, PS, unnP, addUNArg, C_TYPE, use, cArg), expAModRes);
        System.out.printf("Case: Modular Client and Modular Provider in classpath");
        execute(String.format("-cp %s%s%s %s %s %s", modC, PS, modP, C_TYPE, use, cArg), expAModRes);
    }

    private void execute(String args, String msgKey) throws Exception {
        String[] safeArgs = Stream.concat(commonArgs.stream(), Stream.of(args.split("\\s+"))).filter(s -> {
            if (s.contains(" ")) {
                throw new RuntimeException("No spaces in args");
            }
            return !s.isEmpty();
        }).toArray(String[]::new);
        String out = ProcessTools.executeTestJvm(safeArgs).getOutput();
        if ((msgKey != null && out.contains(MSG_MAP.get(msgKey)))) {
            System.out.printf("PASS: Expected Result: %s.%n", MSG_MAP.get(msgKey));
        } else if (out.contains("Exception") || out.contains("Error")) {
            System.out.printf("OUTPUT: %s", out);
            throw new RuntimeException("FAIL: Unknown Exception occured. " + "Expected: " + MSG_MAP.get(msgKey));
        } else {
            System.out.printf("OUTPUT: %s", out);
            throw new RuntimeException("FAIL: Unknown Test case found");
        }
    }

    private static void setUp() throws Exception {
        if (ARTIFACT_DIR.toFile().exists()) {
            System.out.println("Skipping setup: Artifacts already exists.");
            return;
        }
        JarUtils.createJarFile(P_JAR, TEST_CLASSES, "p/TestProvider.class");
        JarUtils.createJarFile(C_JAR, TEST_CLASSES, "c/TestClient.class");
        generateJar(P_JAR, PD_JAR, null, true);
        Builder mBuilder = ModuleDescriptor.newModule("mp").exports("p");
        generateJar(P_JAR, MPD_JAR, mBuilder.build(), true);
        generateJar(P_JAR, MP_JAR, mBuilder.build(), false);
        mBuilder = ModuleDescriptor.newModule("mp").provides("java.security.Provider", Arrays.asList(P_TYPE));
        generateJar(P_JAR, MSP_JAR, mBuilder.build(), false);
        generateJar(P_JAR, MSPD_JAR, mBuilder.build(), true);
        mBuilder = ModuleDescriptor.newModule("mc").exports("c");
        generateJar(C_JAR, AMC_JAR, mBuilder.build(), false);
        generateJar(C_JAR, MC_JAR, mBuilder.requires("mp").build(), false);
        mBuilder = ModuleDescriptor.newModule("mc").exports("c").uses("java.security.Provider");
        generateJar(C_JAR, AMCS_JAR, mBuilder.build(), false);
        generateJar(C_JAR, MCS_JAR, mBuilder.requires("mp").build(), false);
    }

    private static void generateJar(Path sjar, Path djar, ModuleDescriptor mDesc, boolean metaDesc) throws Exception {
        Files.copy(sjar, djar, StandardCopyOption.REPLACE_EXISTING);
        Path dir = Files.createTempDirectory("tmp");
        if (metaDesc) {
            write(dir.resolve(Paths.get("META-INF", "services", "java.security.Provider")), P_TYPE);
        }
        if (mDesc != null) {
            Path mi = dir.resolve("module-info.class");
            try (OutputStream out = Files.newOutputStream(mi)) {
                ModuleInfoWriter.write(mDesc, out);
            }
            System.out.format("Added 'module-info.class' in '%s'%n", djar);
        }
        JarUtils.updateJarFile(djar, dir);
    }

    private static Path artifact(String file) {
        return ARTIFACT_DIR.resolve(file);
    }

    private static String toAbsPath(Path path) {
        return path.toFile().getAbsolutePath();
    }

    private static Path ensurePath(Path at) throws IOException {
        Path parent = at.getParent();
        if (parent != null && !parent.toFile().exists()) {
            ensurePath(parent);
        }
        return Files.createDirectories(parent);
    }

    private static void write(Path at, String content) throws IOException {
        ensurePath(at);
        Files.write(at, content.getBytes("UTF-8"));
    }

    private static void createJavaSecurityFileExtn(boolean useName) {
        int insertAt = Security.getProviders().length + 1;
        String provider = (useName ? "TestProvider" : P_TYPE);
        try {
            Files.write(SEC_FILE, String.format("security.provider.%s=%s", insertAt, provider).getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Security property file created at: %s with value:" + " %s%n", SEC_FILE, provider);
    }
}
