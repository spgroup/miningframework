package sun.misc;

import java.util.*;
import java.util.jar.JarFile;
import sun.misc.JarIndex;
import sun.misc.InvalidJarIndexException;
import sun.net.www.ParseUtil;
import java.util.zip.ZipEntry;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.UnsupportedProfileException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.io.*;
import java.security.AccessController;
import java.security.AccessControlException;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import sun.misc.FileURLMapper;
import sun.net.util.URLUtil;

public class URLClassPath {

    final static String USER_AGENT_JAVA_VERSION = "UA-Java-Version";

    final static String JAVA_VERSION;

    private static final boolean DEBUG;

    private static final boolean DISABLE_JAR_CHECKING;

    private static boolean profileCheckSuppressedByLauncher;

    static {
        JAVA_VERSION = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("java.version"));
        DEBUG = (java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.misc.URLClassPath.debug")) != null);
        String p = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.misc.URLClassPath.disableJarChecking"));
        DISABLE_JAR_CHECKING = p != null ? p.equals("true") || p.equals("") : false;
    }

    private ArrayList<URL> path = new ArrayList<URL>();

    Stack<URL> urls = new Stack<URL>();

    ArrayList<Loader> loaders = new ArrayList<Loader>();

    HashMap<String, Loader> lmap = new HashMap<String, Loader>();

    private URLStreamHandler jarHandler;

    private boolean closed = false;

    public URLClassPath(URL[] urls, URLStreamHandlerFactory factory) {
        for (int i = 0; i < urls.length; i++) {
            path.add(urls[i]);
        }
        push(urls);
        if (factory != null) {
            jarHandler = factory.createURLStreamHandler("jar");
        }
    }

    public URLClassPath(URL[] urls) {
        this(urls, null);
    }

    public synchronized List<IOException> closeLoaders() {
        if (closed) {
            return Collections.emptyList();
        }
        List<IOException> result = new LinkedList<IOException>();
        for (Loader loader : loaders) {
            try {
                loader.close();
            } catch (IOException e) {
                result.add(e);
            }
        }
        closed = true;
        return result;
    }

    public synchronized void addURL(URL url) {
        if (closed)
            return;
        synchronized (urls) {
            if (url == null || path.contains(url))
                return;
            urls.add(0, url);
            path.add(url);
        }
    }

    public URL[] getURLs() {
        synchronized (urls) {
            return path.toArray(new URL[path.size()]);
        }
    }

