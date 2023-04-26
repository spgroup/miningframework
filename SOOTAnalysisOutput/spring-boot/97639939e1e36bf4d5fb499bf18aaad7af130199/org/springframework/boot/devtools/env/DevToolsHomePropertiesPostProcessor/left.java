package org.springframework.boot.devtools.env;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.system.DevToolsEnablementDeducer;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class DevToolsHomePropertiesPostProcessor implements EnvironmentPostProcessor {

    private static final String LEGACY_FILE_NAME = ".spring-boot-devtools.properties";

    private static final String[] FILE_NAMES = new String[] { "spring-boot-devtools.yml", "spring-boot-devtools.yaml", "spring-boot-devtools.properties" };

    private static final String CONFIG_PATH = "/.config/spring-boot/";

    private static final Set<PropertySourceLoader> PROPERTY_SOURCE_LOADERS;

    static {
        Set<PropertySourceLoader> propertySourceLoaders = new HashSet<>();
        propertySourceLoaders.add(new PropertiesPropertySourceLoader());
        if (ClassUtils.isPresent("org.yaml.snakeyaml.Yaml", null)) {
            propertySourceLoaders.add(new YamlPropertySourceLoader());
        }
        PROPERTY_SOURCE_LOADERS = Collections.unmodifiableSet(propertySourceLoaders);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (DevToolsEnablementDeducer.shouldEnable(Thread.currentThread())) {
            List<PropertySource<?>> propertySources = getPropertySources();
            if (propertySources.isEmpty()) {
                addPropertySource(propertySources, LEGACY_FILE_NAME, (file) -> "devtools-local");
            }
            propertySources.forEach(environment.getPropertySources()::addFirst);
        }
    }

    private List<PropertySource<?>> getPropertySources() {
        List<PropertySource<?>> propertySources = new ArrayList<>();
        for (String fileName : FILE_NAMES) {
            addPropertySource(propertySources, CONFIG_PATH + fileName, this::getPropertySourceName);
        }
        return propertySources;
    }

    private String getPropertySourceName(File file) {
        return "devtools-local: [" + file.toURI() + "]";
    }

    private void addPropertySource(List<PropertySource<?>> propertySources, String fileName, Function<File, String> propertySourceNamer) {
        File home = getHomeDirectory();
        File file = (home != null) ? new File(home, fileName) : null;
        FileSystemResource resource = (file != null) ? new FileSystemResource(file) : null;
        if (resource != null && resource.exists() && resource.isFile()) {
            addPropertySource(propertySources, resource, propertySourceNamer);
        }
    }

    private void addPropertySource(List<PropertySource<?>> propertySources, FileSystemResource resource, Function<File, String> propertySourceNamer) {
        try {
            String name = propertySourceNamer.apply(resource.getFile());
            for (PropertySourceLoader loader : PROPERTY_SOURCE_LOADERS) {
                if (canLoadFileExtension(loader, resource.getFilename())) {
                    propertySources.addAll(loader.load(name, resource));
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + resource.getFilename(), ex);
        }
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
        return Arrays.stream(loader.getFileExtensions()).anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(name, fileExtension));
    }

    protected File getHomeDirectory() {
        String home = System.getProperty("user.home");
        if (StringUtils.hasLength(home)) {
            return new File(home);
        }
        return null;
    }
}
