package sun.jkernel;

import java.io.*;
import java.net.URLStreamHandlerFactory;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;
import java.util.zip.*;
import sun.misc.BootClassLoaderHook;
import sun.misc.Launcher;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;

public class DownloadManager extends BootClassLoaderHook {

    public static final String KERNEL_DOWNLOAD_URL_PROPERTY = "kernel.download.url";

    public static final String KERNEL_DOWNLOAD_ENABLED_PROPERTY = "kernel.download.enabled";

    public static final String KERNEL_DOWNLOAD_DIALOG_PROPERTY = "kernel.download.dialog";

    public static final String KERNEL_DEBUG_PROPERTY = "kernel.debug";

    public static final String KERNEL_NOMERGE_PROPERTY = "kernel.nomerge";

    public static final String KERNEL_SIMULTANEOUS_DOWNLOADS_PROPERTY = "kernel.simultaneous.downloads";

    public static final int KERNEL_STATIC_MODTIME = 10000000;

    public static final String RESOURCE_URL = "internal-resource/";

    public static final String REQUESTED_BUNDLES_PATH = "lib" + File.separator + "bundles" + File.separator + "requested.list";

    private static final boolean disableDownloadDialog = "false".equals(System.getProperty(KERNEL_DOWNLOAD_DIALOG_PROPERTY));

    static boolean debug = "true".equals(System.getProperty(KERNEL_DEBUG_PROPERTY));

    private static OutputStream errorStream;

    private static OutputStream logStream;

    static String MUTEX_PREFIX;

    static boolean complete;

    private static int _isJBrokerStarted = -1;

    private static Properties bundleURLs;

    public static final String JAVA_HOME = System.getProperty("java.home");

    public static final String USER_HOME = System.getProperty("user.home");

    public static final String JAVA_VERSION = System.getProperty("java.version");

    static final int BUFFER_SIZE = 2048;

    static volatile boolean jkernelLibLoaded = false;

    public static String DEFAULT_DOWNLOAD_URL = "http://javadl.sun.com/webapps/download/GetList/" + System.getProperty("java.runtime.version") + "-kernel/windows-i586/";

    private static final String CUSTOM_PREFIX = "custom";

    private static final String KERNEL_PATH_SUFFIX = "-kernel";

    public static final String JAR_PATH_PROPERTY = "jarpath";

    public static final String SIZE_PROPERTY = "size";

    public static final String DEPENDENCIES_PROPERTY = "dependencies";

    public static final String INSTALL_PROPERTY = "install";

    private static boolean reportErrors = true;

    static final int ERROR_UNSPECIFIED = 0;

    static final int ERROR_DISK_FULL = 1;

    static final int ERROR_MALFORMED_BUNDLE_PROPERTIES = 2;

    static final int ERROR_DOWNLOADING_BUNDLE_PROPERTIES = 3;

    static final int ERROR_MALFORMED_URL = 4;

    static final int ERROR_RETRY_CANCELLED = 5;

    static final int ERROR_NO_SUCH_BUNDLE = 6;

    static ThreadLocal<Integer> downloading = new ThreadLocal<Integer>() {

        protected Integer initialValue() {
            return 0;
        }
    };

    private static File[] additionalBootStrapPaths = {};

    private static String[] bundleNames;

    private static String[] criticalBundleNames;

    private static String downloadURL;

    private static boolean visitorIdDetermined;

    private static String visitorId;

    public static String CHECK_VALUES_FILE = "check_value.properties";

    static String CHECK_VALUES_DIR = "sun/jkernel/";

    static String CHECK_VALUES_PATH = CHECK_VALUES_DIR + CHECK_VALUES_FILE;

    private static Map<String, Map<String, String>> bundleProperties;

    private static Map<String, String> resourceMap;

    private static Map<String, String> fileMap;

    private static boolean extDirDetermined;

    private static boolean extDirIncluded;