    public URL findResource(String name, boolean check) {
        Loader loader;
        for (int i = 0; (loader = getLoader(i)) != null; i++) {
            URL url = loader.findResource(name, check);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public Resource getResource(String name, boolean check) {
        if (DEBUG) {
            System.err.println("URLClassPath.getResource(\"" + name + "\")");
        }
        Loader loader;
        for (int i = 0; (loader = getLoader(i)) != null; i++) {
            Resource res = loader.getResource(name, check);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public Enumeration<URL> findResources(final String name, final boolean check) {
        return new Enumeration<URL>() {

            private int index = 0;

            private URL url = null;

            private boolean next() {
                if (url != null) {
                    return true;
                } else {
                    Loader loader;
                    while ((loader = getLoader(index++)) != null) {
                        url = loader.findResource(name, check);
                        if (url != null) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            public boolean hasMoreElements() {
                return next();
            }

            public URL nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                URL u = url;
                url = null;
                return u;
            }
        };
    }

    public Resource getResource(String name) {
        return getResource(name, true);
    }

    public Enumeration<Resource> getResources(final String name, final boolean check) {
        return new Enumeration<Resource>() {

            private int index = 0;

            private Resource res = null;

            private boolean next() {
                if (res != null) {
                    return true;
                } else {
                    Loader loader;
                    while ((loader = getLoader(index++)) != null) {
                        res = loader.getResource(name, check);
                        if (res != null) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            public boolean hasMoreElements() {
                return next();
            }

            public Resource nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                Resource r = res;
                res = null;
                return r;
            }
        };
    }

    public Enumeration<Resource> getResources(final String name) {
        return getResources(name, true);
    }

    private synchronized Loader getLoader(int index) {
        if (closed) {
            return null;
        }
        while (loaders.size() < index + 1) {
            URL url;
            synchronized (urls) {
                if (urls.empty()) {
                    return null;
                } else {
                    url = urls.pop();
                }
            }
            String urlNoFragString = URLUtil.urlNoFragString(url);
            if (lmap.containsKey(urlNoFragString)) {
                continue;
            }
            Loader loader;
            try {
                loader = getLoader(url);
                URL[] urls = loader.getClassPath();
                if (urls != null) {
                    push(urls);
                }
            } catch (IOException e) {
                continue;
            }
            loaders.add(loader);
            lmap.put(urlNoFragString, loader);
        }
        return loaders.get(index);
    }

    private Loader getLoader(final URL url) throws IOException {
        try {
            return java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Loader>() {

                public Loader run() throws IOException {
                    String file = url.getFile();
                    if (file != null && file.endsWith("/")) {
                        if ("file".equals(url.getProtocol())) {
                            return new FileLoader(url);
                        } else {
                            return new Loader(url);
                        }
                    } else {
                        return new JarLoader(url, jarHandler, lmap);
                    }
                }
            });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    private void push(URL[] us) {
        synchronized (urls) {
            for (int i = us.length - 1; i >= 0; --i) {
                urls.push(us[i]);
            }
        }
    }

    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            File f = new File(st.nextToken());
            try {
                f = new File(f.getCanonicalPath());
            } catch (IOException x) {
            }
            try {
                urls[count++] = ParseUtil.fileToEncodedURL(f);
            } catch (IOException x) {
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    public URL checkURL(URL url) {
        try {
            check(url);
        } catch (Exception e) {
            return null;
        }
        return url;
    }

    static void check(URL url) throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            URLConnection urlConnection = url.openConnection();
            Permission perm = urlConnection.getPermission();
            if (perm != null) {
                try {
                    security.checkPermission(perm);
                } catch (SecurityException se) {
                    if ((perm instanceof java.io.FilePermission) && perm.getActions().indexOf("read") != -1) {
                        security.checkRead(perm.getName());
                    } else if ((perm instanceof java.net.SocketPermission) && perm.getActions().indexOf("connect") != -1) {
                        URL locUrl = url;
                        if (urlConnection instanceof JarURLConnection) {
                            locUrl = ((JarURLConnection) urlConnection).getJarFileURL();
                        }
                        security.checkConnect(locUrl.getHost(), locUrl.getPort());
                    } else {
                        throw se;
                    }
                }
            }
        }
    }

    private static class Loader implements Closeable {

        private final URL base;

        private JarFile jarfile;

        Loader(URL url) {
            base = url;
        }

        URL getBaseURL() {
            return base;
        }

        URL findResource(final String name, boolean check) {
            URL url;
            try {
                url = new URL(base, ParseUtil.encodePath(name, false));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("name");
            }
            try {
                if (check) {
                    URLClassPath.check(url);
                }
                URLConnection uc = url.openConnection();
                if (uc instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) uc;
                    hconn.setRequestMethod("HEAD");
                    if (hconn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                        return null;
                    }
                } else {
                    uc.setUseCaches(false);
                    InputStream is = uc.getInputStream();
                    is.close();
                }
                return url;
            } catch (Exception e) {
                return null;
            }
        }

        Resource getResource(final String name, boolean check) {
            final URL url;
            try {
                url = new URL(base, ParseUtil.encodePath(name, false));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("name");
            }
            final URLConnection uc;
            try {
                if (check) {
                    URLClassPath.check(url);
                }
                uc = url.openConnection();
                InputStream in = uc.getInputStream();
                if (uc instanceof JarURLConnection) {
                    JarURLConnection juc = (JarURLConnection) uc;
                    jarfile = JarLoader.checkJar(juc.getJarFile());
                }
            } catch (Exception e) {
                return null;
            }
            return new Resource() {

                public String getName() {
                    return name;
                }

                public URL getURL() {
                    return url;
                }

                public URL getCodeSourceURL() {
                    return base;
                }

                public InputStream getInputStream() throws IOException {
                    return uc.getInputStream();
                }

                public int getContentLength() throws IOException {
                    return uc.getContentLength();
                }
            };
        }

        Resource getResource(final String name) {
            return getResource(name, true);
        }

        public void close() throws IOException {
            if (jarfile != null) {
                jarfile.close();
            }
        }

        URL[] getClassPath() throws IOException {
            return null;
        }
    }

    public static void suppressProfileCheckForLauncher() {
        profileCheckSuppressedByLauncher = true;
    }

    static class JarLoader extends Loader {

        private JarFile jar;

        private URL csu;

        private JarIndex index;

        private MetaIndex metaIndex;

        private URLStreamHandler handler;

        private HashMap<String, Loader> lmap;

        private boolean closed = false;

        private static final sun.misc.JavaUtilZipFileAccess zipAccess = sun.misc.SharedSecrets.getJavaUtilZipFileAccess();

        JarLoader(URL url, URLStreamHandler jarHandler, HashMap<String, Loader> loaderMap) throws IOException {
            super(new URL("jar", "", -1, url + "!/", jarHandler));
            csu = url;
            handler = jarHandler;
            lmap = loaderMap;
            if (!isOptimizable(url)) {
                ensureOpen();
            } else {
                String fileName = url.getFile();
                if (fileName != null) {
                    fileName = ParseUtil.decode(fileName);
                    File f = new File(fileName);
                    metaIndex = MetaIndex.forJar(f);
                    if (metaIndex != null && !f.exists()) {
                        metaIndex = null;
                    }
                }
                if (metaIndex == null) {
                    ensureOpen();
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ensureOpen();
                jar.close();
            }
        }

        JarFile getJarFile() {
            return jar;
        }

        private boolean isOptimizable(URL url) {
            return "file".equals(url.getProtocol());
        }

        private void ensureOpen() throws IOException {
            if (jar == null) {
                try {
                    java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<Void>() {

                        public Void run() throws IOException {
                            if (DEBUG) {
                                System.err.println("Opening " + csu);
                                Thread.dumpStack();
                            }
                            jar = getJarFile(csu);
                            index = JarIndex.getJarIndex(jar, metaIndex);
                            if (index != null) {
                                String[] jarfiles = index.getJarFiles();
                                for (int i = 0; i < jarfiles.length; i++) {
                                    try {
                                        URL jarURL = new URL(csu, jarfiles[i]);
                                        String urlNoFragString = URLUtil.urlNoFragString(jarURL);
                                        if (!lmap.containsKey(urlNoFragString)) {
                                            lmap.put(urlNoFragString, null);
                                        }
                                    } catch (MalformedURLException e) {
                                        continue;
                                    }
                                }
                            }
                            return null;
                        }
                    });
                } catch (java.security.PrivilegedActionException pae) {
                    throw (IOException) pae.getException();
                }
            }
        }

        static JarFile checkJar(JarFile jar) throws IOException {
            if (System.getSecurityManager() != null && !DISABLE_JAR_CHECKING && !zipAccess.startsWithLocHeader(jar)) {
                IOException x = new IOException("Invalid Jar file");
                try {
                    jar.close();
                } catch (IOException ex) {
                    x.addSuppressed(ex);
                }
                throw x;
            }
            return jar;
        }

        private JarFile getJarFile(URL url) throws IOException {
            if (isOptimizable(url)) {
                FileURLMapper p = new FileURLMapper(url);
                if (!p.exists()) {
                    throw new FileNotFoundException(p.getPath());
                }
                return checkJar(new JarFile(p.getPath()));
            }
            URLConnection uc = getBaseURL().openConnection();
            uc.setRequestProperty(USER_AGENT_JAVA_VERSION, JAVA_VERSION);
            JarFile jarFile = ((JarURLConnection) uc).getJarFile();
            return checkJar(jarFile);
        }

        JarIndex getIndex() {
            try {
                ensureOpen();
            } catch (IOException e) {
                throw new InternalError(e);
            }
            return index;
        }

        Resource checkResource(final String name, boolean check, final JarEntry entry) {
            final URL url;
            try {
                url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
                if (check) {
                    URLClassPath.check(url);
                }
            } catch (MalformedURLException e) {
                return null;
            } catch (IOException e) {
                return null;
            } catch (AccessControlException e) {
                return null;
            }
            return new Resource() {

                public String getName() {
                    return name;
                }

                public URL getURL() {
                    return url;
                }

                public URL getCodeSourceURL() {
                    return csu;
                }

                public InputStream getInputStream() throws IOException {
                    return jar.getInputStream(entry);
                }

                public int getContentLength() {
                    return (int) entry.getSize();
                }

                public Manifest getManifest() throws IOException {
                    return jar.getManifest();
                }

                public Certificate[] getCertificates() {
                    return entry.getCertificates();
                }

                public CodeSigner[] getCodeSigners() {
                    return entry.getCodeSigners();
                }
            };
        }

        boolean validIndex(final String name) {
            String packageName = name;
            int pos;
            if ((pos = name.lastIndexOf("/")) != -1) {
                packageName = name.substring(0, pos);
            }
            String entryName;
            ZipEntry entry;
            Enumeration<JarEntry> enum_ = jar.entries();
            while (enum_.hasMoreElements()) {
                entry = enum_.nextElement();
                entryName = entry.getName();
                if ((pos = entryName.lastIndexOf("/")) != -1)
                    entryName = entryName.substring(0, pos);
                if (entryName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        void checkProfileAttribute() throws IOException {
            Manifest man = jar.getManifest();
            if (man != null) {
                Attributes attr = man.getMainAttributes();
                if (attr != null) {
                    String value = attr.getValue(Name.PROFILE);
                    if (value != null && !Version.supportsProfile(value)) {
                        String prefix = Version.profileName().length() > 0 ? "This runtime implements " + Version.profileName() + ", " : "";
                        throw new UnsupportedProfileException(prefix + csu + " requires " + value);
                    }
                }
            }
        }

        URL findResource(final String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        Resource getResource(final String name, boolean check) {
            if (metaIndex != null) {
                if (!metaIndex.mayContain(name)) {
                    return null;
                }
            }
            try {
                ensureOpen();
            } catch (IOException e) {
                throw new InternalError(e);
            }
            final JarEntry entry = jar.getJarEntry(name);
            if (entry != null)
                return checkResource(name, check, entry);
            if (index == null)
                return null;
            HashSet<String> visited = new HashSet<String>();
            return getResource(name, check, visited);
        }

        Resource getResource(final String name, boolean check, Set<String> visited) {
            Resource res;
            String[] jarFiles;
            int count = 0;
            LinkedList<String> jarFilesList = null;
            if ((jarFilesList = index.get(name)) == null)
                return null;
            do {
                int size = jarFilesList.size();
                jarFiles = jarFilesList.toArray(new String[size]);
                while (count < size) {
                    String jarName = jarFiles[count++];
                    JarLoader newLoader;
                    final URL url;
                    try {
                        url = new URL(csu, jarName);
                        String urlNoFragString = URLUtil.urlNoFragString(url);
                        if ((newLoader = (JarLoader) lmap.get(urlNoFragString)) == null) {
                            newLoader = AccessController.doPrivileged(new PrivilegedExceptionAction<JarLoader>() {

                                public JarLoader run() throws IOException {
                                    return new JarLoader(url, handler, lmap);
                                }
                            });
                            JarIndex newIndex = newLoader.getIndex();
                            if (newIndex != null) {
                                int pos = jarName.lastIndexOf("/");
                                newIndex.merge(this.index, (pos == -1 ? null : jarName.substring(0, pos + 1)));
                            }
                            lmap.put(urlNoFragString, newLoader);
                        }
                    } catch (java.security.PrivilegedActionException pae) {
                        continue;
                    } catch (MalformedURLException e) {
                        continue;
                    }
                    boolean visitedURL = !visited.add(URLUtil.urlNoFragString(url));
                    if (!visitedURL) {
                        try {
                            newLoader.ensureOpen();
                        } catch (IOException e) {
                            throw new InternalError(e);
                        }
                        final JarEntry entry = newLoader.jar.getJarEntry(name);
                        if (entry != null) {
                            return newLoader.checkResource(name, check, entry);
                        }
                        if (!newLoader.validIndex(name)) {
                            throw new InvalidJarIndexException("Invalid index");
                        }
                    }
                    if (visitedURL || newLoader == this || newLoader.getIndex() == null) {
                        continue;
                    }
                    if ((res = newLoader.getResource(name, check, visited)) != null) {
                        return res;
                    }
                }
                jarFilesList = index.get(name);
            } while (count < jarFilesList.size());
            return null;
        }

        URL[] getClassPath() throws IOException {
            if (index != null) {
                return null;
            }
            if (metaIndex != null) {
                return null;
            }
            ensureOpen();
            parseExtensionsDependencies();
            if (!profileCheckSuppressedByLauncher && SharedSecrets.javaUtilJarAccess().jarFileHasProfileAttribute(jar)) {
                checkProfileAttribute();
            }
            if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(jar)) {
                Manifest man = jar.getManifest();
                if (man != null) {
                    Attributes attr = man.getMainAttributes();
                    if (attr != null) {
                        String value = attr.getValue(Name.CLASS_PATH);
                        if (value != null) {
                            return parseClassPath(csu, value);
                        }
                    }
                }
            }
            return null;
        }

        private void parseExtensionsDependencies() throws IOException {
            ExtensionDependency.checkExtensionsDependencies(jar);
        }

        private URL[] parseClassPath(URL base, String value) throws MalformedURLException {
            StringTokenizer st = new StringTokenizer(value);
            URL[] urls = new URL[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                String path = st.nextToken();
                urls[i] = new URL(base, path);
                i++;
            }
            return urls;
        }
    }

    private static class FileLoader extends Loader {

        private File dir;

        FileLoader(URL url) throws IOException {
            super(url);
            if (!"file".equals(url.getProtocol())) {
                throw new IllegalArgumentException("url");
            }
            String path = url.getFile().replace('/', File.separatorChar);
            path = ParseUtil.decode(path);
            dir = (new File(path)).getCanonicalFile();
        }

        URL findResource(final String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        Resource getResource(final String name, boolean check) {
            final URL url;
            try {
                URL normalizedBase = new URL(getBaseURL(), ".");
                url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
                if (url.getFile().startsWith(normalizedBase.getFile()) == false) {
                    return null;
                }
                if (check)
                    URLClassPath.check(url);
                final File file;
                if (name.indexOf("..") != -1) {
                    file = (new File(dir, name.replace('/', File.separatorChar))).getCanonicalFile();
                    if (!((file.getPath()).startsWith(dir.getPath()))) {
                        return null;
                    }
                } else {
                    file = new File(dir, name.replace('/', File.separatorChar));
                }
                if (file.exists()) {
                    return new Resource() {

                        public String getName() {
                            return name;
                        }

                        public URL getURL() {
                            return url;
                        }

                        public URL getCodeSourceURL() {
                            return getBaseURL();
                        }

                        public InputStream getInputStream() throws IOException {
                            return new FileInputStream(file);
                        }

                        public int getContentLength() throws IOException {
                            return (int) file.length();
                        }
                    };
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }
}
