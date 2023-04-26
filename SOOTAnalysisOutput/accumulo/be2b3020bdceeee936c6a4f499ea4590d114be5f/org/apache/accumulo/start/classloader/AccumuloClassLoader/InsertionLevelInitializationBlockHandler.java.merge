package org.apache.accumulo.start.classloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AccumuloClassLoader {

    public static final String GENERAL_CLASSPATHS = "general.classpaths";

    private static URL accumuloConfigUrl;

    private static URLClassLoader classloader;

    private static final Logger log = LoggerFactory.getLogger(AccumuloClassLoader.class);

    static {
        String configFile = System.getProperty("accumulo.properties", "accumulo.properties");
        if (configFile.startsWith("file://")) {
            try {
                File f = new File(new URI(configFile));
                if (f.exists() && !f.isDirectory()) {
                    accumuloConfigUrl = f.toURI().toURL();
                } else {
                    log.warn("Failed to load Accumulo configuration from " + configFile);
                }
            } catch (URISyntaxException | MalformedURLException e) {
                log.warn("Failed to load Accumulo configuration from " + configFile, e);
            }
        } else {
            accumuloConfigUrl = AccumuloClassLoader.class.getClassLoader().getResource(configFile);
            if (accumuloConfigUrl == null)
                log.warn("Failed to load Accumulo configuration '{}' from classpath", configFile);
        }
        if (accumuloConfigUrl != null)
            log.debug("Using Accumulo configuration at {}", accumuloConfigUrl.getFile());
    }

    public static String getAccumuloProperty(String propertyName, String defaultValue) {
        if (accumuloConfigUrl == null) {
            log.warn("Using default value '{}' for '{}' as there is no Accumulo configuration on classpath", defaultValue, propertyName);
            return defaultValue;
        }
        try {
            FileBasedConfigurationBuilder<PropertiesConfiguration> propsBuilder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class).configure(new Parameters().properties().setURL(accumuloConfigUrl));
            PropertiesConfiguration config = propsBuilder.getConfiguration();
            String value = config.getString(propertyName);
            if (value != null)
                return value;
            return defaultValue;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to look up property " + propertyName + " in " + accumuloConfigUrl.getFile(), e);
        }
    }

    public static String replaceEnvVars(String classpath, Map<String, String> env) {
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

    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "class path configuration is controlled by admin, not unchecked user input")
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
        if (uri == null || !uri.isAbsolute() || (uri.getScheme() != null && uri.getScheme().equals("file://"))) {
            final File extDir = new File(classpath);
            if (extDir.isDirectory())
                urls.add(extDir.toURI().toURL());
            else {
                if (extDir.getParentFile() != null) {
                    File[] extJars = extDir.getParentFile().listFiles((dir, name) -> name.matches("^" + extDir.getName()));
                    if (extJars != null && extJars.length > 0) {
                        for (File jar : extJars) urls.add(jar.toURI().toURL());
                    } else {
                        log.debug("ignoring classpath entry {}", classpath);
                    }
                } else {
                    log.debug("ignoring classpath entry {}", classpath);
                }
            }
        } else {
            urls.add(uri.toURL());
        }
    }

    private static ArrayList<URL> findAccumuloURLs() throws IOException {
        String cp = getAccumuloProperty(GENERAL_CLASSPATHS, null);
        if (cp == null)
            return new ArrayList<>();
        log.warn("'{}' is deprecated but was set to '{}' ", GENERAL_CLASSPATHS, cp);
        String[] cps = replaceEnvVars(cp, System.getenv()).split(",");
        ArrayList<URL> urls = new ArrayList<>();
        for (String classpath : cps) {
            if (!classpath.startsWith("#")) {
                addUrl(classpath, urls);
            }
        }
        return urls;
    }

    public static synchronized ClassLoader getClassLoader() throws IOException {
        if (classloader == null) {
            ArrayList<URL> urls = findAccumuloURLs();
            ClassLoader parentClassLoader = AccumuloClassLoader.class.getClassLoader();
            log.debug("Create 2nd tier ClassLoader using URLs: {}", urls);
            classloader = new URLClassLoader(urls.toArray(new URL[urls.size()]), parentClassLoader) {

                @Override
                protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                    if (name.startsWith("org.apache.accumulo.start.classloader.vfs")) {
                        Class<?> c = findLoadedClass(name);
                        if (c == null) {
                            try {
                                findClass(name);
                            } catch (ClassNotFoundException e) {
                            }
                        }
                    }
                    return super.loadClass(name, resolve);
                }
            };
        }
        return classloader;
    }
}