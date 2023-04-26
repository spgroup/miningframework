package org.jvnet.hudson.test;

import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPluginManager extends PluginManager {

    public static final PluginManager INSTANCE;

    private TestPluginManager() throws IOException {
        super(null, Util.createTempDir());
    }

    @Override
    protected Collection<String> loadBundledPlugins() throws Exception {
        Set<String> names = new HashSet<String>();
        File bundledPlugins = new File(WarExploder.getExplodedDir(), "WEB-INF/plugins");
        File[] children = bundledPlugins.listFiles();
        if (children == null)
            throw new Error("Unable to find " + bundledPlugins);
        for (File child : children) {
            try {
                names.add(child.getName());
                copyBundledPlugin(child.toURI().toURL(), child.getName());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to extract the bundled plugin " + child, e);
            }
        }
        URL u = getClass().getClassLoader().getResource("the.jpl");
        if (u == null) {
            u = getClass().getClassLoader().getResource("the.hpl");
        }
        if (u != null)
            try {
                names.add("the.jpl");
                copyBundledPlugin(u, "the.jpl");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to copy the.jpl", e);
            }
        URL index = getClass().getResource("/test-dependencies/index");
        if (index != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(index.openStream(), "UTF-8"));
            try {
                String line;
                while ((line = r.readLine()) != null) {
                    final URL url = new URL(index, line + ".jpi");
                    File f = new File(url.toURI());
                    if (f.exists()) {
                        copyBundledPlugin(url, line + ".jpi");
                    } else {
                        copyBundledPlugin(new URL(index, line + ".hpi"), line + ".jpi");
                    }
                }
            } finally {
                r.close();
            }
        }
        return names;
    }

    @Override
    public void stop() {
        for (PluginWrapper p : activePlugins) p.stop();
    }

    private static final Logger LOGGER = Logger.getLogger(TestPluginManager.class.getName());

    static {
        try {
            INSTANCE = new TestPluginManager();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
