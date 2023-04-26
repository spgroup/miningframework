package jdk.tools.jlink.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jdk.tools.jlink.plugin.ExecutableImage;
import jdk.tools.jlink.builder.ImageBuilder;
import jdk.tools.jlink.Jlink;
import jdk.tools.jlink.plugin.Plugin;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.plugin.Plugin.Category;
import jdk.tools.jlink.plugin.ModulePool;
import jdk.tools.jlink.plugin.PostProcessorPlugin;
import jdk.tools.jlink.plugin.TransformerPlugin;

public final class ImagePluginConfiguration {

    private static final List<Plugin.Category> CATEGORIES_ORDER = new ArrayList<>();

    static {
        CATEGORIES_ORDER.add(Plugin.Category.FILTER);
        CATEGORIES_ORDER.add(Plugin.Category.TRANSFORMER);
        CATEGORIES_ORDER.add(Plugin.Category.MODULEINFO_TRANSFORMER);
        CATEGORIES_ORDER.add(Plugin.Category.SORTER);
        CATEGORIES_ORDER.add(Plugin.Category.COMPRESSOR);
        CATEGORIES_ORDER.add(Plugin.Category.METAINFO_ADDER);
        CATEGORIES_ORDER.add(Plugin.Category.VERIFIER);
        CATEGORIES_ORDER.add(Plugin.Category.PROCESSOR);
        CATEGORIES_ORDER.add(Plugin.Category.PACKAGER);
    }

    private ImagePluginConfiguration() {
    }

    public static ImagePluginStack parseConfiguration(Jlink.PluginsConfiguration pluginsConfiguration) throws Exception {
        if (pluginsConfiguration == null) {
            return new ImagePluginStack();
        }
        Map<Plugin.Category, List<Plugin>> plugins = new LinkedHashMap<>();
        for (Plugin.Category cat : CATEGORIES_ORDER) {
            plugins.put(cat, new ArrayList<>());
        }
        List<String> seen = new ArrayList<>();
        for (Plugin plug : pluginsConfiguration.getPlugins()) {
            if (seen.contains(plug.getName())) {
                throw new Exception("Plugin " + plug.getName() + " added more than once to stack ");
            }
            seen.add(plug.getName());
            Category category = Utils.getCategory(plug);
            if (category == null) {
                throw new PluginException("Invalid category for " + plug.getName());
            }
            List<Plugin> lst = plugins.get(category);
            lst.add(plug);
        }
        List<TransformerPlugin> transformerPlugins = new ArrayList<>();
        List<PostProcessorPlugin> postProcessingPlugins = new ArrayList<>();
        for (Entry<Plugin.Category, List<Plugin>> entry : plugins.entrySet()) {
            List<Plugin> orderedPlugins = PluginOrderingGraph.sort(entry.getValue());
            Category category = entry.getKey();
            for (Plugin p : orderedPlugins) {
                if (category.isPostProcessor()) {
                    @SuppressWarnings("unchecked")
                    PostProcessorPlugin pp = (PostProcessorPlugin) p;
                    postProcessingPlugins.add(pp);
                } else {
                    @SuppressWarnings("unchecked")
                    TransformerPlugin trans = (TransformerPlugin) p;
                    transformerPlugins.add(trans);
                }
            }
        }
        Plugin lastSorter = null;
        for (Plugin plugin : transformerPlugins) {
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
        return new ImagePluginStack(builder, transformerPlugins, lastSorter, postProcessingPlugins);
    }
}
