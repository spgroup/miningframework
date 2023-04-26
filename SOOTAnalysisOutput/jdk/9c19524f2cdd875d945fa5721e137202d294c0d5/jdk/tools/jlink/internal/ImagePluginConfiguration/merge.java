package jdk.tools.jlink.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jdk.tools.jlink.builder.ImageBuilder;
import jdk.tools.jlink.Jlink;
import jdk.tools.jlink.plugin.Plugin;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.plugin.Plugin.Category;
import jdk.tools.jlink.plugin.ModulePool;

public final class ImagePluginConfiguration {

    private static final List<Category> CATEGORIES_ORDER = new ArrayList<>();

    static {
        CATEGORIES_ORDER.add(Category.FILTER);
        CATEGORIES_ORDER.add(Category.TRANSFORMER);
        CATEGORIES_ORDER.add(Category.MODULEINFO_TRANSFORMER);
        CATEGORIES_ORDER.add(Category.SORTER);
        CATEGORIES_ORDER.add(Category.COMPRESSOR);
        CATEGORIES_ORDER.add(Category.METAINFO_ADDER);
        CATEGORIES_ORDER.add(Category.VERIFIER);
        CATEGORIES_ORDER.add(Category.PROCESSOR);
        CATEGORIES_ORDER.add(Category.PACKAGER);
    }

    private ImagePluginConfiguration() {
    }

    public static ImagePluginStack parseConfiguration(Jlink.PluginsConfiguration pluginsConfiguration) throws Exception {
        if (pluginsConfiguration == null) {
            return new ImagePluginStack();
        }
        Map<Category, List<Plugin>> plugins = new LinkedHashMap<>();
        for (Category cat : CATEGORIES_ORDER) {
            plugins.put(cat, new ArrayList<>());
        }
        List<String> seen = new ArrayList<>();
        for (Plugin plug : pluginsConfiguration.getPlugins()) {
            if (seen.contains(plug.getName())) {
                throw new Exception("Plugin " + plug.getName() + " added more than once to stack ");
            }
            seen.add(plug.getName());
            Category category = plug.getType();
            if (category == null) {
                throw new PluginException("Invalid category for " + plug.getName());
            }
            List<Plugin> lst = plugins.get(category);
            lst.add(plug);
        }
        List<Plugin> orderedPlugins = new ArrayList<>();
        plugins.entrySet().stream().forEach((entry) -> {
            orderedPlugins.addAll(entry.getValue());
        });
        Plugin lastSorter = null;
        for (Plugin plugin : orderedPlugins) {
            if (plugin.getName().equals(pluginsConfiguration.getLastSorterPluginName())) {
                lastSorter = plugin;
                break;
            }
        }
        if (pluginsConfiguration.getLastSorterPluginName() != null && lastSorter == null) {
            throw new IOException("Unknown last plugin " + pluginsConfiguration.getLastSorterPluginName());
        }
        ImageBuilder builder = pluginsConfiguration.getImageBuilder();
        if (builder == null) {
            builder = new ImageBuilder() {

                @Override
                public DataOutputStream getJImageOutputStream() {
                    throw new PluginException("No directory setup to store files");
                }

                @Override
                public ExecutableImage getExecutableImage() {
                    throw new PluginException("No directory setup to store files");
                }

                @Override
                public void storeFiles(ModulePool files) {
                    throw new PluginException("No directory setup to store files");
                }
            };
        }
        return new ImagePluginStack(builder, orderedPlugins, lastSorter);
    }
}
