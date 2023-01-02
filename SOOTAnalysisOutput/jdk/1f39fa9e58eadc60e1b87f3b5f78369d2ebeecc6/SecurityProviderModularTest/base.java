import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import jdk.testlibrary.ProcessTools;
import jdk.testlibrary.OutputAnalyzer;
import jdk.test.lib.compiler.CompilerUtils;
import org.testng.annotations.BeforeTest;

public class SecurityProviderModularTest extends ModularTest {

    private static final Path S_SRC = SRC.resolve("TestSecurityProvider.java");

    private static final String S_PKG = "provider";

    private static final String S_JAR_NAME = S_PKG + JAR_EXTN;

    private static final String S_WITH_DESCR_JAR_NAME = S_PKG + DESCRIPTOR + JAR_EXTN;

    private static final String MS_JAR_NAME = MODULAR + S_PKG + JAR_EXTN;

    private static final String MS_WITH_DESCR_JAR_NAME = MODULAR + S_PKG + DESCRIPTOR + JAR_EXTN;

    private static final Path C_SRC = SRC.resolve("TestSecurityProviderClient.java");

    private static final String C_PKG = "client";

    private static final String C_JAR_NAME = C_PKG + JAR_EXTN;

    private static final String MCN_JAR_NAME = MODULAR + C_PKG + "N" + JAR_EXTN;

    private static final String MC_JAR_NAME = MODULAR + C_PKG + JAR_EXTN;

    private static final Path BUILD_DIR = Paths.get(".").resolve("build");

    private static final Path COMPILE_DIR = BUILD_DIR.resolve("bin");

    private static final Path S_BUILD_DIR = COMPILE_DIR.resolve(S_PKG);

    private static final Path S_WITH_META_DESCR_BUILD_DIR = COMPILE_DIR.resolve(S_PKG + DESCRIPTOR);

    private static final Path C_BLD_DIR = COMPILE_DIR.resolve(C_PKG);

    private static final Path M_BASE_PATH = BUILD_DIR.resolve("mbase");

    private static final Path ARTIFACTS_DIR = BUILD_DIR.resolve("artifacts");

    private static final Path S_ARTIFACTS_DIR = ARTIFACTS_DIR.resolve(S_PKG);

    private static final Path S_JAR = S_ARTIFACTS_DIR.resolve(S_JAR_NAME);

    private static final Path S_WITH_DESCRIPTOR_JAR = S_ARTIFACTS_DIR.resolve(S_WITH_DESCR_JAR_NAME);

    private static final Path MS_JAR = S_ARTIFACTS_DIR.resolve(MS_JAR_NAME);

    private static final Path MS_WITH_DESCR_JAR = S_ARTIFACTS_DIR.resolve(MS_WITH_DESCR_JAR_NAME);

    private static final Path C_ARTIFACTS_DIR = ARTIFACTS_DIR.resolve(C_PKG);

    private static final Path C_JAR = C_ARTIFACTS_DIR.resolve(C_JAR_NAME);

    private static final Path MC_JAR = C_ARTIFACTS_DIR.resolve(MC_JAR_NAME);

    private static final Path MCN_JAR = C_ARTIFACTS_DIR.resolve(MCN_JAR_NAME);

    private static final String MAIN = C_PKG + ".TestSecurityProviderClient";

    private static final String S_INTERFACE = "java.security.Provider";

    private static final String S_IMPL = S_PKG + ".TestSecurityProvider";

    private static final List<String> M_REQUIRED = Arrays.asList("java.base");

    private static final Path META_DESCR_PATH = Paths.get("META-INF").resolve("services").resolve(S_INTERFACE);

    private static final Path S_META_DESCR_FPATH = S_WITH_META_DESCR_BUILD_DIR.resolve(META_DESCR_PATH);

    private static final boolean WITH_S_DESCR = true;

    private static final boolean WITHOUT_S_DESCR = false;

    private static final String PROVIDER_NOT_FOUND_MSG = "Unable to find Test" + " Security Provider";

    private static final String CAN_NOT_ACCESS_MSG = "cannot access class";

    private static final String NO_FAILURE = null;

