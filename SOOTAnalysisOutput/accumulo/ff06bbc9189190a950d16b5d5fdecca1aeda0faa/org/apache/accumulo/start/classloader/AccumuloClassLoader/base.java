package org.apache.accumulo.start.classloader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jci.listeners.AbstractFilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AccumuloClassLoader {

    private static class Listener extends AbstractFilesystemAlterationListener {

        private volatile boolean firstCall = true;

        @Override
        public void onStop(FilesystemAlterationObserver pObserver) {
            super.onStop(pObserver);
            if (firstCall) {
                synchronized (this) {
                    firstCall = false;
                    this.notifyAll();
                }
                return;
            }
            if (super.getChangedFiles().size() > 0 || super.getCreatedFiles().size() > 0 || super.getDeletedFiles().size() > 0 || super.getChangedDirectories().size() > 0 || super.getCreatedDirectories().size() > 0 || super.getDeletedDirectories().size() > 0) {
                log.debug("Files have changed, setting loader to null ");
                loader = null;
            }
        }

        public synchronized void waitForFirstCall() {
            while (firstCall == true) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static final Logger log = Logger.getLogger(AccumuloClassLoader.class);

    public static final String CLASSPATH_PROPERTY_NAME = "general.classpaths";

    public static final String DYNAMIC_CLASSPATH_PROPERTY_NAME = "general.dynamic.classpaths";

    public static final String ACCUMULO_CLASSPATH_VALUE = "$ACCUMULO_HOME/conf,\n" + "$ACCUMULO_HOME/lib/[^.].$ACCUMULO_VERSION.jar,\n" + "$ACCUMULO_HOME/lib/[^.].*.jar,\n" + "$ZOOKEEPER_HOME/zookeeper[^.].*.jar,\n" + "$HADOOP_HOME/[^.].*.jar,\n" + "$HADOOP_HOME/conf,\n" + "$HADOOP_HOME/lib/[^.].*.jar,\n";

    public static final String DEFAULT_DYNAMIC_CLASSPATH_VALUE = "$ACCUMULO_HOME/lib/ext/[^.].*.jar\n";

    public static final String DEFAULT_CLASSPATH_VALUE = ACCUMULO_CLASSPATH_VALUE;

    private static final String SITE_CONF;

    static {
        String configFile = System.getProperty("org.apache.accumulo.config.file", "accumulo-site.xml");
        if (System.getenv("ACCUMULO_HOME") != null) {
            SITE_CONF = System.getenv("ACCUMULO_HOME") + "/conf/" + configFile;
        } else {
            String userDir = System.getProperty("user.dir");
            if (userDir == null)
                throw new RuntimeException("Property user.dir is not set");
            int index = userDir.indexOf("accumulo/");
            if (index >= 0) {
                String acuhome = userDir.substring(0, index + "accumulo/".length());
                SITE_CONF = acuhome + "/conf/" + configFile;
            } else {
                SITE_CONF = "/conf/" + configFile;
            }
        }
    }

    private static ClassLoader parent = null;

    private static volatile ClassLoader loader = null;

    private static AccumuloFilesystemAlterationMonitor monitor = null;

    private static Object lock = new Object();

    private static ArrayList<URL> findDynamicURLs() throws IOException {
        StringBuilder cp = new StringBuilder(getAccumuloDynamicClasspathStrings());
        String envJars = System.getenv("ACCUMULO_XTRAJARS");
        if (null != envJars && !envJars.equals(""))
            cp = cp.append(",").append(envJars);
        String[] cps = replaceEnvVars(cp.toString(), System.getenv()).split(",");
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String classpath : cps) {
            if (!classpath.startsWith("#")) {
                addUrl(classpath, urls);
            }
        }
        return urls;
    }

    private static Set<File> findDirsFromUrls() throws IOException {
        Set<File> dirs = new HashSet<File>();
        StringBuilder cp = new StringBuilder(getAccumuloDynamicClasspathStrings());
        String envJars = System.getenv("ACCUMULO_XTRAJARS");
        if (null != envJars && !envJars.equals(""))
            cp = cp.append(",").append(envJars);
        String[] cps = replaceEnvVars(cp.toString(), System.getenv()).split(",");
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String classpath : cps) {
            if (!classpath.startsWith("#")) {
                classpath = classpath.trim();
                if (classpath.length() == 0)
                    continue;
                classpath = replaceEnvVars(classpath, System.getenv());
                URI uri = null;
                try {
                    uri = new URI(classpath);
                } catch (URISyntaxException e) {
                }
                if (null == uri || !uri.isAbsolute() || (null != uri.getScheme() && uri.getScheme().equals("file://"))) {
                    final File extDir = new File(classpath);
                    if (extDir.isDirectory())
                        urls.add(extDir.toURI().toURL());
                    else {
                        urls.add(extDir.getAbsoluteFile().getParentFile().toURI().toURL());
                    }
                } else {
                    urls.add(uri.toURL());
                }
            }
        }
        for (URL url : urls) {
            try {
                File f = new File(url.toURI());
                if (!f.isDirectory())
                    f = f.getParentFile();
                dirs.add(f);
            } catch (URISyntaxException e) {
                log.error("Unable to find directory for " + url + ", cannot create URI from it");
            }
        }
        return dirs;
    }

    private static ArrayList<URL> findAccumuloURLs() throws IOException {
        String cp = getAccumuloClasspathStrings();
        if (cp == null)
            return new ArrayList<URL>();
        String[] cps = replaceEnvVars(cp, System.getenv()).split(",");
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String classpath : cps) {
            if (!classpath.startsWith("#")) {
                addUrl(classpath, urls);
            }
        }
        return urls;
    }

    private static void addUrl(String classpath, ArrayList<URL> urls) throws MalformedURLException {
        classpath = classpath.trim();
        if (classpath.length() == 0)
            return;
        classpath = replaceEnvVars(classpath, System.getenv());
        URI uri = null;
        try {
            uri = new URI(classpath);
        } catch (URISyntaxException e) {
        }
        if (null == uri || !uri.isAbsolute() || (null != uri.getScheme() && uri.getScheme().equals("file://"))) {
            final File extDir = new File(classpath);
            if (extDir.isDirectory())
                urls.add(extDir.toURI().toURL());
            else {
                if (extDir.getParentFile() != null) {
                    File[] extJars = extDir.getParentFile().listFiles(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            return name.matches("^" + extDir.getName());
                        }
                    });
                    if (extJars != null && extJars.length > 0) {
                        for (File jar : extJars) urls.add(jar.toURI().toURL());
                    }
                }
            }
        } else {
            urls.add(uri.toURL());
        }
    }

    private static String replaceEnvVars(String classpath, Map<String, String> env) {
        Pattern envPat = Pattern.compile("\\$[A-Za-z][a-zA-Z0-9_]*");
        Matcher envMatcher = envPat.matcher(classpath);
        while (envMatcher.find(0)) {
            String varName = envMatcher.group().substring(1);
            String varValue = env.get(varName);
            if (varValue == null) {
                varValue = "";
            }
            classpath = (classpath.substring(0, envMatcher.start()) + varValue + classpath.substring(envMatcher.end()));
            envMatcher.reset(classpath);
        }
        return classpath;
    }

    public static String getAccumuloDynamicClasspathStrings() throws IllegalStateException {
        return getAccumuloString(DYNAMIC_CLASSPATH_PROPERTY_NAME, DEFAULT_DYNAMIC_CLASSPATH_VALUE);
    }

    public static String getAccumuloClasspathStrings() throws IllegalStateException {
        return getAccumuloString(CLASSPATH_PROPERTY_NAME, ACCUMULO_CLASSPATH_VALUE);
    }

    private static String getAccumuloString(String propertyName, String defaultValue) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            String site_classpath_string = null;
            try {
                Document site_conf = db.parse(SITE_CONF);
                site_classpath_string = getAccumuloClassPathStrings(site_conf, propertyName);
            } catch (Exception e) {
            }
            if (site_classpath_string != null)
                return site_classpath_string;
            return defaultValue;
        } catch (Exception e) {
            throw new IllegalStateException("ClassPath Strings Lookup failed", e);
        }
    }

    private static String getAccumuloClassPathStrings(Document d, String propertyName) {
        NodeList pnodes = d.getElementsByTagName("property");
        for (int i = pnodes.getLength() - 1; i >= 0; i--) {
            Element current_property = (Element) pnodes.item(i);
            Node cname = current_property.getElementsByTagName("name").item(0);
            if (cname != null && cname.getTextContent().compareTo(propertyName) == 0) {
                Node cvalue = current_property.getElementsByTagName("value").item(0);
                if (cvalue != null) {
                    return cvalue.getTextContent();
                }
            }
        }
        return null;
    }

    public static void printClassPath() {
        try {
            System.out.println("Accumulo List of classpath items are:");
            for (URL url : findDynamicURLs()) {
                System.out.println(url.toExternalForm());
            }
            for (URL url : findAccumuloURLs()) {
                System.out.println(url.toExternalForm());
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public synchronized static <U> Class<? extends U> loadClass(String classname, Class<U> extension) throws ClassNotFoundException {
        try {
            return (Class<? extends U>) Class.forName(classname, true, getClassLoader()).asSubclass(extension);
        } catch (IOException e) {
            throw new ClassNotFoundException("IO Error loading class " + classname, e);
        }
    }

    public static Class<?> loadClass(String classname) throws ClassNotFoundException {
        return loadClass(classname, Object.class).asSubclass(Object.class);
    }

    private static ClassLoader getAccumuloClassLoader() throws IOException {
        ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
        ArrayList<URL> accumuloURLs = findAccumuloURLs();
        log.debug("Create Dependency ClassLoader using URLs: " + accumuloURLs.toString());
        URLClassLoader aClassLoader = new URLClassLoader(accumuloURLs.toArray(new URL[accumuloURLs.size()]), parentClassLoader);
        return aClassLoader;
    }

    public static ClassLoader getClassLoader() throws IOException {
        ClassLoader localLoader = loader;
        while (null == localLoader) {
            synchronized (lock) {
                if (null == loader) {
                    if (null != monitor) {
                        monitor.stop();
                        monitor = null;
                    }
                    if (null == parent)
                        parent = getAccumuloClassLoader();
                    final ArrayList<URL> dynamicURLs = findDynamicURLs();
                    Set<File> monitoredDirs = findDirsFromUrls();
                    monitor = new AccumuloFilesystemAlterationMonitor();
                    Listener myListener = new Listener();
                    for (File dir : monitoredDirs) {
                        if (monitor.getListenersFor(dir) != null || monitor.getListenersFor(dir).length > 0) {
                            log.debug("Monitor listening to " + dir.getAbsolutePath());
                            monitor.addListener(dir, myListener);
                        }
                    }
                    monitor.setInterval(1000);
                    monitor.start();
                    myListener.waitForFirstCall();
                    log.debug("Create Dynamic ClassLoader using URLs: " + dynamicURLs.toString());
                    HashSet<URL> checkDynamicURLs = new HashSet<URL>(findDynamicURLs());
                    HashSet<URL> originalDynamicURLs = new HashSet<URL>(dynamicURLs);
                    if (checkDynamicURLs.equals(originalDynamicURLs)) {
                        loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                            public ClassLoader run() {
                                return new URLClassLoader(dynamicURLs.toArray(new URL[dynamicURLs.size()]), parent);
                            }
                        });
                    }
                }
            }
            localLoader = loader;
        }
        return localLoader;
    }
}
