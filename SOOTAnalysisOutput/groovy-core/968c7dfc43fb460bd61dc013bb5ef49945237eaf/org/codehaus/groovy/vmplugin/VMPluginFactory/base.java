package org.codehaus.groovy.vmplugin;

import org.codehaus.groovy.vmplugin.v4.Java4;

public class VMPluginFactory {

    private static final String JDK5_CLASSNAME_CHECK = "java.lang.annotation.Annotation";

    private static final String JDK6_CLASSNAME_CHECK = "javax.script.ScriptEngine";

    private static final String JDK7_CLASSNAME_CHECK = "java.nio.file.FileRef";

    private static final String JDK5_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v5.Java5";

    private static final String JDK6_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v6.Java6";

    private static final String JDK7_PLUGIN_NAME = "org.codehaus.groovy.vmplugin.v7.Java7";

    private static VMPlugin plugin;

    static {
        plugin = createPlugin(JDK7_CLASSNAME_CHECK, JDK7_PLUGIN_NAME);
        if (plugin == null) {
            plugin = createPlugin(JDK6_CLASSNAME_CHECK, JDK6_PLUGIN_NAME);
        }
        if (plugin == null) {
            plugin = createPlugin(JDK5_CLASSNAME_CHECK, JDK5_PLUGIN_NAME);
        }
        if (plugin == null) {
            plugin = new Java4();
        }
    }

    public static VMPlugin getPlugin() {
        return plugin;
    }

    private static VMPlugin createPlugin(String classNameCheck, String pluginName) {
        try {
            ClassLoader loader = VMPluginFactory.class.getClassLoader();
            loader.loadClass(classNameCheck);
            return (VMPlugin) loader.loadClass(pluginName).newInstance();
        } catch (Exception ex) {
            return null;
        }
    }
}