    private static final String SERVICE_LOADER = "SERVICE_LOADER";

    private static final String CLASS_LOADER = "CLASS_LOADER";

    private static final String SECURITY_PROP = "SECURITY_PROP";

    private static final List<String> MECHANISMS = Arrays.asList(SERVICE_LOADER, CLASS_LOADER, SECURITY_PROP);

    private static final Path SECURE_PROP_EXTN = Paths.get("./java.secure.ext");

    @Override
    public Object[][] getTestInput() {
        List<List<Object>> params = new ArrayList<>();
        MECHANISMS.stream().forEach((mechanism) -> {
            boolean useCLoader = CLASS_LOADER.equals(mechanism);
            boolean useSLoader = SERVICE_LOADER.equals(mechanism);
            String[] args = new String[] { mechanism };
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.EXPLICIT, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.EXPLICIT, WITHOUT_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.AUTO, WITH_S_DESCR, ((useCLoader) ? CAN_NOT_ACCESS_MSG : NO_FAILURE), args));
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.AUTO, WITHOUT_S_DESCR, ((useCLoader) ? CAN_NOT_ACCESS_MSG : PROVIDER_NOT_FOUND_MSG), args));
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.UNNAMED, WITH_S_DESCR, ((useCLoader) ? CAN_NOT_ACCESS_MSG : NO_FAILURE), args));
            params.add(Arrays.asList(MODULE_TYPE.EXPLICIT, MODULE_TYPE.UNNAMED, WITHOUT_S_DESCR, ((useCLoader) ? CAN_NOT_ACCESS_MSG : ((useSLoader) ? PROVIDER_NOT_FOUND_MSG : NO_FAILURE)), args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.EXPLICIT, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.EXPLICIT, WITHOUT_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.AUTO, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.AUTO, WITHOUT_S_DESCR, (useCLoader) ? NO_FAILURE : PROVIDER_NOT_FOUND_MSG, args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.UNNAMED, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.AUTO, MODULE_TYPE.UNNAMED, WITHOUT_S_DESCR, ((useSLoader) ? PROVIDER_NOT_FOUND_MSG : NO_FAILURE), args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.EXPLICIT, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.EXPLICIT, WITHOUT_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.AUTO, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.AUTO, WITHOUT_S_DESCR, (useCLoader) ? NO_FAILURE : PROVIDER_NOT_FOUND_MSG, args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.UNNAMED, WITH_S_DESCR, NO_FAILURE, args));
            params.add(Arrays.asList(MODULE_TYPE.UNNAMED, MODULE_TYPE.UNNAMED, WITHOUT_S_DESCR, ((useSLoader) ? PROVIDER_NOT_FOUND_MSG : NO_FAILURE), args));
        });
        return params.stream().map(p -> p.toArray()).toArray(Object[][]::new);
    }

    @BeforeTest
    public void buildArtifacts() {
        boolean done = true;
        try {
            done &= CompilerUtils.compile(S_SRC, S_BUILD_DIR);
            done &= CompilerUtils.compile(S_SRC, S_WITH_META_DESCR_BUILD_DIR);
            done &= createMetaInfServiceDescriptor(S_META_DESCR_FPATH, S_IMPL);
            generateJar(true, MODULE_TYPE.EXPLICIT, MS_JAR, S_BUILD_DIR, false);
            generateJar(true, MODULE_TYPE.EXPLICIT, MS_WITH_DESCR_JAR, S_WITH_META_DESCR_BUILD_DIR, false);
            generateJar(true, MODULE_TYPE.UNNAMED, S_JAR, S_BUILD_DIR, false);
            generateJar(true, MODULE_TYPE.UNNAMED, S_WITH_DESCRIPTOR_JAR, S_WITH_META_DESCR_BUILD_DIR, false);
            done &= CompilerUtils.compile(C_SRC, C_BLD_DIR, "-cp", S_JAR.toFile().getCanonicalPath());
            generateJar(false, MODULE_TYPE.EXPLICIT, MC_JAR, C_BLD_DIR, true);
            generateJar(false, MODULE_TYPE.EXPLICIT, MCN_JAR, C_BLD_DIR, false);
            generateJar(false, MODULE_TYPE.UNNAMED, C_JAR, C_BLD_DIR, false);
            System.out.format("%nArtifacts generated successfully? %s", done);
            if (!done) {
                throw new RuntimeException("Artifacts generation failed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateJar(boolean isService, MODULE_TYPE moduleType, Path jar, Path compilePath, boolean depends) throws IOException {
        ModuleDescriptor mDescriptor = null;
        if (isService) {
            mDescriptor = generateModuleDescriptor(isService, moduleType, S_PKG, S_PKG, S_INTERFACE, S_IMPL, null, M_REQUIRED, depends);
        } else {
            mDescriptor = generateModuleDescriptor(isService, moduleType, C_PKG, C_PKG, S_INTERFACE, null, S_PKG, M_REQUIRED, depends);
        }
        generateJar(mDescriptor, jar, compilePath);
    }

    @Override
    public OutputAnalyzer executeTestClient(MODULE_TYPE cModuleType, Path cJarPath, MODULE_TYPE sModuletype, Path sJarPath, String... args) throws Exception {
        OutputAnalyzer output = null;
        try {
            if (!(cModuleType == MODULE_TYPE.UNNAMED && sModuletype == MODULE_TYPE.UNNAMED)) {
                copyJarsToModuleBase(cModuleType, cJarPath, M_BASE_PATH);
                copyJarsToModuleBase(sModuletype, sJarPath, M_BASE_PATH);
            }
            System.out.format("%nExecuting java client with required" + " custom security provider in class/module path.");
            String mName = getModuleName(cModuleType, cJarPath, C_PKG);
            Path cmBasePath = (cModuleType != MODULE_TYPE.UNNAMED || sModuletype != MODULE_TYPE.UNNAMED) ? M_BASE_PATH : null;
            String cPath = buildClassPath(cModuleType, cJarPath, sModuletype, sJarPath);
            Map<String, String> vmArgs = getVMArgs(sModuletype, getModuleName(sModuletype, sJarPath, S_PKG), args);
            output = ProcessTools.executeTestJava(getJavaCommand(cmBasePath, cPath, mName, MAIN, vmArgs, args)).outputTo(System.out).errorTo(System.out);
        } finally {
            cleanModuleBasePath(M_BASE_PATH);
        }
        return output;
    }

    @Override
    public Path findJarPath(boolean isService, MODULE_TYPE moduleType, boolean addMetaDesc, boolean dependsOnServiceModule) {
        if (isService) {
            if (moduleType == MODULE_TYPE.EXPLICIT) {
                if (addMetaDesc) {
                    return MS_WITH_DESCR_JAR;
                } else {
                    return MS_JAR;
                }
            } else {
                if (addMetaDesc) {
                    return S_WITH_DESCRIPTOR_JAR;
                } else {
                    return S_JAR;
                }
            }
        } else {
            if (moduleType == MODULE_TYPE.EXPLICIT) {
                if (dependsOnServiceModule) {
                    return MC_JAR;
                } else {
                    return MCN_JAR;
                }
            } else {
                return C_JAR;
            }
        }
    }

    private Map<String, String> getVMArgs(MODULE_TYPE sModuletype, String addModName, String... args) throws IOException {
        final Map<String, String> vmArgs = new LinkedHashMap<>();
        vmArgs.put("-Duser.language=", "en");
        vmArgs.put("-Duser.region=", "US");
        if (addModName != null && sModuletype == MODULE_TYPE.AUTO) {
            vmArgs.put("--add-modules=", addModName);
        }
        if (args != null && args.length > 0 && SECURITY_PROP.equals(args[0])) {
            if (sModuletype == MODULE_TYPE.UNNAMED) {
                Files.write(SECURE_PROP_EXTN, ("security.provider.10=" + S_IMPL).getBytes());
            } else {
                Files.write(SECURE_PROP_EXTN, "security.provider.10=TEST".getBytes());
            }
            vmArgs.put("-Djava.security.properties=", SECURE_PROP_EXTN.toFile().getCanonicalPath());
        }
        return vmArgs;
    }
}
