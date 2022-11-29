package sun.jkernel;

import java.io.*;
import java.net.HttpRetryException;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;
import java.util.zip.GZIPInputStream;

public class Bundle {

    static {
        if (!DownloadManager.jkernelLibLoaded) {
            System.loadLibrary("jkernel");
        }
    }

    public static native boolean extraCompress(String srcPath, String destPath) throws IOException;

    public static native boolean extraUncompress(String srcPath, String destPath) throws IOException;

    private static final String BUNDLE_JAR_ENTRY_NAME = "classes.jar";

    protected static final int NOT_DOWNLOADED = 0;

    protected static final int QUEUED = 1;

    protected static final int DOWNLOADED = 2;

    protected static final int INSTALLED = 3;

    private static ExecutorService threadPool;

    static final int THREADS;

    static {
        String downloads = System.getProperty(DownloadManager.KERNEL_SIMULTANEOUS_DOWNLOADS_PROPERTY);
        if (downloads != null)
            THREADS = Integer.parseInt(downloads.trim());
        else
            THREADS = 1;
    }

    private static Mutex receiptsMutex;

    private static Map<String, Bundle> bundles = new HashMap<String, Bundle>();

    static Set<String> receipts = new HashSet<String>();

    private static int bytesDownloaded;

    private static File receiptPath = new File(DownloadManager.getBundlePath(), "receipts");

    private static int receiptsSize;

    private String name;

    private File localPath;

    private File jarPath;

    private File lowJarPath;

    private File lowJavaPath = null;

    protected int state;

    protected boolean deleteOnInstall = true;

    private static Mutex getReceiptsMutex() {
        if (receiptsMutex == null)
            receiptsMutex = Mutex.create(DownloadManager.MUTEX_PREFIX + "receipts");
        return receiptsMutex;
    }

