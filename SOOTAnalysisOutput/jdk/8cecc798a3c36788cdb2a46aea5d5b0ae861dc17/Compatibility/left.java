import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.util.JarUtils;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Compatibility {

    private static final String TEST_SRC = System.getProperty("test.src");

    private static final String TEST_CLASSES = System.getProperty("test.classes");

    private static final String TEST_JDK = System.getProperty("test.jdk");

    private static JdkInfo TEST_JDK_INFO;

    private static final String PROXY_HOST = System.getProperty("proxyHost");

    private static final String PROXY_PORT = System.getProperty("proxyPort", "80");

    private static final String JAVA_SECURITY = System.getProperty("javaSecurityFile", TEST_SRC + "/java.security");

    private static final String PASSWORD = "testpass";

    private static final String KEYSTORE = "testKeystore.jks";

    private static final String RSA = "RSA";

    private static final String DSA = "DSA";

    private static final String EC = "EC";

    private static String[] KEY_ALGORITHMS;

    private static final String[] DEFAULT_KEY_ALGORITHMS = new String[] { RSA, DSA, EC };

    private static final String SHA1 = "SHA-1";

    private static final String SHA256 = "SHA-256";

    private static final String SHA384 = "SHA-384";

    private static final String SHA512 = "SHA-512";

    private static final String DEFAULT = "DEFAULT";

    private static String[] DIGEST_ALGORITHMS;

    private static final String[] DEFAULT_DIGEST_ALGORITHMS = new String[] { SHA1, SHA256, SHA384, SHA512, DEFAULT };

    private static final boolean[] EXPIRED = Boolean.valueOf(System.getProperty("expired", "true")) ? new boolean[] { false, true } : new boolean[] { false };

    private static final boolean TEST_COMPREHENSIVE_JAR_CONTENTS = Boolean.valueOf(System.getProperty("testComprehensiveJarContents", "false"));

    private static final boolean TEST_JAR_UPDATE = Boolean.valueOf(System.getProperty("testJarUpdate", "false"));

    private static final boolean STRICT = Boolean.valueOf(System.getProperty("strict", "false"));

    private static final Calendar CALENDAR = Calendar.getInstance();

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final int CERT_VALIDITY = Integer.valueOf(System.getProperty("certValidity", "1440"));

    static {
        if (CERT_VALIDITY < 1 || CERT_VALIDITY > 1440) {
            throw new RuntimeException("certValidity out of range [1, 1440]: " + CERT_VALIDITY);
        }
    }

    public static final boolean DELAY_VERIFY = Boolean.valueOf(System.getProperty("delayVerify", "false"));

    private static long lastCertStartTime;

    private static DetailsOutputStream detailsOutput;

    private static int sigfileCounter;

    private static String nextSigfileName(String alias, String u, String s) {
        String sigfileName = "" + (++sigfileCounter);
        System.out.println("using sigfile " + sigfileName + " for alias " + alias + " signing " + u + ".jar to " + s + ".jar");
        return sigfileName;
    }

    public static void main(String... args) throws Throwable {
        PrintStream origStdOut = System.out;
        PrintStream origStdErr = System.err;
        detailsOutput = new DetailsOutputStream(outfile());
        PrintStream printStream = new PrintStream(detailsOutput);
        System.setOut(printStream);
        System.setErr(printStream);
        TEST_JDK_INFO = new JdkInfo(TEST_JDK);
        List<TsaInfo> tsaList = tsaInfoList();
        List<JdkInfo> jdkInfoList = jdkInfoList();
        List<CertInfo> certList = createCertificates(jdkInfoList);
        List<SignItem> signItems = test(jdkInfoList, tsaList, certList, createJars());
        boolean failed = generateReport(tsaList, signItems);
        System.setOut(origStdOut);
        System.setErr(origStdErr);
        if (failed) {
            throw new RuntimeException("At least one test case failed. " + "Please check the failed row(s) in report.html " + "or failedReport.html.");
        }
    }

    private static SignItem createJarFile(String jar, Manifest m, String... files) throws IOException {
        JarUtils.createJarFile(Path.of(jar), m, Path.of("."), Arrays.stream(files).map(Path::of).toArray(Path[]::new));
        return SignItem.build().signedJar(jar.replaceAll("[.]jar$", "")).addContentFiles(Arrays.stream(files).collect(Collectors.toList()));
    }

    private static String createDummyFile(String name) throws IOException {
        if (name.contains("/"))
            new File(name).getParentFile().mkdir();
        try (OutputStream fos = new FileOutputStream(name)) {
            fos.write(name.getBytes(UTF_8));
        }
        return name;
    }

    private static List<SignItem> createJars() throws IOException {
        List<SignItem> jarList = new ArrayList<>();
        Manifest m = new Manifest();
        m.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0");
        jarList.add(createJarFile("test.jar", m, createDummyFile("dummy")));
        if (TEST_COMPREHENSIVE_JAR_CONTENTS) {
            jarList.add(createJarFile("empty.jar", m));
            JarUtils.createJar("nomainatts.jar");
            jarList.add(SignItem.build().signedJar("nomainatts"));
            jarList.add(createJarFile("files.jar", m, IntStream.range(1, 9).boxed().map(i -> {
                try {
                    return createDummyFile("dummy" + i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(String[]::new)));
            jarList.add(createJarFile("longfilename.jar", m, createDummyFile("test".repeat(20))));
        }
        return jarList;
    }

    private static List<SignItem> updateJar(SignItem prev) throws IOException {
        List<SignItem> jarList = new ArrayList<>();
        Files.copy(Path.of(prev.signedJar + ".jar"), Path.of(prev.signedJar + "-signagainunmodified.jar"));
        jarList.add(SignItem.build(prev).signedJar(prev.signedJar + "-signagainunmodified"));
        String oldJar = prev.signedJar;
        String newJar = oldJar + "-addfile";
        String triggerUpdateFile = "addfile";
        JarUtils.updateJar(oldJar + ".jar", newJar + ".jar", triggerUpdateFile);
        jarList.add(SignItem.build(prev).signedJar(newJar).addContentFiles(Arrays.asList(triggerUpdateFile)));
        return jarList;
    }

    private static List<CertInfo> createCertificates(List<JdkInfo> jdkInfoList) throws Throwable {
        List<CertInfo> certList = new ArrayList<>();
        Set<String> expiredCertFilter = new HashSet<>();
        for (JdkInfo jdkInfo : jdkInfoList) {
            for (String keyAlgorithm : keyAlgs()) {
                if (!jdkInfo.supportsKeyAlg(keyAlgorithm))
                    continue;
                for (int keySize : keySizes(keyAlgorithm)) {
                    for (String digestAlgorithm : digestAlgs()) {
                        for (boolean expired : EXPIRED) {
                            if (expired && !expiredCertFilter.add(keyAlgorithm)) {
                                continue;
                            }
                            CertInfo certInfo = new CertInfo(jdkInfo, keyAlgorithm, digestAlgorithm, keySize, expired);
                            String sigalg = certInfo.sigalg();
                            if (sigalg != null && !jdkInfo.isSupportedSigalg(sigalg)) {
                                continue;
                            }
                            createCertificate(jdkInfo, certInfo);
                            certList.add(certInfo);
                        }
                    }
                }
            }
        }
        System.out.println("the keystore contents:");
        for (JdkInfo jdkInfo : jdkInfoList) {
            execTool(jdkInfo.jdkPath + "/bin/keytool", new String[] { "-v", "-storetype", "jks", "-storepass", PASSWORD, "-keystore", KEYSTORE, "-list" });
        }
        return certList;
    }

    private static void createCertificate(JdkInfo jdkInfo, CertInfo certInfo) throws Throwable {
        List<String> arguments = new ArrayList<>();
        arguments.add("-J-Djava.security.properties=" + JAVA_SECURITY);
        arguments.add("-v");
        arguments.add("-debug");
        arguments.add("-storetype");
        arguments.add("jks");
        arguments.add("-keystore");
        arguments.add(KEYSTORE);
        arguments.add("-storepass");
        arguments.add(PASSWORD);
        arguments.add(jdkInfo.majorVersion < 6 ? "-genkey" : "-genkeypair");
        arguments.add("-keyalg");
        arguments.add(certInfo.keyAlgorithm);
        String sigalg = certInfo.sigalg();
        if (sigalg != null) {
            arguments.add("-sigalg");
            arguments.add(sigalg);
        }
        if (certInfo.keySize != 0) {
            arguments.add("-keysize");
            arguments.add(certInfo.keySize + "");
        }
        arguments.add("-dname");
        arguments.add("CN=" + certInfo);
        arguments.add("-alias");
        arguments.add(certInfo.alias());
        arguments.add("-keypass");
        arguments.add(PASSWORD);
        arguments.add("-startdate");
        arguments.add(startDate(certInfo.expired));
        arguments.add("-validity");
        arguments.add("1");
        OutputAnalyzer outputAnalyzer = execTool(jdkInfo.jdkPath + "/bin/keytool", arguments.toArray(new String[arguments.size()]));
        if (outputAnalyzer.getExitValue() != 0 || outputAnalyzer.getOutput().matches("[Ee]xception") || outputAnalyzer.getOutput().matches(Test.ERROR + " ?")) {
            System.out.println(outputAnalyzer.getOutput());
            throw new Exception("error generating a key pair: " + arguments);
        }
    }

    private static String startDate(boolean expiredCert) {
        CALENDAR.setTime(new Date());
        if (DELAY_VERIFY || expiredCert) {
            CALENDAR.add(Calendar.DAY_OF_MONTH, -1);
        }
        if (DELAY_VERIFY && !expiredCert) {
            CALENDAR.add(Calendar.MINUTE, CERT_VALIDITY);
        }
        Date startDate = CALENDAR.getTime();
        if (!expiredCert) {
            lastCertStartTime = startDate.getTime();
        }
        return DATE_FORMAT.format(startDate);
    }

    private static String outfile() {
        return System.getProperty("o");
    }

    private static List<JdkInfo> jdkInfoList() throws Throwable {
        String[] jdkList = list("jdkList");
        if (jdkList.length == 0) {
            jdkList = new String[] { "TEST_JDK" };
        }
        List<JdkInfo> jdkInfoList = new ArrayList<>();
        for (String jdkPath : jdkList) {
            JdkInfo jdkInfo = "TEST_JDK".equalsIgnoreCase(jdkPath) ? TEST_JDK_INFO : new JdkInfo(jdkPath);
            if (!jdkInfoList.contains(jdkInfo)) {
                jdkInfoList.add(jdkInfo);
            } else {
                System.out.println("The JDK version is duplicate: " + jdkPath);
            }
        }
        return jdkInfoList;
    }

    private static List<String> keyAlgs() throws IOException {
        if (KEY_ALGORITHMS == null)
            KEY_ALGORITHMS = list("keyAlgs");
        if (KEY_ALGORITHMS.length == 0)
            return Arrays.asList(DEFAULT_KEY_ALGORITHMS);
        return Arrays.stream(KEY_ALGORITHMS).map(a -> a.split(";")[0]).collect(Collectors.toList());
    }

    private static int[] keySizes(String keyAlgorithm) throws IOException {
        if (KEY_ALGORITHMS == null)
            KEY_ALGORITHMS = list("keyAlgs");
        for (String keyAlg : KEY_ALGORITHMS) {
            String[] split = (keyAlg + " ").split(";");
            if (keyAlgorithm.equals(split[0].trim()) && split.length > 1) {
                int[] sizes = new int[split.length - 1];
                for (int i = 1; i <= sizes.length; i++) sizes[i - 1] = split[i].isBlank() ? 0 : Integer.parseInt(split[i].trim());
                return sizes;
            }
        }
        if (RSA.equals(keyAlgorithm) || DSA.equals(keyAlgorithm)) {
            return new int[] { 1024, 2048, 0 };
        } else if (EC.equals(keyAlgorithm)) {
            return new int[] { 384, 571, 0 };
        } else {
            throw new RuntimeException("problem determining key sizes");
        }
    }

    private static List<String> digestAlgs() throws IOException {
        if (DIGEST_ALGORITHMS == null)
            DIGEST_ALGORITHMS = list("digestAlgs");
        if (DIGEST_ALGORITHMS.length == 0)
            return Arrays.asList(DEFAULT_DIGEST_ALGORITHMS);
        return Arrays.asList(DIGEST_ALGORITHMS);
    }

    private static List<TsaInfo> tsaInfoList() throws IOException {
        String[] tsaList = list("tsaList");
        List<TsaInfo> tsaInfoList = new ArrayList<>();
        for (int i = 0; i < tsaList.length; i++) {
            String[] values = tsaList[i].split(";digests=");
            String[] digests = new String[0];
            if (values.length == 2) {
                digests = values[1].split(",");
            }
            String tsaUrl = values[0];
            if (tsaUrl.isEmpty() || tsaUrl.equalsIgnoreCase("notsa")) {
                tsaUrl = null;
            }
            TsaInfo bufTsa = new TsaInfo(i, tsaUrl);
            for (String digest : digests) {
                bufTsa.addDigest(digest.toUpperCase());
            }
            tsaInfoList.add(bufTsa);
        }
        if (tsaInfoList.size() == 0) {
            throw new RuntimeException("TSA service is mandatory unless " + "'notsa' specified explicitly.");
        }
        return tsaInfoList;
    }

    private static String[] list(String listProp) throws IOException {
        String listFileProp = listProp + "File";
        String listFile = System.getProperty(listFileProp);
        if (!isEmpty(listFile)) {
            System.out.println(listFileProp + "=" + listFile);
            List<String> list = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(listFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String item = line.trim();
                if (!item.isEmpty()) {
                    list.add(item);
                }
            }
            reader.close();
            return list.toArray(new String[list.size()]);
        }
        String list = System.getProperty(listProp);
        System.out.println(listProp + "=" + list);
        return !isEmpty(list) ? list.split("#") : new String[0];
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private static List<SignItem> test(List<JdkInfo> jdkInfoList, List<TsaInfo> tsaInfoList, List<CertInfo> certList, List<SignItem> jars) throws Throwable {
        detailsOutput.transferPhase();
        List<SignItem> signItems = new ArrayList<>();
        signItems.addAll(signing(jdkInfoList, tsaInfoList, certList, jars));
        if (TEST_JAR_UPDATE) {
            signItems.addAll(signing(jdkInfoList, tsaInfoList, certList, updating(signItems.stream().filter(x -> x.status != Status.ERROR).collect(Collectors.toList()))));
        }
        detailsOutput.transferPhase();
        for (SignItem signItem : signItems) {
            for (JdkInfo verifierInfo : jdkInfoList) {
                if (!verifierInfo.supportsKeyAlg(signItem.certInfo.keyAlgorithm))
                    continue;
                VerifyItem verifyItem = VerifyItem.build(verifierInfo);
                verifyItem.addSignerCertInfos(signItem);
                signItem.addVerifyItem(verifyItem);
                verifying(signItem, verifyItem);
            }
        }
        long lastCertExpirationTime = lastCertStartTime + 24 * 60 * 60 * 1000;
        if (lastCertExpirationTime < System.currentTimeMillis()) {
            throw new AssertionError("CERT_VALIDITY (" + CERT_VALIDITY + " [minutes]) was too short. " + "Creating and signing the jars took longer, " + "presumably at least " + ((lastCertExpirationTime - System.currentTimeMillis()) / 60 * 1000 + CERT_VALIDITY) + " [minutes].");
        }
        if (DELAY_VERIFY) {
            detailsOutput.transferPhase();
            System.out.print("Waiting for delay verifying");
            while (System.currentTimeMillis() < lastCertExpirationTime) {
                TimeUnit.SECONDS.sleep(30);
                System.out.print(".");
            }
            System.out.println();
            System.out.println("Delay verifying starts");
            for (SignItem signItem : signItems) {
                for (VerifyItem verifyItem : signItem.verifyItems) {
                    verifying(signItem, verifyItem);
                }
            }
        }
        detailsOutput.transferPhase();
        return signItems;
    }

    private static List<SignItem> signing(List<JdkInfo> jdkInfos, List<TsaInfo> tsaList, List<CertInfo> certList, List<SignItem> unsignedJars) throws Throwable {
        List<SignItem> signItems = new ArrayList<>();
        for (CertInfo certInfo : certList) {
            JdkInfo signerInfo = certInfo.jdkInfo;
            String keyAlgorithm = certInfo.keyAlgorithm;
            String sigDigestAlgorithm = certInfo.digestAlgorithm;
            int keySize = certInfo.keySize;
            boolean expired = certInfo.expired;
            for (String jarDigestAlgorithm : digestAlgs()) {
                if (DEFAULT.equals(jarDigestAlgorithm)) {
                    jarDigestAlgorithm = null;
                }
                for (TsaInfo tsaInfo : tsaList) {
                    String tsaUrl = tsaInfo.tsaUrl;
                    List<String> tsaDigestAlgs = digestAlgs();
                    if (tsaUrl == null)
                        tsaDigestAlgs = Arrays.asList(DEFAULT);
                    if (!signerInfo.supportsTsadigestalg) {
                        tsaDigestAlgs = Arrays.asList(DEFAULT);
                    }
                    for (String tsaDigestAlg : tsaDigestAlgs) {
                        if (DEFAULT.equals(tsaDigestAlg)) {
                            tsaDigestAlg = null;
                        } else if (!tsaInfo.isDigestSupported(tsaDigestAlg)) {
                            continue;
                        }
                        if (tsaUrl != null && TsaFilter.filter(signerInfo.version, tsaDigestAlg, expired, tsaInfo.index)) {
                            continue;
                        }
                        for (SignItem prevSign : unsignedJars) {
                            String unsignedJar = prevSign.signedJar;
                            SignItem signItem = SignItem.build(prevSign).certInfo(certInfo).jdkInfo(signerInfo);
                            String signedJar = unsignedJar + "-" + "JDK_" + (signerInfo.version + "-CERT_" + certInfo).replaceAll("[^a-z_0-9A-Z.]+", "-");
                            if (jarDigestAlgorithm != null) {
                                signedJar += "-DIGESTALG_" + jarDigestAlgorithm;
                                signItem.digestAlgorithm(jarDigestAlgorithm);
                            }
                            if (tsaUrl == null) {
                                signItem.tsaIndex(-1);
                            } else {
                                signedJar += "-TSA_" + tsaInfo.index;
                                signItem.tsaIndex(tsaInfo.index);
                                if (tsaDigestAlg != null) {
                                    signedJar += "-TSADIGALG_" + tsaDigestAlg;
                                    signItem.tsaDigestAlgorithm(tsaDigestAlg);
                                }
                            }
                            signItem.signedJar(signedJar);
                            String signingId = signingId(signItem);
                            detailsOutput.writeAnchorName(signingId, "Signing: " + signingId);
                            OutputAnalyzer signOA = signJar(signerInfo.jarsignerPath, certInfo.sigalg(), jarDigestAlgorithm, tsaDigestAlg, tsaUrl, certInfo.alias(), unsignedJar, signedJar);
                            Status signingStatus = signingStatus(signOA, tsaUrl != null);
                            signItem.status(signingStatus);
                            signItems.add(signItem);
                        }
                    }
                }
            }
        }
        return signItems;
    }

    private static List<SignItem> updating(List<SignItem> prevSignItems) throws IOException {
        List<SignItem> updateItems = new ArrayList<>();
        for (SignItem prevSign : prevSignItems) {
            updateItems.addAll(updateJar(prevSign));
        }
        return updateItems;
    }

    private static void verifying(SignItem signItem, VerifyItem verifyItem) throws Throwable {
        boolean delayVerify = verifyItem.status != Status.NONE;
        String verifyingId = verifyingId(signItem, verifyItem, delayVerify);
        detailsOutput.writeAnchorName(verifyingId, "Verifying: " + verifyingId);
        OutputAnalyzer verifyOA = verifyJar(verifyItem.jdkInfo.jarsignerPath, signItem.signedJar, verifyItem.certInfo == null ? null : verifyItem.certInfo.alias());
        Status verifyingStatus = verifyingStatus(signItem, verifyItem, verifyOA);
        try {
            String match = "^  (" + "  Signature algorithm: " + signItem.certInfo.expectedSigalg() + ", " + signItem.certInfo.expectedKeySize() + "-bit key" + ")|(" + "  Digest algorithm: " + signItem.expectedDigestAlg() + (signItem.tsaIndex < 0 ? "" : ")|(" + "Timestamped by \".+\" on .*" + ")|(" + "  Timestamp digest algorithm: " + signItem.expectedTsaDigestAlg() + ")|(" + "  Timestamp signature algorithm: .*") + ")$";
            verifyOA.stdoutShouldMatchByLine("^- Signed by \"CN=" + signItem.certInfo.toString().replaceAll("[.]", "[.]") + "\"$", "^(- Signed by \"CN=.+\")?$", match);
        } catch (Throwable e) {
            e.printStackTrace();
            verifyingStatus = Status.ERROR;
        }
        if (!delayVerify) {
            verifyItem.status(verifyingStatus);
        } else {
            verifyItem.delayStatus(verifyingStatus);
        }
        if (verifyItem.prevVerify != null) {
            verifying(signItem, verifyItem.prevVerify);
        }
    }

    private static Status signingStatus(OutputAnalyzer outputAnalyzer, boolean tsa) {
        if (outputAnalyzer.getExitValue() != 0) {
            return Status.ERROR;
        }
        if (!outputAnalyzer.getOutput().contains(Test.JAR_SIGNED)) {
            return Status.ERROR;
        }
        boolean warning = false;
        for (String line : outputAnalyzer.getOutput().lines().toArray(String[]::new)) {
            if (line.matches(Test.ERROR + " ?"))
                return Status.ERROR;
            if (line.matches(Test.WARNING + " ?"))
                warning = true;
        }
        return warning ? Status.WARNING : Status.NORMAL;
    }

    private static Status verifyingStatus(SignItem signItem, VerifyItem verifyItem, OutputAnalyzer outputAnalyzer) {
        List<String> expectedSignedContent = new ArrayList<>();
        if (verifyItem.certInfo == null) {
            expectedSignedContent.addAll(signItem.jarContents);
        } else {
            SignItem i = signItem;
            while (i != null) {
                if (i.certInfo != null && i.certInfo.equals(verifyItem.certInfo)) {
                    expectedSignedContent.addAll(i.jarContents);
                }
                i = i.prevSign;
            }
        }
        List<String> expectedUnsignedContent = new ArrayList<>(signItem.jarContents);
        expectedUnsignedContent.removeAll(expectedSignedContent);
        int expectedExitCode = !STRICT || expectedUnsignedContent.isEmpty() ? 0 : 32;
        if (outputAnalyzer.getExitValue() != expectedExitCode) {
            System.out.println("verifyingStatus: error: exit code != " + expectedExitCode + ": " + outputAnalyzer.getExitValue() + " != " + expectedExitCode);
            return Status.ERROR;
        }
        String expectedSuccessMessage = expectedUnsignedContent.isEmpty() ? Test.JAR_VERIFIED : Test.JAR_VERIFIED_WITH_SIGNER_ERRORS;
        if (!outputAnalyzer.getOutput().contains(expectedSuccessMessage)) {
            System.out.println("verifyingStatus: error: expectedSuccessMessage not found: " + expectedSuccessMessage);
            return Status.ERROR;
        }
        boolean tsa = signItem.tsaIndex >= 0;
        boolean warning = false;
        for (String line : outputAnalyzer.getOutput().lines().toArray(String[]::new)) {
            if (line.isBlank())
                continue;
            if (Test.JAR_VERIFIED.equals(line))
                continue;
            if (line.matches(Test.ERROR + " ?") && expectedExitCode == 0) {
                System.out.println("verifyingStatus: error: line.matches(" + Test.ERROR + "\" ?\"): " + line);
                return Status.ERROR;
            }
            if (line.matches(Test.WARNING + " ?")) {
                warning = true;
                continue;
            }
            if (!warning)
                continue;
            line = line.strip();
            if (Test.NOT_YET_VALID_CERT_SIGNING_WARNING.equals(line))
                continue;
            if (Test.HAS_EXPIRING_CERT_SIGNING_WARNING.equals(line))
                continue;
            if (Test.HAS_EXPIRING_CERT_VERIFYING_WARNING.equals(line))
                continue;
            if (line.matches("^" + Test.NO_TIMESTAMP_SIGNING_WARN_TEMPLATE.replaceAll("\\(%1\\$tY-%1\\$tm-%1\\$td\\)", "\\\\([^\\\\)]+\\\\)" + "( or after any future revocation date)?").replaceAll("[.]", "[.]") + "$") && !tsa)
                continue;
            if (line.matches("^" + Test.NO_TIMESTAMP_VERIFYING_WARN_TEMPLATE.replaceAll("\\(as early as %1\\$tY-%1\\$tm-%1\\$td\\)", "\\\\([^\\\\)]+\\\\)" + "( or after any future revocation date)?").replaceAll("[.]", "[.]") + "$") && !tsa)
                continue;
            if (line.matches("^This jar contains signatures that do(es)? not " + "include a timestamp[.] Without a timestamp, users may " + "not be able to validate this jar after the signer " + "certificate's expiration date \\([^\\)]+\\) or after " + "any future revocation date[.]") && !tsa)
                continue;
            if (Test.CERTIFICATE_SELF_SIGNED.equals(line))
                continue;
            if (Test.HAS_EXPIRED_CERT_VERIFYING_WARNING.equals(line) && signItem.certInfo.expired)
                continue;
            System.out.println("verifyingStatus: unexpected line: " + line);
            return Status.ERROR;
        }
        return warning ? Status.WARNING : Status.NORMAL;
    }

    private static OutputAnalyzer signJar(String jarsignerPath, String sigalg, String jarDigestAlgorithm, String tsadigestalg, String tsa, String alias, String unsignedJar, String signedJar) throws Throwable {
        List<String> arguments = new ArrayList<>();
        if (PROXY_HOST != null && PROXY_PORT != null) {
            arguments.add("-J-Dhttp.proxyHost=" + PROXY_HOST);
            arguments.add("-J-Dhttp.proxyPort=" + PROXY_PORT);
            arguments.add("-J-Dhttps.proxyHost=" + PROXY_HOST);
            arguments.add("-J-Dhttps.proxyPort=" + PROXY_PORT);
        }
        arguments.add("-J-Djava.security.properties=" + JAVA_SECURITY);
        arguments.add("-debug");
        arguments.add("-verbose");
        if (jarDigestAlgorithm != null) {
            arguments.add("-digestalg");
            arguments.add(jarDigestAlgorithm);
        }
        if (sigalg != null) {
            arguments.add("-sigalg");
            arguments.add(sigalg);
        }
        if (tsa != null) {
            arguments.add("-tsa");
            arguments.add(tsa);
        }
        if (tsadigestalg != null) {
            arguments.add("-tsadigestalg");
            arguments.add(tsadigestalg);
        }
        arguments.add("-keystore");
        arguments.add(KEYSTORE);
        arguments.add("-storepass");
        arguments.add(PASSWORD);
        arguments.add("-sigfile");
        arguments.add(nextSigfileName(alias, unsignedJar, signedJar));
        arguments.add("-signedjar");
        arguments.add(signedJar + ".jar");
        arguments.add(unsignedJar + ".jar");
        arguments.add(alias);
        OutputAnalyzer outputAnalyzer = execTool(jarsignerPath, arguments.toArray(new String[arguments.size()]));
        return outputAnalyzer;
    }

    private static OutputAnalyzer verifyJar(String jarsignerPath, String signedJar, String alias) throws Throwable {
        List<String> arguments = new ArrayList<>();
        arguments.add("-J-Djava.security.properties=" + JAVA_SECURITY);
        arguments.add("-debug");
        arguments.add("-verbose");
        arguments.add("-certs");
        arguments.add("-keystore");
        arguments.add(KEYSTORE);
        arguments.add("-verify");
        if (STRICT)
            arguments.add("-strict");
        arguments.add(signedJar + ".jar");
        if (alias != null)
            arguments.add(alias);
        OutputAnalyzer outputAnalyzer = execTool(jarsignerPath, arguments.toArray(new String[arguments.size()]));
        return outputAnalyzer;
    }

    private static boolean generateReport(List<TsaInfo> tsaList, List<SignItem> signItems) throws IOException {
        System.out.println("Report is being generated...");
        StringBuilder report = new StringBuilder();
        report.append(HtmlHelper.startHtml());
        report.append(HtmlHelper.startPre());
        report.append("TSA list:\n");
        for (TsaInfo tsaInfo : tsaList) {
            report.append(String.format("%d=%s%n", tsaInfo.index, tsaInfo.tsaUrl == null ? "notsa" : tsaInfo.tsaUrl));
        }
        report.append(HtmlHelper.endPre());
        report.append(HtmlHelper.startTable());
        List<String> headers = new ArrayList<>();
        headers.add("[Jarfile]");
        headers.add("[Signing Certificate]");
        headers.add("[Signer JDK]");
        headers.add("[Signature Algorithm]");
        headers.add("[Jar Digest Algorithm]");
        headers.add("[TSA Digest Algorithm]");
        headers.add("[TSA]");
        headers.add("[Signing Status]");
        headers.add("[Verifier JDK]");
        headers.add("[Verifying Certificate]");
        headers.add("[Verifying Status]");
        if (DELAY_VERIFY) {
            headers.add("[Delay Verifying Status]");
        }
        headers.add("[Failed]");
        report.append(HtmlHelper.htmlRow(headers.toArray(new String[headers.size()])));
        StringBuilder failedReport = new StringBuilder(report.toString());
        boolean failed = signItems.isEmpty();
        for (SignItem signItem : signItems) {
            failed = failed || signItem.verifyItems.isEmpty();
            for (VerifyItem verifyItem : signItem.verifyItems) {
                String reportRow = reportRow(signItem, verifyItem);
                report.append(reportRow);
                boolean isFailedCase = isFailed(signItem, verifyItem);
                if (isFailedCase) {
                    failedReport.append(reportRow);
                }
                failed = failed || isFailedCase;
            }
        }
        report.append(HtmlHelper.endTable());
        report.append(HtmlHelper.endHtml());
        generateFile("report.html", report.toString());
        if (failed) {
            failedReport.append(HtmlHelper.endTable());
            failedReport.append(HtmlHelper.endPre());
            failedReport.append(HtmlHelper.endHtml());
            generateFile("failedReport.html", failedReport.toString());
        }
        System.out.println("Report is generated.");
        return failed;
    }

    private static void generateFile(String path, String content) throws IOException {
        FileWriter writer = new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }

    private static String jarsignerPath(String jdkPath) {
        return jdkPath + "/bin/jarsigner";
    }

    private static String execJdkUtils(String jdkPath, String method, String... args) throws Throwable {
        String[] cmd = new String[args.length + 5];
        cmd[0] = jdkPath + "/bin/java";
        cmd[1] = "-cp";
        cmd[2] = TEST_CLASSES;
        cmd[3] = JdkUtils.class.getName();
        cmd[4] = method;
        System.arraycopy(args, 0, cmd, 5, args.length);
        return ProcessTools.executeCommand(cmd).getOutput();
    }

    private static OutputAnalyzer execTool(String toolPath, String... args) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            String[] cmd = new String[args.length + 4];
            cmd[0] = toolPath;
            cmd[1] = "-J-Duser.language=en";
            cmd[2] = "-J-Duser.country=US";
            cmd[3] = "-J-Djava.security.egd=file:/dev/./urandom";
            System.arraycopy(args, 0, cmd, 4, args.length);
            return ProcessTools.executeCommand(cmd);
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("child process duration [ms]: " + (end - start));
        }
    }

    private static class JdkInfo {

        private final String jdkPath;

        private final String jarsignerPath;

        private final String version;

        private final int majorVersion;

        private final boolean supportsTsadigestalg;

        private Map<String, Boolean> sigalgMap = new HashMap<>();

        private JdkInfo(String jdkPath) throws Throwable {
            this.jdkPath = jdkPath;
            version = execJdkUtils(jdkPath, JdkUtils.M_JAVA_RUNTIME_VERSION);
            if (version == null || version.isBlank()) {
                throw new RuntimeException("Cannot determine the JDK version: " + jdkPath);
            }
            majorVersion = Integer.parseInt((version.matches("^1[.].*") ? version.substring(2) : version).replaceAll("[^0-9].*$", ""));
            jarsignerPath = jarsignerPath(jdkPath);
            supportsTsadigestalg = execTool(jarsignerPath, "-help").getOutput().contains("-tsadigestalg");
        }

        private boolean isSupportedSigalg(String sigalg) throws Throwable {
            if (!sigalgMap.containsKey(sigalg)) {
                boolean isSupported = Boolean.parseBoolean(execJdkUtils(jdkPath, JdkUtils.M_IS_SUPPORTED_SIGALG, sigalg));
                sigalgMap.put(sigalg, isSupported);
            }
            return sigalgMap.get(sigalg);
        }

        private boolean isAtLeastMajorVersion(int minVersion) {
            return majorVersion >= minVersion;
        }

        private boolean supportsKeyAlg(String keyAlgorithm) {
            return isAtLeastMajorVersion(6) || !EC.equals(keyAlgorithm);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            JdkInfo other = (JdkInfo) obj;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "JdkInfo[" + version + ", " + jdkPath + "]";
        }
    }

    private static class TsaInfo {

        private final int index;

        private final String tsaUrl;

        private Set<String> digestList = new HashSet<>();

        private TsaInfo(int index, String tsa) {
            this.index = index;
            this.tsaUrl = tsa;
        }

        private void addDigest(String digest) {
            digestList.add(digest);
        }

        private boolean isDigestSupported(String digest) {
            return digest == null || digestList.isEmpty() || digestList.contains(digest);
        }

        @Override
        public String toString() {
            return "TsaInfo[" + index + ", " + tsaUrl + "]";
        }
    }

    private static class CertInfo {

        private static int certCounter;

        private final int nr = ++certCounter;

        private final JdkInfo jdkInfo;

        private final String keyAlgorithm;

        private final String digestAlgorithm;

        private final int keySize;

        private final boolean expired;

        private CertInfo(JdkInfo jdkInfo, String keyAlgorithm, String digestAlgorithm, int keySize, boolean expired) {
            this.jdkInfo = jdkInfo;
            this.keyAlgorithm = keyAlgorithm;
            this.digestAlgorithm = digestAlgorithm;
            this.keySize = keySize;
            this.expired = expired;
        }

        private String sigalg() {
            return DEFAULT.equals(digestAlgorithm) ? null : expectedSigalg();
        }

        private String expectedSigalg() {
            return (DEFAULT.equals(this.digestAlgorithm) ? this.digestAlgorithm : "SHA-256").replace("-", "") + "with" + keyAlgorithm + (EC.equals(keyAlgorithm) ? "DSA" : "");
        }

        private int expectedKeySize() {
            if (keySize != 0)
                return keySize;
            if (RSA.equals(keyAlgorithm) || DSA.equals(keyAlgorithm)) {
                return 2048;
            } else if (EC.equals(keyAlgorithm)) {
                return 256;
            } else {
                throw new RuntimeException("problem determining key size");
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (digestAlgorithm == null ? 0 : digestAlgorithm.hashCode());
            result = prime * result + (expired ? 1231 : 1237);
            result = prime * result + (jdkInfo == null ? 0 : jdkInfo.hashCode());
            result = prime * result + (keyAlgorithm == null ? 0 : keyAlgorithm.hashCode());
            result = prime * result + keySize;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CertInfo other = (CertInfo) obj;
            if (digestAlgorithm == null) {
                if (other.digestAlgorithm != null)
                    return false;
            } else if (!digestAlgorithm.equals(other.digestAlgorithm))
                return false;
            if (expired != other.expired)
                return false;
            if (jdkInfo == null) {
                if (other.jdkInfo != null)
                    return false;
            } else if (!jdkInfo.equals(other.jdkInfo))
                return false;
            if (keyAlgorithm == null) {
                if (other.keyAlgorithm != null)
                    return false;
            } else if (!keyAlgorithm.equals(other.keyAlgorithm))
                return false;
            if (keySize != other.keySize)
                return false;
            return true;
        }

        private String alias() {
            return (jdkInfo.version + "_" + toString()).toLowerCase(Locale.ENGLISH);
        }

        @Override
        public String toString() {
            return "nr" + nr + "_" + keyAlgorithm + "_" + digestAlgorithm + (keySize == 0 ? "" : "_" + keySize) + (expired ? "_Expired" : "");
        }
    }

    private static class TsaFilter {

        private static final Set<Condition> SET = new HashSet<>();

        private static boolean filter(String signerVersion, String digestAlgorithm, boolean expiredCert, int tsaIndex) {
            return !SET.add(new Condition(signerVersion, digestAlgorithm, expiredCert, tsaIndex));
        }

        private static class Condition {

            private final String signerVersion;

            private final String digestAlgorithm;

            private final boolean expiredCert;

            private final int tsaIndex;

            private Condition(String signerVersion, String digestAlgorithm, boolean expiredCert, int tsaIndex) {
                this.signerVersion = signerVersion;
                this.digestAlgorithm = digestAlgorithm;
                this.expiredCert = expiredCert;
                this.tsaIndex = tsaIndex;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((digestAlgorithm == null) ? 0 : digestAlgorithm.hashCode());
                result = prime * result + (expiredCert ? 1231 : 1237);
                result = prime * result + ((signerVersion == null) ? 0 : signerVersion.hashCode());
                result = prime * result + tsaIndex;
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                Condition other = (Condition) obj;
                if (digestAlgorithm == null) {
                    if (other.digestAlgorithm != null)
                        return false;
                } else if (!digestAlgorithm.equals(other.digestAlgorithm))
                    return false;
                if (expiredCert != other.expiredCert)
                    return false;
                if (signerVersion == null) {
                    if (other.signerVersion != null)
                        return false;
                } else if (!signerVersion.equals(other.signerVersion))
                    return false;
                if (tsaIndex != other.tsaIndex)
                    return false;
                return true;
            }
        }
    }

    private static enum Status {

        NONE, ERROR, WARNING, NORMAL
    }

    private static class SignItem {

        private SignItem prevSign;

        private CertInfo certInfo;

        private JdkInfo jdkInfo;

        private String digestAlgorithm;

        private String tsaDigestAlgorithm;

        private int tsaIndex;

        private Status status;

        private String unsignedJar;

        private String signedJar;

        private List<String> jarContents = new ArrayList<>();

        private List<VerifyItem> verifyItems = new ArrayList<>();

        private static SignItem build() {
            return new SignItem().addContentFiles(Arrays.asList("META-INF/MANIFEST.MF"));
        }

        private static SignItem build(SignItem prevSign) {
            return build().prevSign(prevSign).unsignedJar(prevSign.signedJar).addContentFiles(prevSign.jarContents);
        }

        private SignItem prevSign(SignItem prevSign) {
            this.prevSign = prevSign;
            return this;
        }

        private SignItem certInfo(CertInfo certInfo) {
            this.certInfo = certInfo;
            return this;
        }

        private SignItem jdkInfo(JdkInfo jdkInfo) {
            this.jdkInfo = jdkInfo;
            return this;
        }

        private SignItem digestAlgorithm(String digestAlgorithm) {
            this.digestAlgorithm = digestAlgorithm;
            return this;
        }

        String expectedDigestAlg() {
            return digestAlgorithm != null ? digestAlgorithm : "SHA-256";
        }

        private SignItem tsaDigestAlgorithm(String tsaDigestAlgorithm) {
            this.tsaDigestAlgorithm = tsaDigestAlgorithm;
            return this;
        }

        String expectedTsaDigestAlg() {
            return tsaDigestAlgorithm != null ? tsaDigestAlgorithm : "SHA-256";
        }

        private SignItem tsaIndex(int tsaIndex) {
            this.tsaIndex = tsaIndex;
            return this;
        }

        private SignItem status(Status status) {
            this.status = status;
            return this;
        }

        private SignItem unsignedJar(String unsignedJar) {
            this.unsignedJar = unsignedJar;
            return this;
        }

        private SignItem signedJar(String signedJar) {
            this.signedJar = signedJar;
            return this;
        }

        private SignItem addContentFiles(List<String> files) {
            this.jarContents.addAll(files);
            return this;
        }

        private void addVerifyItem(VerifyItem verifyItem) {
            verifyItems.add(verifyItem);
        }

        private boolean isErrorInclPrev() {
            if (prevSign != null && prevSign.isErrorInclPrev()) {
                System.out.println("SignItem.isErrorInclPrev: returning true from previous");
                return true;
            }
            return status == Status.ERROR;
        }

        private List<String> toStringWithPrev(Function<SignItem, String> toStr) {
            List<String> s = new ArrayList<>();
            if (prevSign != null) {
                s.addAll(prevSign.toStringWithPrev(toStr));
            }
            if (status != null) {
                s.add(toStr.apply(this));
            }
            return s;
        }
    }

    private static class VerifyItem {

        private VerifyItem prevVerify;

        private CertInfo certInfo;

        private JdkInfo jdkInfo;

        private Status status = Status.NONE;

        private Status delayStatus = Status.NONE;

        private static VerifyItem build(JdkInfo jdkInfo) {
            VerifyItem verifyItem = new VerifyItem();
            verifyItem.jdkInfo = jdkInfo;
            return verifyItem;
        }

        private VerifyItem certInfo(CertInfo certInfo) {
            this.certInfo = certInfo;
            return this;
        }

        private void addSignerCertInfos(SignItem signItem) {
            VerifyItem prevVerify = this;
            CertInfo lastCertInfo = null;
            while (signItem != null) {
                if (signItem.certInfo != null && !signItem.certInfo.equals(lastCertInfo)) {
                    lastCertInfo = signItem.certInfo;
                    prevVerify = prevVerify.prevVerify = build(jdkInfo).certInfo(signItem.certInfo);
                }
                signItem = signItem.prevSign;
            }
        }

        private VerifyItem status(Status status) {
            this.status = status;
            return this;
        }

        private boolean isErrorInclPrev() {
            if (prevVerify != null && prevVerify.isErrorInclPrev()) {
                System.out.println("VerifyItem.isErrorInclPrev: returning true from previous");
                return true;
            }
            return status == Status.ERROR || delayStatus == Status.ERROR;
        }

        private VerifyItem delayStatus(Status status) {
            this.delayStatus = status;
            return this;
        }

        private List<String> toStringWithPrev(Function<VerifyItem, String> toStr) {
            List<String> s = new ArrayList<>();
            if (prevVerify != null) {
                s.addAll(prevVerify.toStringWithPrev(toStr));
            }
            s.add(toStr.apply(this));
            return s;
        }
    }

    private static String signingId(SignItem signItem) {
        return signItem.signedJar;
    }

    private static String verifyingId(SignItem signItem, VerifyItem verifyItem, boolean delayVerify) {
        return signingId(signItem) + (delayVerify ? "-DV" : "-V") + "_" + verifyItem.jdkInfo.version + (verifyItem.certInfo == null ? "" : "_" + verifyItem.certInfo);
    }

    private static String reportRow(SignItem signItem, VerifyItem verifyItem) {
        List<String> values = new ArrayList<>();
        Consumer<Function<SignItem, String>> s_values_add = f -> {
            values.add(String.join("<br/><br/>", signItem.toStringWithPrev(f)));
        };
        Consumer<Function<VerifyItem, String>> v_values_add = f -> {
            values.add(String.join("<br/><br/>", verifyItem.toStringWithPrev(f)));
        };
        s_values_add.accept(i -> i.unsignedJar + " -> " + i.signedJar);
        s_values_add.accept(i -> i.certInfo.toString());
        s_values_add.accept(i -> i.jdkInfo.version);
        s_values_add.accept(i -> i.certInfo.expectedSigalg());
        s_values_add.accept(i -> null2Default(i.digestAlgorithm, i.expectedDigestAlg()));
        s_values_add.accept(i -> i.tsaIndex == -1 ? "" : null2Default(i.tsaDigestAlgorithm, i.expectedTsaDigestAlg()));
        s_values_add.accept(i -> i.tsaIndex == -1 ? "" : i.tsaIndex + "");
        s_values_add.accept(i -> HtmlHelper.anchorLink(PhaseOutputStream.fileName(PhaseOutputStream.Phase.SIGNING), signingId(i), "" + i.status));
        values.add(verifyItem.jdkInfo.version);
        v_values_add.accept(i -> i.certInfo == null ? "no alias" : "" + i.certInfo);
        v_values_add.accept(i -> HtmlHelper.anchorLink(PhaseOutputStream.fileName(PhaseOutputStream.Phase.VERIFYING), verifyingId(signItem, i, false), "" + i.status.toString()));
        if (DELAY_VERIFY) {
            v_values_add.accept(i -> HtmlHelper.anchorLink(PhaseOutputStream.fileName(PhaseOutputStream.Phase.DELAY_VERIFYING), verifyingId(signItem, verifyItem, true), verifyItem.delayStatus.toString()));
        }
        values.add(isFailed(signItem, verifyItem) ? "X" : "");
        return HtmlHelper.htmlRow(values.toArray(new String[values.size()]));
    }

    private static boolean isFailed(SignItem signItem, VerifyItem verifyItem) {
        System.out.println("isFailed: signItem = " + signItem + ", verifyItem = " + verifyItem);
        if (signItem.signedJar.startsWith("eofr") && !signItem.jdkInfo.isAtLeastMajorVersion(13) && !verifyItem.jdkInfo.isAtLeastMajorVersion(13))
            return false;
        boolean isFailed = signItem.isErrorInclPrev() || verifyItem.isErrorInclPrev();
        System.out.println("isFailed: returning " + isFailed);
        return isFailed;
    }

    private static String null2Default(String value, String defaultValue) {
        return value != null ? value : DEFAULT + "(" + (defaultValue == null ? "N/A" : defaultValue) + ")";
    }
}