    static {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                if (debug)
                    println("DownloadManager startup");
                MUTEX_PREFIX = "jkernel";
                boolean downloadEnabled = !"false".equals(System.getProperty(KERNEL_DOWNLOAD_ENABLED_PROPERTY));
                complete = !getBundlePath().exists() || !downloadEnabled;
                if (!complete) {
                    loadJKernelLibrary();
                    log("Log opened");
                    if (isWindowsVista()) {
                        getLocalLowTempBundlePath().mkdirs();
                    }
                    new Thread() {

                        public void run() {
                            startBackgroundDownloads();
                        }
                    }.start();
                    try {
                        String dummyPath;
                        if (isWindowsVista()) {
                            dummyPath = USER_HOME + "\\appdata\\locallow\\dummy.kernel";
                        } else {
                            dummyPath = USER_HOME + "\\dummy.kernel";
                        }
                        File f = new File(dummyPath);
                        FileOutputStream out = new FileOutputStream(f, true);
                        out.close();
                        f.deleteOnExit();
                    } catch (IOException e) {
                        log(e);
                    }
                    new Thread("BundleDownloader") {

                        public void run() {
                            downloadRequestedBundles();
                        }
                    }.start();
                }
                return null;
            }
        });
    }

    static synchronized void loadJKernelLibrary() {
        if (!jkernelLibLoaded) {
            try {
                System.loadLibrary("jkernel");
                jkernelLibLoaded = true;
                debug = getDebugProperty();
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    static String appendTransactionId(String url) {
        StringBuilder result = new StringBuilder(url);
        String visitorId = DownloadManager.getVisitorId();
        if (visitorId != null) {
            if (url.indexOf("?") == -1)
                result.append('?');
            else
                result.append('&');
            result.append("transactionId=");
            result.append(DownloadManager.getVisitorId());
        }
        return result.toString();
    }

    static synchronized String getBaseDownloadURL() {
        if (downloadURL == null) {
            log("Determining download URL...");
            loadJKernelLibrary();
            downloadURL = System.getProperty(DownloadManager.KERNEL_DOWNLOAD_URL_PROPERTY);
            log("System property kernel.download.url = " + downloadURL);
            if (downloadURL == null) {
                downloadURL = getUrlFromRegistry();
                log("getUrlFromRegistry = " + downloadURL);
            }
            if (downloadURL == null)
                downloadURL = DEFAULT_DOWNLOAD_URL;
            log("Final download URL: " + downloadURL);
        }
        return downloadURL;
    }

    static Map<String, String> readTreeMap(InputStream rawIn) throws IOException {
        Map<String, String> result = new HashMap<String, String>();
        InputStream in = new BufferedInputStream(rawIn);
        List<String> tokens = new ArrayList<String>();
        StringBuilder currentToken = new StringBuilder();
        for (; ; ) {
            int c = in.read();
            if (c == -1)
                break;
            if (c < 32) {
                if (tokens.size() > 0) {
                    tokens.set(tokens.size() - 1, currentToken.toString());
                }
                currentToken.setLength(0);
                if (c > tokens.size()) {
                    throw new InternalError("current token level is " + (tokens.size() - 1) + " but encountered token " + "level " + c);
                } else if (c == tokens.size()) {
                    tokens.add(null);
                } else {
                    StringBuilder key = new StringBuilder();
                    for (int i = 1; i < tokens.size(); i++) {
                        if (i > 1)
                            key.append('/');
                        key.append(tokens.get(i));
                    }
                    result.put(key.toString(), tokens.get(0));
                    while (c < tokens.size()) tokens.remove(c);
                    tokens.add(null);
                }
            } else if (c < 254)
                currentToken.append((char) c);
            else if (c == 255)
                currentToken.append(".class");
            else {
                throw new InternalError("internal error processing " + "resource_map (can't-happen error)");
            }
        }
        if (tokens.size() > 0)
            tokens.set(tokens.size() - 1, currentToken.toString());
        StringBuilder key = new StringBuilder();
        for (int i = 1; i < tokens.size(); i++) {
            if (i > 1)
                key.append('/');
            key.append(tokens.get(i));
        }
        if (!tokens.isEmpty())
            result.put(key.toString(), tokens.get(0));
        in.close();
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> getResourceMap() throws IOException {
        if (resourceMap == null) {
            InputStream in = DownloadManager.class.getResourceAsStream("resource_map");
            if (in != null) {
                in = new BufferedInputStream(in);
                try {
                    resourceMap = readTreeMap(in);
                    in.close();
                } catch (IOException e) {
                    resourceMap = new HashMap<String, String>();
                    complete = true;
                    log("Can't find resource_map, forcing complete to true");
                }
                in.close();
            } else {
                resourceMap = new HashMap<String, String>();
                complete = true;
                log("Can't find resource_map, forcing complete to true");
            }
            for (int i = 1; ; i++) {
                String name = CUSTOM_PREFIX + i;
                File customPath = new File(getBundlePath(), name + ".jar");
                if (customPath.exists()) {
                    JarFile custom = new JarFile(customPath);
                    Enumeration entries = custom.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (!entry.isDirectory())
                            resourceMap.put(entry.getName(), name);
                    }
                } else
                    break;
            }
        }
        return resourceMap;
    }

    public static Map<String, String> getFileMap() throws IOException {
        if (fileMap == null) {
            InputStream in = DownloadManager.class.getResourceAsStream("file_map");
            if (in != null) {
                in = new BufferedInputStream(in);
                try {
                    fileMap = readTreeMap(in);
                    in.close();
                } catch (IOException e) {
                    fileMap = new HashMap<String, String>();
                    complete = true;
                    log("Can't find file_map, forcing complete to true");
                }
                in.close();
            } else {
                fileMap = new HashMap<String, String>();
                complete = true;
                log("Can't find file_map, forcing complete to true");
            }
        }
        return fileMap;
    }

    private static synchronized Map<String, Map<String, String>> getBundleProperties() throws IOException {
        if (bundleProperties == null) {
            InputStream in = DownloadManager.class.getResourceAsStream("bundle.properties");
            if (in == null) {
                complete = true;
                log("Can't find bundle.properties, forcing complete to true");
                return null;
            }
            in = new BufferedInputStream(in);
            Properties tmp = new Properties();
            tmp.load(in);
            bundleProperties = new HashMap<String, Map<String, String>>();
            for (Map.Entry e : tmp.entrySet()) {
                String key = (String) e.getKey();
                String[] properties = ((String) e.getValue()).split("\\|");
                Map<String, String> map = new HashMap<String, String>();
                for (String entry : properties) {
                    int equals = entry.indexOf("=");
                    if (equals == -1)
                        throw new InternalError("error parsing bundle.properties: " + entry);
                    map.put(entry.substring(0, equals).trim(), entry.substring(equals + 1).trim());
                }
                bundleProperties.put(key, map);
            }
            in.close();
        }
        return bundleProperties;
    }

    static String getBundleProperty(String bundleName, String property) {
        try {
            Map<String, Map<String, String>> props = getBundleProperties();
            Map map = props != null ? props.get(bundleName) : null;
            return map != null ? (String) map.get(property) : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String[] getBundleNames() throws IOException {
        if (bundleNames == null) {
            Set<String> result = new HashSet<String>();
            Map<String, String> resourceMap = getResourceMap();
            if (resourceMap != null)
                result.addAll(resourceMap.values());
            Map<String, String> fileMap = getFileMap();
            if (fileMap != null)
                result.addAll(fileMap.values());
            bundleNames = result.toArray(new String[result.size()]);
        }
        return bundleNames;
    }

    private static String[] getCriticalBundleNames() throws IOException {
        if (criticalBundleNames == null) {
            Set<String> result = new HashSet<String>();
            Map<String, String> fileMap = getFileMap();
            if (fileMap != null)
                result.addAll(fileMap.values());
            criticalBundleNames = result.toArray(new String[result.size()]);
        }
        return criticalBundleNames;
    }

    public static void send(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int c;
        while ((c = in.read(buffer)) > 0) out.write(buffer, 0, c);
    }

    static void performCompletionIfNeeded() {
        if (debug)
            log("DownloadManager.performCompletionIfNeeded: checking (" + complete + ", " + System.getProperty(KERNEL_NOMERGE_PROPERTY) + ")");
        if (complete || "true".equals(System.getProperty(KERNEL_NOMERGE_PROPERTY)))
            return;
        Bundle.loadReceipts();
        try {
            if (debug) {
                List critical = new ArrayList(Arrays.asList(getCriticalBundleNames()));
                critical.removeAll(Bundle.receipts);
                log("DownloadManager.performCompletionIfNeeded: still need " + critical.size() + " bundles (" + critical + ")");
            }
            if (Bundle.receipts.containsAll(Arrays.asList(getCriticalBundleNames()))) {
                log("DownloadManager.performCompletionIfNeeded: running");
                new Thread("JarMerger") {

                    public void run() {
                        createMergedJars();
                    }
                }.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Bundle getBundleForResource(String resource) throws IOException {
        String bundleName = getResourceMap().get(resource);
        return bundleName != null ? Bundle.getBundle(bundleName) : null;
    }

    private static Bundle getBundleForFile(String file) throws IOException {
        String bundleName = getFileMap().get(file);
        return bundleName != null ? Bundle.getBundle(bundleName) : null;
    }

    static File getBundlePath() {
        return new File(JAVA_HOME, "lib" + File.separatorChar + "bundles");
    }

    private static String getAppDataLocalLow() {
        return USER_HOME + "\\appdata\\locallow\\";
    }

    public static String getKernelJREDir() {
        return "kerneljre" + JAVA_VERSION;
    }

    static File getLocalLowTempBundlePath() {
        return new File(getLocalLowKernelJava() + "-bundles");
    }

    static String getLocalLowKernelJava() {
        return getAppDataLocalLow() + getKernelJREDir();
    }

    private static void addEntryToBootClassPath(File path) {
        synchronized (Launcher.class) {
            synchronized (DownloadManager.class) {
                File[] newBootStrapPaths = new File[additionalBootStrapPaths.length + 1];
                System.arraycopy(additionalBootStrapPaths, 0, newBootStrapPaths, 0, additionalBootStrapPaths.length);
                newBootStrapPaths[newBootStrapPaths.length - 1] = path;
                additionalBootStrapPaths = newBootStrapPaths;
                if (bootstrapClassPath != null)
                    bootstrapClassPath.addURL(getFileURL(path));
            }
        }
    }

    private static URLClassPath bootstrapClassPath = null;

    private synchronized static URLClassPath getBootClassPath(URLClassPath bcp, URLStreamHandlerFactory factory) {
        if (bootstrapClassPath == null) {
            bootstrapClassPath = new URLClassPath(bcp.getURLs(), factory);
            for (File path : additionalBootStrapPaths) {
                bootstrapClassPath.addURL(getFileURL(path));
            }
        }
        return bootstrapClassPath;
    }

    private static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
        }
        try {
            return ParseUtil.fileToEncodedURL(file);
        } catch (MalformedURLException e) {
            throw new InternalError();
        }
    }

    private static synchronized boolean extDirIsIncluded() {
        if (!extDirDetermined) {
            extDirDetermined = true;
            String raw = System.getProperty("java.ext.dirs");
            String ext = JAVA_HOME + File.separator + "lib" + File.separator + "ext";
            int index = 0;
            while (index < raw.length()) {
                int newIndex = raw.indexOf(File.pathSeparator, index);
                if (newIndex == -1)
                    newIndex = raw.length();
                String path = raw.substring(index, newIndex);
                if (path.equals(ext)) {
                    extDirIncluded = true;
                    break;
                }
                index = newIndex + 1;
            }
        }
        return extDirIncluded;
    }

    private static String doGetBootClassPathEntryForResource(String resourceName) {
        boolean retry = false;
        do {
            Bundle bundle = null;
            try {
                bundle = getBundleForResource(resourceName);
                if (bundle != null) {
                    File path = bundle.getJarPath();
                    boolean isExt = path.getParentFile().getName().equals("ext");
                    if (isExt && !extDirIsIncluded())
                        return null;
                    if (getBundleProperty(bundle.getName(), JAR_PATH_PROPERTY) == null) {
                        Bundle merged = Bundle.getBundle("merged");
                        if (merged != null && merged.isInstalled()) {
                            File jar;
                            if (resourceName.endsWith(".class"))
                                jar = merged.getJarPath();
                            else
                                jar = new File(merged.getJarPath().getPath().replaceAll("merged-rt.jar", "merged-resources.jar"));
                            addEntryToBootClassPath(jar);
                            return jar.getPath();
                        }
                    }
                    if (!bundle.isInstalled()) {
                        bundle.queueDependencies(true);
                        log("On-demand downloading " + bundle.getName() + " for resource " + resourceName + "...");
                        bundle.install();
                        log(bundle + " install finished.");
                    }
                    log("Double-checking " + bundle + " state...");
                    if (!bundle.isInstalled()) {
                        throw new IllegalStateException("Expected state of " + bundle + " to be INSTALLED");
                    }
                    if (isExt) {
                        Launcher.addURLToExtClassLoader(path.toURL());
                        return null;
                    }
                    if ("javaws".equals(bundle.getName())) {
                        Launcher.addURLToAppClassLoader(path.toURL());
                        log("Returning null for javaws");
                        return null;
                    }
                    if ("core".equals(bundle.getName()))
                        return null;
                    addEntryToBootClassPath(path);
                    return path.getPath();
                }
                return null;
            } catch (Throwable e) {
                retry = handleException(e);
                log("Error downloading bundle for " + resourceName + ":");
                log(e);
                if (e instanceof IOException) {
                    if (bundle != null) {
                        if (bundle.getJarPath() != null) {
                            File packTmp = new File(bundle.getJarPath() + ".pack");
                            packTmp.delete();
                            bundle.getJarPath().delete();
                        }
                        if (bundle.getLocalPath() != null) {
                            bundle.getLocalPath().delete();
                        }
                        bundle.setState(Bundle.NOT_DOWNLOADED);
                    }
                }
            }
        } while (retry);
        sendErrorPing(ERROR_RETRY_CANCELLED);
        return null;
    }

    static synchronized void sendErrorPing(int code) {
        try {
            File bundlePath;
            if (isWindowsVista()) {
                bundlePath = getLocalLowTempBundlePath();
            } else {
                bundlePath = getBundlePath();
            }
            File tmp = new File(bundlePath, "tmp");
            File errors = new File(tmp, "errors");
            String errorString = String.valueOf(code);
            if (errors.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(errors));
                String line = in.readLine();
                while (line != null) {
                    if (line.equals(errorString))
                        return;
                    line = in.readLine();
                }
            }
            tmp.mkdirs();
            Writer out = new FileWriter(errors, true);
            out.write(errorString + System.getProperty("line.separator"));
            out.close();
            postDownloadError(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean handleException(Throwable e) {
        if (e instanceof IOException) {
            int code = ERROR_UNSPECIFIED;
            if (e.getMessage().indexOf("not enough space") != -1)
                code = ERROR_DISK_FULL;
            return askUserToRetryDownloadOrQuit(code);
        } else
            return false;
    }

    static synchronized void flushBundleURLs() {
        bundleURLs = null;
    }

    static synchronized Properties getBundleURLs(boolean showUI) throws IOException {
        if (bundleURLs == null) {
            log("Entering DownloadManager.getBundleURLs");
            String base = getBaseDownloadURL();
            String url = appendTransactionId(base);
            File bundlePath = null;
            if (isWindowsVista()) {
                bundlePath = getLocalLowTempBundlePath();
            } else {
                bundlePath = getBundlePath();
            }
            File tmp = new File(bundlePath, "urls." + getCurrentProcessId() + ".properties");
            try {
                log("Downloading from " + url + " to " + tmp);
                downloadFromURL(url, tmp, "", showUI);
                bundleURLs = new Properties();
                if (tmp.exists()) {
                    addToTotalDownloadSize((int) tmp.length());
                    InputStream in = new FileInputStream(tmp);
                    in = new BufferedInputStream(in);
                    bundleURLs.load(in);
                    in.close();
                    if (bundleURLs.isEmpty()) {
                        fatalError(ERROR_MALFORMED_BUNDLE_PROPERTIES);
                    }
                } else {
                    fatalError(ERROR_DOWNLOADING_BUNDLE_PROPERTIES);
                }
            } finally {
                if (!debug)
                    tmp.delete();
            }
            log("Leaving DownloadManager.getBundleURLs");
        }
        return bundleURLs;
    }

    public static String getBootClassPathEntryForResource(final String resourceName) {
        if (debug)
            log("Entering getBootClassPathEntryForResource(" + resourceName + ")");
        if (isJREComplete() || downloading == null || resourceName.startsWith("sun/jkernel")) {
            if (debug)
                log("Bailing: " + isJREComplete() + ", " + (downloading == null));
            return null;
        }
        incrementDownloadCount();
        try {
            String result = (String) AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    return (String) doGetBootClassPathEntryForResource(resourceName);
                }
            });
            log("getBootClassPathEntryForResource(" + resourceName + ") == " + result);
            return result;
        } finally {
            decrementDownloadCount();
        }
    }

    public static String getBootClassPathEntryForClass(final String className) {
        return getBootClassPathEntryForResource(className.replace('.', '/') + ".class");
    }

    private static boolean doDownloadFile(String relativePath) throws IOException {
        Bundle bundle = getBundleForFile(relativePath);
        if (bundle != null) {
            bundle.queueDependencies(true);
            log("On-demand downloading " + bundle.getName() + " for file " + relativePath + "...");
            bundle.install();
            return true;
        }
        return false;
    }

    public static boolean downloadFile(final String relativePath) throws IOException {
        if (isJREComplete() || downloading == null)
            return false;
        incrementDownloadCount();
        try {
            Object result = AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    File path = new File(JAVA_HOME, relativePath.replace('/', File.separatorChar));
                    if (path.exists())
                        return true;
                    try {
                        return new Boolean(doDownloadFile(relativePath));
                    } catch (IOException e) {
                        return e;
                    }
                }
            });
            if (result instanceof Boolean)
                return ((Boolean) result).booleanValue();
            else
                throw (IOException) result;
        } finally {
            decrementDownloadCount();
        }
    }

    static void incrementDownloadCount() {
        downloading.set(downloading.get() + 1);
    }

    static void decrementDownloadCount() {
        downloading.set(downloading.get() - 1);
    }

    public static boolean isCurrentThreadDownloading() {
        return downloading != null ? downloading.get() > 0 : false;
    }

    public static boolean isJREComplete() {
        return complete;
    }

    static void doBackgroundDownloads(boolean showProgress) {
        if (!complete) {
            if (!showProgress && !debug)
                reportErrors = false;
            try {
                Bundle swing = Bundle.getBundle("javax_swing_core");
                if (!swing.isInstalled())
                    swing.install(showProgress, false, false);
                for (String name : getCriticalBundleNames()) {
                    Bundle bundle = Bundle.getBundle(name);
                    if (!bundle.isInstalled()) {
                        bundle.install(showProgress, false, true);
                    }
                }
                shutdown();
            } catch (IOException e) {
                log(e);
            }
        }
    }

    static void copyReceiptFile(File from, File to) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(from)));
        OutputStream out = new FileOutputStream(to);
        String line = in.readLine();
        while (line != null) {
            out.write((line + '\n').getBytes("utf-8"));
            line = in.readLine();
        }
        in.close();
        out.close();
    }

    private static void downloadRequestedBundles() {
        log("Checking for requested bundles...");
        try {
            File list = new File(JAVA_HOME, REQUESTED_BUNDLES_PATH);
            if (list.exists()) {
                FileInputStream in = new FileInputStream(list);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                send(in, buffer);
                in.close();
                String raw = new String(buffer.toByteArray(), "utf-8");
                List bundles = new ArrayList();
                StringBuilder token = new StringBuilder();
                for (int i = 0; i < raw.length(); i++) {
                    char c = raw.charAt(i);
                    if (c == ',' || Character.isWhitespace(c)) {
                        if (token.length() > 0) {
                            bundles.add(token.toString());
                            token.setLength(0);
                        }
                    } else
                        token.append(c);
                }
                if (token.length() > 0)
                    bundles.add(token.toString());
                log("Requested bundles: " + bundles);
                for (int i = 0; i < bundles.size(); i++) {
                    Bundle bundle = Bundle.getBundle((String) bundles.get(i));
                    if (bundle != null && !bundle.isInstalled()) {
                        log("Downloading " + bundle + " due to requested.list");
                        bundle.install(true, false, false);
                    }
                }
            }
        } catch (IOException e) {
            log(e);
        }
    }

    static void fatalError(int code) {
        fatalError(code, null);
    }

    static void fatalError(int code, String arg) {
        sendErrorPing(code);
        for (int i = 0; i < Bundle.THREADS; i++) bundleInstallComplete();
        if (reportErrors)
            displayError(code, arg);
        boolean inPlugIn = (Boolean.getBoolean("java.awt.headless") || System.getProperty("javaplugin.version") != null);
        KernelError error = new KernelError("Java Kernel bundle download failed");
        if (inPlugIn)
            throw error;
        else {
            log(error);
            System.exit(1);
        }
    }

    private static void startBackgroundDownloadWithBroker() {
        if (!BackgroundDownloader.getBackgroundDownloadProperty()) {
            return;
        }
        if (!launchBrokerProcess()) {
            return;
        }
        String kernelDownloadURLProperty = getBaseDownloadURL();
        String kernelDownloadURL;
        if (kernelDownloadURLProperty == null || kernelDownloadURLProperty.equals(DEFAULT_DOWNLOAD_URL)) {
            kernelDownloadURL = " ";
        } else {
            kernelDownloadURL = kernelDownloadURLProperty;
        }
        startBackgroundDownloadWithBrokerImpl(kernelDownloadURLProperty);
    }

    private static void startBackgroundDownloads() {
        if (!complete) {
            if (BackgroundDownloader.getBackgroundMutex().acquire(0)) {
                BackgroundDownloader.getBackgroundMutex().release();
                if (isWindowsVista()) {
                    startBackgroundDownloadWithBroker();
                } else {
                    BackgroundDownloader.startBackgroundDownloads();
                }
            }
        }
    }

    static native void addToTotalDownloadSize(int size);

    static void downloadFromURL(String url, File file, String name, boolean showProgress) {
        downloadFromURLImpl(url, file, name, disableDownloadDialog ? false : showProgress);
    }

    private static native void downloadFromURLImpl(String url, File file, String name, boolean showProgress);

    static native String getUrlFromRegistry();

    static native String getVisitorId0();

    static native void postDownloadComplete();

    static native void postDownloadError(int code);

    static synchronized String getVisitorId() {
        if (!visitorIdDetermined) {
            visitorIdDetermined = true;
            visitorId = getVisitorId0();
        }
        return visitorId;
    }

    public static native void displayError(int code, String arg);

    public static native boolean askUserToRetryDownloadOrQuit(int code);

    static native boolean isWindowsVista();

    private static native void startBackgroundDownloadWithBrokerImpl(String command);

    private static int isJBrokerStarted() {
        if (_isJBrokerStarted == -1) {
            _isJBrokerStarted = isJBrokerRunning() ? 1 : 0;
        }
        return _isJBrokerStarted;
    }

    private static native boolean isJBrokerRunning();

    private static native boolean isIEProtectedMode();

    private static native boolean launchJBroker(String jbrokerPath);

    static native void bundleInstallStart();

    static native void bundleInstallComplete();

    private static native boolean moveFileWithBrokerImpl(String fromPath, String userHome);

    private static native boolean moveDirWithBrokerImpl(String fromPath, String userHome);

    static boolean moveFileWithBroker(String fromPath) {
        if (!launchBrokerProcess()) {
            return false;
        }
        return moveFileWithBrokerImpl(fromPath, USER_HOME);
    }

    static boolean moveDirWithBroker(String fromPath) {
        if (!launchBrokerProcess()) {
            return false;
        }
        return moveDirWithBrokerImpl(fromPath, USER_HOME);
    }

    private static synchronized boolean launchBrokerProcess() {
        if (isJBrokerStarted() == 0) {
            boolean ret = launchJBroker(JAVA_HOME);
            _isJBrokerStarted = ret ? 1 : 0;
            return ret;
        }
        return true;
    }

    private static class StreamMonitor implements Runnable {

        private InputStream istream;

        public StreamMonitor(InputStream stream) {
            istream = new BufferedInputStream(stream);
            new Thread(this).start();
        }

        public void run() {
            byte[] buffer = new byte[4096];
            try {
                int ret = istream.read(buffer);
                while (ret != -1) {
                    ret = istream.read(buffer);
                }
            } catch (IOException e) {
                try {
                    istream.close();
                } catch (IOException e2) {
                }
            }
        }
    }

    private static void copyAll(File src, File dest, Set excludes) throws IOException {
        if (!excludes.contains(src.getName())) {
            if (src.isDirectory()) {
                File[] children = src.listFiles();
                if (children != null) {
                    for (int i = 0; i < children.length; i++) copyAll(children[i], new File(dest, children[i].getName()), excludes);
                }
            } else {
                dest.getParentFile().mkdirs();
                FileInputStream in = new FileInputStream(src);
                FileOutputStream out = new FileOutputStream(dest);
                send(in, out);
                in.close();
                out.close();
            }
        }
    }

    public static void dumpOutput(final Process p) {
        Thread outputReader = new Thread("outputReader") {

            public void run() {
                try {
                    InputStream in = p.getInputStream();
                    DownloadManager.send(in, System.out);
                } catch (IOException e) {
                    log(e);
                }
            }
        };
        outputReader.start();
        Thread errorReader = new Thread("errorReader") {

            public void run() {
                try {
                    InputStream in = p.getErrorStream();
                    DownloadManager.send(in, System.err);
                } catch (IOException e) {
                    log(e);
                }
            }
        };
        errorReader.start();
    }

    private static void createMergedJars() {
        log("DownloadManager.createMergedJars");
        File bundlePath;
        if (isWindowsVista()) {
            bundlePath = getLocalLowTempBundlePath();
        } else {
            bundlePath = getBundlePath();
        }
        File tmp = new File(bundlePath, "tmp");
        if (new File(getBundlePath(), "tmp" + File.separator + "finished").exists())
            return;
        log("DownloadManager.createMergedJars: running");
        tmp.mkdirs();
        boolean retry = false;
        do {
            try {
                Bundle.getBundle("merged").install(false, false, true);
                postDownloadComplete();
                File finished = new File(tmp, "finished");
                new FileOutputStream(finished).close();
                if (isWindowsVista()) {
                    if (!moveFileWithBroker(getKernelJREDir() + "-bundles\\tmp\\finished")) {
                        throw new IOException("unable to create 'finished' file");
                    }
                }
                log("DownloadManager.createMergedJars: created " + finished);
                if (isWindowsVista()) {
                    File tmpDir = getLocalLowTempBundlePath();
                    File[] list = tmpDir.listFiles();
                    if (list != null) {
                        for (int i = 0; i < list.length; i++) {
                            list[i].delete();
                        }
                    }
                    tmpDir.delete();
                    log("Finished cleanup, " + tmpDir + ".exists(): " + tmpDir.exists());
                }
            } catch (IOException e) {
                log(e);
            }
        } while (retry);
        log("DownloadManager.createMergedJars: finished");
    }

    private static void shutdown() {
        try {
            ExecutorService e = Bundle.getThreadPool();
            e.shutdown();
            e.awaitTermination(60 * 60 * 24, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

    static native boolean getDebugKey();

    public static boolean getDebugProperty() {
        boolean debugEnabled = getDebugKey();
        if (System.getProperty(KERNEL_DEBUG_PROPERTY) != null) {
            debugEnabled = Boolean.valueOf(System.getProperty(KERNEL_DEBUG_PROPERTY));
        }
        return debugEnabled;
    }

    static void println(String msg) {
        if (System.err != null)
            System.err.println(msg);
        else {
            try {
                if (errorStream == null)
                    errorStream = new FileOutputStream(FileDescriptor.err);
                errorStream.write((msg + System.getProperty("line.separator")).getBytes("utf-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void log(String msg) {
        if (debug) {
            println(msg);
            try {
                if (logStream == null) {
                    loadJKernelLibrary();
                    File path = isWindowsVista() ? getLocalLowTempBundlePath() : getBundlePath();
                    path = new File(path, "kernel." + getCurrentProcessId() + ".log");
                    logStream = new FileOutputStream(path);
                }
                logStream.write((msg + System.getProperty("line.separator")).getBytes("utf-8"));
                logStream.flush();
            } catch (IOException e) {
            }
        }
    }

    static void log(Throwable e) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(buffer);
        e.printStackTrace(p);
        p.close();
        log(buffer.toString(0));
    }

    private static void printMap(Map map) {
        int size = 0;
        Set<Integer> identityHashes = new HashSet<Integer>();
        Iterator i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            System.out.println(key + ": " + value);
            Integer keyHash = Integer.valueOf(System.identityHashCode(key));
            if (!identityHashes.contains(keyHash)) {
                identityHashes.add(keyHash);
                size += key.length();
            }
            Integer valueHash = Integer.valueOf(System.identityHashCode(value));
            if (!identityHashes.contains(valueHash)) {
                identityHashes.add(valueHash);
                size += value.length();
            }
        }
        System.out.println(size + " bytes");
    }

    private static void dumpMaps() throws IOException {
        System.out.println("Resources:");
        System.out.println("----------");
        printMap(getResourceMap());
        System.out.println();
        System.out.println("Files:");
        System.out.println("----------");
        printMap(getFileMap());
    }

    private static void processDownload(String bundleName) throws IOException {
        if (bundleName.equals("all")) {
            debug = true;
            doBackgroundDownloads(true);
            performCompletionIfNeeded();
        } else {
            Bundle bundle = Bundle.getBundle(bundleName);
            if (bundle == null) {
                println("Unknown bundle: " + bundleName);
                System.exit(1);
            } else
                bundle.install();
        }
    }

    static native int getCurrentProcessId();

    private DownloadManager() {
    }

    static void setBootClassLoaderHook() {
        if (!isJREComplete()) {
            sun.misc.BootClassLoaderHook.setHook(new DownloadManager());
        }
    }

    public String loadBootstrapClass(String name) {
        return DownloadManager.getBootClassPathEntryForClass(name);
    }

    public boolean loadLibrary(String name) {
        try {
            if (!DownloadManager.isJREComplete() && !DownloadManager.isCurrentThreadDownloading()) {
                return DownloadManager.downloadFile("bin/" + System.mapLibraryName(name));
            }
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Error downloading library " + name + ": " + e);
        } catch (NoClassDefFoundError e) {
        }
        return false;
    }

    public boolean prefetchFile(String name) {
        try {
            return sun.jkernel.DownloadManager.downloadFile(name);
        } catch (IOException ioe) {
            return false;
        }
    }

    public String getBootstrapResource(String name) {
        try {
            return DownloadManager.getBootClassPathEntryForResource(name);
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    public URLClassPath getBootstrapClassPath(URLClassPath bcp, URLStreamHandlerFactory factory) {
        return DownloadManager.getBootClassPath(bcp, factory);
    }

    public boolean isCurrentThreadPrefetching() {
        return DownloadManager.isCurrentThreadDownloading();
    }

    public static void main(String[] arg) throws Exception {
        AccessController.checkPermission(new AllPermission());
        boolean valid = false;
        if (arg.length == 2 && arg[0].equals("-install")) {
            valid = true;
            Bundle bundle = new Bundle() {

                protected void updateState() {
                    state = DOWNLOADED;
                }
            };
            File jarPath;
            int index = 0;
            do {
                index++;
                jarPath = new File(getBundlePath(), CUSTOM_PREFIX + index + ".jar");
            } while (jarPath.exists());
            bundle.setName(CUSTOM_PREFIX + index);
            bundle.setLocalPath(new File(arg[1]));
            bundle.setJarPath(jarPath);
            bundle.setDeleteOnInstall(false);
            bundle.install();
        } else if (arg.length == 2 && arg[0].equals("-download")) {
            valid = true;
            processDownload(arg[1]);
        } else if (arg.length == 1 && arg[0].equals("-dumpmaps")) {
            valid = true;
            dumpMaps();
        } else if (arg.length == 2 && arg[0].equals("-sha1")) {
            valid = true;
            System.out.println(BundleCheck.getInstance(new File(arg[1])));
        } else if (arg.length == 1 && arg[0].equals("-downloadtest")) {
            valid = true;
            File file = File.createTempFile("download", ".test");
            for (; ; ) {
                file.delete();
                downloadFromURL(getBaseDownloadURL(), file, "URLS", true);
                System.out.println("Downloaded " + file.length() + " bytes");
            }
        }
        if (!valid) {
            System.out.println("usage: DownloadManager -install <path>.zip |");
            System.out.println("       DownloadManager -download " + "<bundle_name> |");
            System.out.println("       DownloadManager -dumpmaps");
            System.exit(1);
        }
    }
}
