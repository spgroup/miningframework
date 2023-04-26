package org.codehaus.groovy.vmplugin;

import org.codehaus.groovy.vmplugin.v4.Java4;

public class VMPluginFactory {

    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";

    private static final String JDK5_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v5.Java5";

    private static final String JDK6_CLASSNAME_CHECK = "javax.script.ScriptEngine";

    private static final String JDK6_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v6.Java6";

    private static final String JDK7_CLASSNAME_CHECK = "java.nio.file.FileRef";

    private static final String JDK7_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v7.Java7";

    private static VMPlugin plugin;

    static {
        try {
            ClassLoader loader = VMPluginFactory.class.getClassLoader();
            loader.loadClass(JDK7_CLASSNAME_CHECK);
            plugin = (VMPlugin) loader.loadClass(JDK7_PLUGIN_NAME).newInstance();
        } catch (Exception ex) {
        }
        if (plugin == null) {
            try {
                ClassLoader loader = VMPluginFactory.class.getClassLoader();
                loader.loadClass(JDK6_CLASSNAME_CHECK);
                plugin = (VMPlugin) loader.loadClass(JDK6_PLUGIN_NAME).newInstance();
            } catch (Exception ex) {
            }
        }
        if (plugin == null) {
            try {
                ClassLoader loader = VMPluginFactory.class.getClassLoader();
                loader.loadClass(JDK5_CLASSNAME_CHECK);
                plugin = (VMPlugin) loader.loadClass(JDK5_PLUGIN_NAME).newInstance();
            } catch (Exception ex) {
                plugin = new Java4();
            }
        }
    }

    public static VMPlugin getPlugin() {
        return plugin;
    }
}
