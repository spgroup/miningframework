import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

public abstract class PKCS11Test {

    private boolean enableSM = false;

    static final Properties props = System.getProperties();

    static final String PKCS11 = "PKCS11";

    static final String BASE = System.getProperty("test.src", ".");

    static final char SEP = File.separatorChar;

    private static final String DEFAULT_POLICY = BASE + SEP + ".." + SEP + "policy";

    static final String CLOSED_BASE;

    static {
        String absBase = new File(BASE).getAbsolutePath();
        int k = absBase.indexOf(SEP + "test" + SEP + "sun" + SEP);
        if (k < 0)
            k = 0;
        String p1 = absBase.substring(0, k + 6);
        String p2 = absBase.substring(k + 5);
        CLOSED_BASE = p1 + "closed" + p2;
        System.setProperty("closed.base", CLOSED_BASE);
    }

    public static enum ECCState {

        None, Basic, Extended
    }

    static double nss_version = -1;

    static ECCState nss_ecc_status = ECCState.Extended;

    static String nss_library = "softokn3";

    static double softoken3_version = -1;

    static double nss3_version = -1;

    static Provider pkcs11;

    static {
        ServiceLoader sl = ServiceLoader.load(java.security.Provider.class);
        Iterator<Provider> iter = sl.iterator();
        Provider p = null;
        boolean found = false;
        while (iter.hasNext()) {
            try {
                p = iter.next();
                if (p.getName().equals("SunPKCS11")) {
                    found = true;
                    break;
                }
            } catch (Exception | ServiceConfigurationError e) {
            }
        }
        if (!found) {
            try {
                Class clazz = Class.forName("sun.security.pkcs11.SunPKCS11");
                p = (Provider) clazz.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        pkcs11 = p;
    }

    static boolean isBadSolarisSparc(Provider p) {
        if ("SunPKCS11-Solaris".equals(p.getName()) && badSolarisSparc) {
            System.out.println("SunPKCS11-Solaris provider requires " + "Solaris SPARC 11.2 or later, skipping");
            return true;
        }
        return false;
    }

    static Provider getSunPKCS11(String config) throws Exception {
        if (pkcs11 == null) {
            throw new NoSuchProviderException("No PKCS11 provider available");
        }
        return pkcs11.configure(config);
    }

    public abstract void main(Provider p) throws Exception;

    private void premain(Provider p) throws Exception {
        try {
            if (enableSM) {
                System.setSecurityManager(new SecurityManager());
            }
            long start = System.currentTimeMillis();
            System.out.printf("Running test with provider %s (security manager %s) ...%n", p.getName(), enableSM ? "enabled" : "disabled");
            main(p);
            long stop = System.currentTimeMillis();
            System.out.println("Completed test with provider " + p.getName() + " (" + (stop - start) + " ms).");
        } finally {
            if (enableSM) {
                System.setSecurityManager(null);
            }
        }
    }

    public static void main(PKCS11Test test) throws Exception {
        main(test, null);
    }

    public static void main(PKCS11Test test, String[] args) throws Exception {
        if (args != null) {
            if (args.length > 0) {
                if ("sm".equals(args[0])) {
                    test.enableSM = true;
                } else {
                    throw new RuntimeException("Unknown Command, use 'sm' as " + "first arguemtn to enable security manager");
                }
            }
            if (test.enableSM) {
                System.setProperty("java.security.policy", (args.length > 1) ? BASE + SEP + args[1] : DEFAULT_POLICY);
            }
        }
        Provider[] oldProviders = Security.getProviders();
        try {
            System.out.println("Beginning test run " + test.getClass().getName() + "...");
            testDefault(test);
            testNSS(test);
            testDeimos(test);
        } finally {
            Provider[] newProviders = Security.getProviders();
            boolean found = true;
            if (oldProviders.length == newProviders.length) {
                found = false;
                for (int i = 0; i < oldProviders.length; i++) {
                    if (oldProviders[i] != newProviders[i]) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                for (Provider p : newProviders) {
                    Security.removeProvider(p.getName());
                }
                for (Provider p : oldProviders) {
                    Security.addProvider(p);
                }
            }
        }
    }

    public static void testDeimos(PKCS11Test test) throws Exception {
        if (new File("/opt/SUNWconn/lib/libpkcs11.so").isFile() == false || "true".equals(System.getProperty("NO_DEIMOS"))) {
            return;
        }
        String base = getBase();
        String p11config = base + SEP + "nss" + SEP + "p11-deimos.txt";
        Provider p = getSunPKCS11(p11config);
        test.premain(p);
    }

    public static void testDefault(PKCS11Test test) throws Exception {
        if ("true".equals(System.getProperty("NO_DEFAULT"))) {
            return;
        }
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            Provider p = providers[i];
            if (p.getName().startsWith("SunPKCS11-")) {
                test.premain(p);
            }
        }
    }

    private static String PKCS11_BASE;

    static {
        try {
            PKCS11_BASE = getBase();
        } catch (Exception e) {
        }
    }

    private final static String PKCS11_REL_PATH = "sun/security/pkcs11";

    public static String getBase() throws Exception {
        if (PKCS11_BASE != null) {
            return PKCS11_BASE;
        }
        File cwd = new File(System.getProperty("test.src", ".")).getCanonicalFile();
        while (true) {
            File file = new File(cwd, "TEST.ROOT");
            if (file.isFile()) {
                break;
            }
            cwd = cwd.getParentFile();
            if (cwd == null) {
                throw new Exception("Test root directory not found");
            }
        }
        PKCS11_BASE = new File(cwd, PKCS11_REL_PATH.replace('/', SEP)).getAbsolutePath();
        return PKCS11_BASE;
    }

    public static String getNSSLibDir() throws Exception {
        return getNSSLibDir(nss_library);
    }

    static String getNSSLibDir(String library) throws Exception {
        String osName = props.getProperty("os.name");
        if (osName.startsWith("Win")) {
            osName = "Windows";
        } else if (osName.equals("Mac OS X")) {
            osName = "MacOSX";
        }
        String osid = osName + "-" + props.getProperty("os.arch") + "-" + props.getProperty("sun.arch.data.model");
        String[] nssLibDirs = osMap.get(osid);
        if (nssLibDirs == null) {
            System.out.println("Warning: unsupported OS: " + osid + ", please initialize NSS librarys location firstly, skipping test");
            return null;
        }
        if (nssLibDirs.length == 0) {
            System.out.println("Warning: NSS not supported on this platform, skipping test");
            return null;
        }
        String nssLibDir = null;
        for (String dir : nssLibDirs) {
            if (new File(dir).exists() && new File(dir + System.mapLibraryName(library)).exists()) {
                nssLibDir = dir;
                System.setProperty("pkcs11test.nss.libdir", nssLibDir);
                break;
            }
        }
        if (nssLibDir == null) {
            System.out.println("Warning: can't find NSS librarys on this machine, skipping test");
            return null;
        }
        return nssLibDir;
    }

    static boolean isBadNSSVersion(Provider p) {
        if (isNSS(p) && badNSSVersion) {
            System.out.println("NSS 3.11 has a DER issue that recent " + "version do not.");
            return true;
        }
        return false;
    }

    protected static void safeReload(String lib) throws Exception {
        try {
            System.load(lib);
        } catch (UnsatisfiedLinkError e) {
            if (e.getMessage().contains("already loaded")) {
                return;
            }
        }
    }

    static boolean loadNSPR(String libdir) throws Exception {
        safeReload(libdir + System.mapLibraryName("nspr4"));
        safeReload(libdir + System.mapLibraryName("plc4"));
        safeReload(libdir + System.mapLibraryName("plds4"));
        safeReload(libdir + System.mapLibraryName("sqlite3"));
        safeReload(libdir + System.mapLibraryName("nssutil3"));
        return true;
    }

    public static boolean isNSS(Provider p) {
        return p.getName().toUpperCase().equals("SUNPKCS11-NSS");
    }

    static double getNSSVersion() {
        if (nss_version == -1)
            getNSSInfo();
        return nss_version;
    }

    static ECCState getNSSECC() {
        if (nss_version == -1)
            getNSSInfo();
        return nss_ecc_status;
    }

    public static double getLibsoftokn3Version() {
        if (softoken3_version == -1)
            return getNSSInfo("softokn3");
        return softoken3_version;
    }

    public static double getLibnss3Version() {
        if (nss3_version == -1)
            return getNSSInfo("nss3");
        return nss3_version;
    }

    static void getNSSInfo() {
        getNSSInfo(nss_library);
    }

    static double getNSSInfo(String library) {
        String nssHeader1 = "$Header: NSS";
        String nssHeader2 = "Version: NSS";
        boolean found = false;
        String s = null;
        int i = 0;
        String libfile = "";
        if (library.compareTo("softokn3") == 0 && softoken3_version > -1)
            return softoken3_version;
        if (library.compareTo("nss3") == 0 && nss3_version > -1)
            return nss3_version;
        try {
            libfile = getNSSLibDir() + System.mapLibraryName(library);
            try (FileInputStream is = new FileInputStream(libfile)) {
                byte[] data = new byte[1000];
                int read = 0;
                while (is.available() > 0) {
                    if (read == 0) {
                        read = is.read(data, 0, 1000);
                    } else {
                        System.arraycopy(data, 900, data, 0, 100);
                        read = 100 + is.read(data, 100, 900);
                    }
                    s = new String(data, 0, read);
                    i = s.indexOf(nssHeader1);
                    if (i > 0 || (i = s.indexOf(nssHeader2)) > 0) {
                        found = true;
                        if (i < 920) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!found) {
            System.out.println("lib" + library + " version not found, set to 0.0: " + libfile);
            nss_version = 0.0;
            return nss_version;
        }
        int afterheader = s.indexOf("NSS", i) + 4;
        int nextSpaceIndex = s.indexOf(' ', afterheader);
        if (nextSpaceIndex == -1) {
            System.out.println("===== Content start =====");
            System.out.println(s);
            System.out.println("===== Content end =====");
        }
        String version = s.substring(afterheader, nextSpaceIndex);
        String[] dot = version.split("\\.");
        if (dot.length > 2) {
            version = dot[0] + "." + dot[1];
            for (int j = 2; dot.length > j; j++) {
                version += dot[j];
            }
        }
        try {
            nss_version = Double.parseDouble(version);
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse lib" + library + " version. Set to 0.0");
            e.printStackTrace();
        }
        System.out.print("lib" + library + " version = " + version + ".  ");
        if (s.indexOf("Basic") > 0) {
            nss_ecc_status = ECCState.Basic;
            System.out.println("ECC Basic.");
        } else if (s.indexOf("Extended") > 0) {
            nss_ecc_status = ECCState.Extended;
            System.out.println("ECC Extended.");
        } else {
            System.out.println("ECC None.");
        }
        if (library.compareTo("softokn3") == 0) {
            softoken3_version = nss_version;
        } else if (library.compareTo("nss3") == 0) {
            nss3_version = nss_version;
        }
        return nss_version;
    }

    public static void useNSS() {
        nss_library = "nss3";
    }

    public static void testNSS(PKCS11Test test) throws Exception {
        String libdir = getNSSLibDir();
        if (libdir == null) {
            return;
        }
        String base = getBase();
        if (loadNSPR(libdir) == false) {
            return;
        }
        String libfile = libdir + System.mapLibraryName(nss_library);
        String customDBdir = System.getProperty("CUSTOM_DB_DIR");
        String dbdir = (customDBdir != null) ? customDBdir : base + SEP + "nss" + SEP + "db";
        dbdir = dbdir.replace('\\', '/');
        String customConfig = System.getProperty("CUSTOM_P11_CONFIG");
        String customConfigName = System.getProperty("CUSTOM_P11_CONFIG_NAME", "p11-nss.txt");
        String p11config = (customConfig != null) ? customConfig : base + SEP + "nss" + SEP + customConfigName;
        System.setProperty("pkcs11test.nss.lib", libfile);
        System.setProperty("pkcs11test.nss.db", dbdir);
        Provider p = getSunPKCS11(p11config);
        test.premain(p);
    }

    static List<ECParameterSpec> getKnownCurves(Provider p) throws Exception {
        int index;
        int begin;
        int end;
        String curve;
        List<ECParameterSpec> results = new ArrayList<>();
        String kcProp = Security.getProvider("SunEC").getProperty("AlgorithmParameters.EC SupportedCurves");
        if (kcProp == null) {
            throw new RuntimeException("\"AlgorithmParameters.EC SupportedCurves property\" not found");
        }
        System.out.println("Finding supported curves using list from SunEC\n");
        index = 0;
        for (; ; ) {
            begin = kcProp.indexOf('[', index);
            end = kcProp.indexOf(']', index);
            if (begin == -1 || end == -1) {
                break;
            }
            index = end + 1;
            begin++;
            end = kcProp.indexOf(',', begin);
            if (end == -1) {
                end = index - 1;
            }
            curve = kcProp.substring(begin, end);
            ECParameterSpec e = getECParameterSpec(p, curve);
            System.out.print("\t " + curve + ": ");
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", p);
                kpg.initialize(e);
                kpg.generateKeyPair();
                results.add(e);
                System.out.println("Supported");
            } catch (ProviderException ex) {
                System.out.println("Unsupported: PKCS11: " + ex.getCause().getMessage());
            } catch (InvalidAlgorithmParameterException ex) {
                System.out.println("Unsupported: Key Length: " + ex.getMessage());
            }
        }
        if (results.size() == 0) {
            throw new RuntimeException("No supported EC curves found");
        }
        return results;
    }

    private static ECParameterSpec getECParameterSpec(Provider p, String name) throws Exception {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", p);
        parameters.init(new ECGenParameterSpec(name));
        return parameters.getParameterSpec(ECParameterSpec.class);
    }

    boolean checkSupport(List<ECParameterSpec> supportedEC, ECParameterSpec curve) {
        for (ECParameterSpec ec : supportedEC) {
            if (ec.equals(curve)) {
                return true;
            }
        }
        return false;
    }

    private static final Map<String, String[]> osMap;

    static {
        osMap = new HashMap<>();
        osMap.put("SunOS-sparc-32", new String[] { "/usr/lib/mps/" });
        osMap.put("SunOS-sparcv9-64", new String[] { "/usr/lib/mps/64/" });
        osMap.put("SunOS-x86-32", new String[] { "/usr/lib/mps/" });
        osMap.put("SunOS-amd64-64", new String[] { "/usr/lib/mps/64/" });
        osMap.put("Linux-i386-32", new String[] { "/usr/lib/i386-linux-gnu/", "/usr/lib32/", "/usr/lib/" });
        osMap.put("Linux-amd64-64", new String[] { "/usr/lib/x86_64-linux-gnu/", "/usr/lib/x86_64-linux-gnu/nss/", "/usr/lib64/" });
        osMap.put("Linux-ppc64-64", new String[] { "/usr/lib64/" });
        osMap.put("Linux-ppc64le-64", new String[] { "/usr/lib64/" });
        osMap.put("Windows-x86-32", new String[] { PKCS11_BASE + "/nss/lib/windows-i586/".replace('/', SEP) });
        osMap.put("Windows-amd64-64", new String[] { PKCS11_BASE + "/nss/lib/windows-amd64/".replace('/', SEP) });
        osMap.put("MacOSX-x86_64-64", new String[] { PKCS11_BASE + "/nss/lib/macosx-x86_64/" });
        osMap.put("Linux-arm-32", new String[] { "/usr/lib/arm-linux-gnueabi/nss/", "/usr/lib/arm-linux-gnueabihf/nss/" });
        osMap.put("Linux-aarch64-64", new String[] { "/usr/lib/aarch64-linux-gnu/nss/" });
    }

    private final static char[] hexDigits = "0123456789abcdef".toCharArray();

    static final boolean badNSSVersion = getNSSVersion() >= 3.11 && getNSSVersion() < 3.12;

    private static final String distro = distro();

    static final boolean badSolarisSparc = System.getProperty("os.name").equals("SunOS") && System.getProperty("os.arch").equals("sparcv9") && System.getProperty("os.version").compareTo("5.11") <= 0 && getDistro().compareTo("11.2") < 0;

    public static String toString(byte[] b) {
        if (b == null) {
            return "(null)";
        }
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            int k = b[i] & 0xff;
            if (i != 0) {
                sb.append(':');
            }
            sb.append(hexDigits[k >>> 4]);
            sb.append(hexDigits[k & 0xf]);
        }
        return sb.toString();
    }

    public static byte[] parse(String s) {
        if (s.equals("(null)")) {
            return null;
        }
        try {
            int n = s.length();
            ByteArrayOutputStream out = new ByteArrayOutputStream(n / 3);
            StringReader r = new StringReader(s);
            while (true) {
                int b1 = nextNibble(r);
                if (b1 < 0) {
                    break;
                }
                int b2 = nextNibble(r);
                if (b2 < 0) {
                    throw new RuntimeException("Invalid string " + s);
                }
                int b = (b1 << 4) | b2;
                out.write(b);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int nextNibble(StringReader r) throws IOException {
        while (true) {
            int ch = r.read();
            if (ch == -1) {
                return -1;
            } else if ((ch >= '0') && (ch <= '9')) {
                return ch - '0';
            } else if ((ch >= 'a') && (ch <= 'f')) {
                return ch - 'a' + 10;
            } else if ((ch >= 'A') && (ch <= 'F')) {
                return ch - 'A' + 10;
            }
        }
    }

    <T> T[] concat(T[] a, T[] b) {
        if ((b == null) || (b.length == 0)) {
            return a;
        }
        T[] r = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    static List<String> getSupportedAlgorithms(String type, String alg, Provider p) {
        List<String> algorithms = new ArrayList<>();
        Set<Provider.Service> services = p.getServices();
        for (Provider.Service service : services) {
            if (service.getType().equals(type) && service.getAlgorithm().startsWith(alg)) {
                algorithms.add(service.getAlgorithm());
            }
        }
        return algorithms;
    }

    static String getDistro() {
        return distro;
    }

    private static String distro() {
        if (props.getProperty("os.name").equals("SunOS")) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("uname -v").getInputStream()))) {
                return in.readLine();
            } catch (Exception e) {
                throw new RuntimeException("Failed to determine distro.", e);
            }
        } else {
            return null;
        }
    }

    static byte[] generateData(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }
}