    static synchronized void loadReceipts() {
        getReceiptsMutex().acquire();
        try {
            if (receiptPath.exists()) {
                int size = (int) receiptPath.length();
                if (size != receiptsSize) {
                    DataInputStream in = null;
                    try {
                        receipts.clear();
                        for (String bundleName : DownloadManager.getBundleNames()) {
                            if ("true".equals(DownloadManager.getBundleProperty(bundleName, DownloadManager.INSTALL_PROPERTY)))
                                receipts.add(bundleName);
                        }
                        if (receiptPath.exists()) {
                            in = new DataInputStream(new BufferedInputStream(new FileInputStream(receiptPath)));
                            String line;
                            while ((line = in.readLine()) != null) {
                                receipts.add(line.trim());
                            }
                        }
                        receiptsSize = size;
                    } catch (IOException e) {
                        DownloadManager.log(e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ioe) {
                                DownloadManager.log(ioe);
                            }
                        }
                    }
                }
            }
        } finally {
            getReceiptsMutex().release();
        }
    }

    public static synchronized Bundle getBundle(String bundleId) throws IOException {
        Bundle result = (Bundle) bundles.get(bundleId);
        if (result == null && (bundleId.equals("merged") || Arrays.asList(DownloadManager.getBundleNames()).contains(bundleId))) {
            result = new Bundle();
            result.name = bundleId;
            if (DownloadManager.isWindowsVista()) {
                result.localPath = new File(DownloadManager.getLocalLowTempBundlePath(), bundleId + ".zip");
                result.lowJavaPath = new File(DownloadManager.getLocalLowKernelJava() + bundleId);
            } else {
                result.localPath = new File(DownloadManager.getBundlePath(), bundleId + ".zip");
            }
            String jarPath = DownloadManager.getBundleProperty(bundleId, DownloadManager.JAR_PATH_PROPERTY);
            if (jarPath != null) {
                if (DownloadManager.isWindowsVista()) {
                    result.lowJarPath = new File(DownloadManager.getLocalLowKernelJava() + bundleId, jarPath);
                }
                result.jarPath = new File(DownloadManager.JAVA_HOME, jarPath);
            } else {
                if (DownloadManager.isWindowsVista()) {
                    result.lowJarPath = new File(DownloadManager.getLocalLowKernelJava() + bundleId + "\\lib\\bundles", bundleId + ".jar");
                }
                result.jarPath = new File(DownloadManager.getBundlePath(), bundleId + ".jar");
            }
            bundles.put(bundleId, result);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getLocalPath() {
        return localPath;
    }

    public void setLocalPath(File localPath) {
        this.localPath = localPath;
    }

    public File getJarPath() {
        return jarPath;
    }

    public void setJarPath(File jarPath) {
        this.jarPath = jarPath;
    }

    public int getSize() {
        return Integer.valueOf(DownloadManager.getBundleProperty(getName(), DownloadManager.SIZE_PROPERTY));
    }

    public boolean getDeleteOnInstall() {
        return deleteOnInstall;
    }

    public void setDeleteOnInstall(boolean deleteOnInstall) {
        this.deleteOnInstall = deleteOnInstall;
    }

    protected void updateState() {
        synchronized (Bundle.class) {
            loadReceipts();
            if (receipts.contains(name) || "true".equals(DownloadManager.getBundleProperty(name, DownloadManager.INSTALL_PROPERTY)))
                state = Bundle.INSTALLED;
            else if (localPath.exists())
                state = Bundle.DOWNLOADED;
        }
    }

    private String getURL(boolean showUI) throws IOException {
        Properties urls = DownloadManager.getBundleURLs(showUI);
        String result = urls.getProperty(name + ".zip");
        if (result == null) {
            result = urls.getProperty(name);
            if (result == null) {
                DownloadManager.log("Unable to determine bundle URL for " + this);
                DownloadManager.log("Bundle URLs: " + urls);
                DownloadManager.sendErrorPing(DownloadManager.ERROR_NO_SUCH_BUNDLE);
                throw new NullPointerException("Unable to determine URL " + "for bundle: " + this);
            }
        }
        return result;
    }

    private void download(boolean showProgress) {
        if (DownloadManager.isJREComplete())
            return;
        Mutex mutex = Mutex.create(DownloadManager.MUTEX_PREFIX + name + ".download");
        mutex.acquire();
        try {
            long start = System.currentTimeMillis();
            boolean retry;
            do {
                retry = false;
                updateState();
                if (state == DOWNLOADED || state == INSTALLED) {
                    return;
                }
                File tmp = null;
                try {
                    tmp = new File(localPath + ".tmp");
                    if (DownloadManager.getBaseDownloadURL().equals(DownloadManager.RESOURCE_URL)) {
                        String path = "/" + name + ".zip";
                        InputStream in = getClass().getResourceAsStream(path);
                        if (in == null)
                            throw new IOException("could not locate " + "resource: " + path);
                        FileOutputStream out = new FileOutputStream(tmp);
                        DownloadManager.send(in, out);
                        in.close();
                        out.close();
                    } else {
                        try {
                            String bundleURL = getURL(showProgress);
                            DownloadManager.log("Downloading from: " + bundleURL);
                            DownloadManager.downloadFromURL(bundleURL, tmp, name.replace('_', '.'), showProgress);
                        } catch (HttpRetryException e) {
                            DownloadManager.flushBundleURLs();
                            String bundleURL = getURL(showProgress);
                            DownloadManager.log("Retrying at new " + "URL: " + bundleURL);
                            DownloadManager.downloadFromURL(bundleURL, tmp, name.replace('_', '.'), showProgress);
                        }
                    }
                    if (!tmp.exists() || tmp.length() == 0) {
                        if (showProgress) {
                            DownloadManager.complete = true;
                        }
                        DownloadManager.fatalError(DownloadManager.ERROR_UNSPECIFIED);
                    }
                    BundleCheck gottenCheck = BundleCheck.getInstance(tmp);
                    BundleCheck expectedCheck = BundleCheck.getInstance(name);
                    if (expectedCheck.equals(gottenCheck)) {
                        long uncompressedLength = tmp.length();
                        localPath.delete();
                        File uncompressedPath = new File(tmp.getPath() + ".jar0");
                        if (!extraUncompress(tmp.getPath(), uncompressedPath.getPath())) {
                            if (DownloadManager.debug) {
                                DownloadManager.log("Uncompressing with GZIP");
                            }
                            GZIPInputStream in = new GZIPInputStream(new BufferedInputStream(new FileInputStream(tmp), DownloadManager.BUFFER_SIZE));
                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(uncompressedPath), DownloadManager.BUFFER_SIZE);
                            DownloadManager.send(in, out);
                            in.close();
                            out.close();
                            if (!uncompressedPath.renameTo(localPath)) {
                                throw new IOException("unable to rename " + uncompressedPath + " to " + localPath);
                            }
                        } else {
                            if (DownloadManager.debug) {
                                DownloadManager.log("Uncompressing with LZMA");
                            }
                            if (!uncompressedPath.renameTo(localPath)) {
                                throw new IOException("unable to rename " + uncompressedPath + " to " + localPath);
                            }
                        }
                        state = DOWNLOADED;
                        bytesDownloaded += uncompressedLength;
                        long time = (System.currentTimeMillis() - start);
                        DownloadManager.log("Downloaded " + name + " in " + time + "ms.  Downloaded " + bytesDownloaded + " bytes this session.");
                    } else {
                        tmp.delete();
                        DownloadManager.log("DownloadManager: Security check failed for " + "bundle " + name);
                        if (showProgress) {
                            retry = DownloadManager.askUserToRetryDownloadOrQuit(DownloadManager.ERROR_UNSPECIFIED);
                        }
                        if (!retry) {
                            throw new RuntimeException("Failed bundle security check and user " + "canceled");
                        }
                    }
                } catch (IOException e) {
                    DownloadManager.log(e);
                }
            } while (retry);
        } finally {
            mutex.release();
        }
    }

    void queueDependencies(boolean showProgress) {
        try {
            String dependencies = DownloadManager.getBundleProperty(name, DownloadManager.DEPENDENCIES_PROPERTY);
            if (dependencies != null) {
                StringTokenizer st = new StringTokenizer(dependencies, " ,");
                while (st.hasMoreTokens()) {
                    Bundle b = getBundle(st.nextToken());
                    if (b != null && !b.isInstalled()) {
                        if (DownloadManager.debug) {
                            DownloadManager.log("Queueing " + b.name + " as a dependency of " + name + "...");
                        }
                        b.install(showProgress, true, false);
                    }
                }
            }
        } catch (IOException e) {
            DownloadManager.log(e);
        }
    }

    static synchronized ExecutorService getThreadPool() {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(THREADS, new ThreadFactory() {

                public Thread newThread(Runnable r) {
                    Thread result = new Thread(r);
                    result.setDaemon(true);
                    return result;
                }
            });
        }
        return threadPool;
    }

    private void unpackBundle() throws IOException {
        File useJarPath = null;
        if (DownloadManager.isWindowsVista()) {
            useJarPath = lowJarPath;
            File jarDir = useJarPath.getParentFile();
            if (jarDir != null) {
                jarDir.mkdirs();
            }
        } else {
            useJarPath = jarPath;
        }
        DownloadManager.log("Unpacking " + this + " to " + useJarPath);
        InputStream rawStream = new FileInputStream(localPath);
        JarInputStream in = new JarInputStream(rawStream) {

            public void close() throws IOException {
            }
        };
        try {
            File jarTmp = null;
            JarEntry entry;
            while ((entry = in.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.equals("classes.pack")) {
                    File packTmp = new File(useJarPath + ".pack");
                    packTmp.getParentFile().mkdirs();
                    DownloadManager.log("Writing temporary .pack file " + packTmp);
                    OutputStream tmpOut = new FileOutputStream(packTmp);
                    try {
                        DownloadManager.send(in, tmpOut);
                    } finally {
                        tmpOut.close();
                    }
                    jarTmp = new File(useJarPath + ".tmp");
                    DownloadManager.log("Writing temporary .jar file " + jarTmp);
                    unpack(packTmp, jarTmp);
                    packTmp.delete();
                } else if (!entryName.startsWith("META-INF")) {
                    File dest;
                    if (DownloadManager.isWindowsVista()) {
                        dest = new File(lowJavaPath, entryName.replace('/', File.separatorChar));
                    } else {
                        dest = new File(DownloadManager.JAVA_HOME, entryName.replace('/', File.separatorChar));
                    }
                    if (entryName.equals(BUNDLE_JAR_ENTRY_NAME))
                        dest = useJarPath;
                    File destTmp = new File(dest + ".tmp");
                    boolean exists = dest.exists();
                    if (!exists) {
                        DownloadManager.log(dest + ".mkdirs()");
                        dest.getParentFile().mkdirs();
                    }
                    try {
                        DownloadManager.log("Using temporary file " + destTmp);
                        FileOutputStream out = new FileOutputStream(destTmp);
                        try {
                            byte[] buffer = new byte[2048];
                            int c;
                            while ((c = in.read(buffer)) > 0) out.write(buffer, 0, c);
                        } finally {
                            out.close();
                        }
                        if (exists)
                            dest.delete();
                        DownloadManager.log("Renaming from " + destTmp + " to " + dest);
                        if (!destTmp.renameTo(dest)) {
                            throw new IOException("unable to rename " + destTmp + " to " + dest);
                        }
                    } catch (IOException e) {
                        if (!exists)
                            throw e;
                    }
                }
            }
            if (jarTmp != null) {
                if (useJarPath.exists())
                    jarTmp.delete();
                else if (!jarTmp.renameTo(useJarPath)) {
                    throw new IOException("unable to rename " + jarTmp + " to " + useJarPath);
                }
            }
            if (DownloadManager.isWindowsVista()) {
                DownloadManager.log("Using broker to move " + name);
                if (!DownloadManager.moveDirWithBroker(DownloadManager.getKernelJREDir() + name)) {
                    throw new IOException("unable to create " + name);
                }
                DownloadManager.log("Broker finished " + name);
            }
            DownloadManager.log("Finished unpacking " + this);
        } finally {
            rawStream.close();
        }
        if (deleteOnInstall) {
            localPath.delete();
        }
    }

    public static void unpack(File pack, File jar) throws IOException {
        Process p = Runtime.getRuntime().exec(DownloadManager.JAVA_HOME + File.separator + "bin" + File.separator + "unpack200 -Hoff \"" + pack + "\" \"" + jar + "\"");
        try {
            p.waitFor();
        } catch (InterruptedException e) {
        }
    }

    public void install() throws IOException {
        install(true, false, true);
    }

    public synchronized void install(final boolean showProgress, final boolean downloadOnly, boolean block) throws IOException {
        if (DownloadManager.isJREComplete())
            return;
        if (state == NOT_DOWNLOADED || state == QUEUED) {
            if (state != QUEUED) {
                DownloadManager.addToTotalDownloadSize(getSize());
                state = QUEUED;
            }
            if (getThreadPool().isShutdown()) {
                if (state == NOT_DOWNLOADED || state == QUEUED)
                    doInstall(showProgress, downloadOnly);
            } else {
                Future task = getThreadPool().submit(new Runnable() {

                    public void run() {
                        try {
                            if (state == NOT_DOWNLOADED || state == QUEUED || (!downloadOnly && state == DOWNLOADED)) {
                                doInstall(showProgress, downloadOnly);
                            }
                        } catch (IOException e) {
                        }
                    }
                });
                queueDependencies(showProgress);
                if (block) {
                    try {
                        task.get();
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
            }
        } else if (state == DOWNLOADED && !downloadOnly)
            doInstall(showProgress, false);
    }

    private void doInstall(boolean showProgress, boolean downloadOnly) throws IOException {
        Mutex mutex = Mutex.create(DownloadManager.MUTEX_PREFIX + name + ".install");
        DownloadManager.bundleInstallStart();
        try {
            mutex.acquire();
            updateState();
            if (state == NOT_DOWNLOADED || state == QUEUED) {
                download(showProgress);
            }
            if (state == DOWNLOADED && downloadOnly) {
                return;
            }
            if (state == INSTALLED) {
                return;
            }
            if (state != DOWNLOADED) {
                DownloadManager.fatalError(DownloadManager.ERROR_UNSPECIFIED);
            }
            DownloadManager.log("Calling unpackBundle for " + this);
            unpackBundle();
            DownloadManager.log("Writing receipt for " + this);
            writeReceipt();
            updateState();
            DownloadManager.log("Finished installing " + this + ", state=" + state);
        } finally {
            if (lowJavaPath != null) {
                lowJavaPath.delete();
            }
            mutex.release();
            DownloadManager.bundleInstallComplete();
        }
    }

    synchronized void setState(int state) {
        this.state = state;
    }

    public boolean isInstalled() {
        synchronized (Bundle.class) {
            updateState();
            return state == INSTALLED;
        }
    }

    private void writeReceipt() {
        getReceiptsMutex().acquire();
        File useReceiptPath = null;
        try {
            try {
                receipts.add(name);
                if (DownloadManager.isWindowsVista()) {
                    useReceiptPath = new File(DownloadManager.getLocalLowTempBundlePath(), "receipts");
                    if (receiptPath.exists()) {
                        DownloadManager.copyReceiptFile(receiptPath, useReceiptPath);
                    }
                    FileOutputStream out = new FileOutputStream(useReceiptPath, receiptPath.exists());
                    out.write((name + System.getProperty("line.separator")).getBytes("utf-8"));
                    out.close();
                    if (!DownloadManager.moveFileWithBroker(DownloadManager.getKernelJREDir() + "-bundles" + File.separator + "receipts")) {
                        throw new IOException("failed to write receipts");
                    }
                } else {
                    useReceiptPath = receiptPath;
                    FileOutputStream out = new FileOutputStream(useReceiptPath, true);
                    out.write((name + System.getProperty("line.separator")).getBytes("utf-8"));
                    out.close();
                }
            } catch (IOException e) {
                DownloadManager.log(e);
            }
        } finally {
            getReceiptsMutex().release();
        }
    }

    public String toString() {
        return "Bundle[" + name + "]";
    }
}
