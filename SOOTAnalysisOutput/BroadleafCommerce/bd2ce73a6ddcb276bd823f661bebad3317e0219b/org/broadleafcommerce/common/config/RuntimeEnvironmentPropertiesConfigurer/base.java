package org.broadleafcommerce.common.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class RuntimeEnvironmentPropertiesConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    private static final Log LOG = LogFactory.getLog(RuntimeEnvironmentPropertiesConfigurer.class);

    protected static Set<String> defaultEnvironments = new LinkedHashSet<String>();

    protected static Set<Resource> blcPropertyLocations = new LinkedHashSet<Resource>();

    protected static Set<Resource> defaultPropertyLocations = new LinkedHashSet<Resource>();

    static {
        defaultEnvironments.add("production");
        defaultEnvironments.add("staging");
        defaultEnvironments.add("integrationqa");
        defaultEnvironments.add("integrationdev");
        defaultEnvironments.add("development");
        defaultEnvironments.add("local");
        blcPropertyLocations.add(new ClassPathResource("config/bc/admin/"));
        blcPropertyLocations.add(new ClassPathResource("config/bc/"));
        blcPropertyLocations.add(new ClassPathResource("config/bc/cms/"));
        blcPropertyLocations.add(new ClassPathResource("config/bc/web/"));
        defaultPropertyLocations.add(new ClassPathResource("runtime-properties/"));
    }

    protected String defaultEnvironment = "development";

    protected RuntimeEnvironmentKeyResolver keyResolver;

    protected Set<String> environments = Collections.emptySet();

    protected Set<Resource> propertyLocations;

    protected StringValueResolver stringValueResolver;

    public RuntimeEnvironmentPropertiesConfigurer() {
        super();
        setIgnoreUnresolvablePlaceholders(true);
    }

    public void afterPropertiesSet() throws IOException {
        if (environments == null || environments.size() == 0) {
            environments = defaultEnvironments;
        }
        Set<Resource> combinedLocations = new LinkedHashSet<Resource>();
        combinedLocations.addAll(defaultPropertyLocations);
        if (propertyLocations != null && propertyLocations.size() > 0) {
            combinedLocations.addAll(propertyLocations);
        }
        propertyLocations = combinedLocations;
        if (!environments.contains(defaultEnvironment)) {
            throw new AssertionError("Default environment '" + defaultEnvironment + "' not listed in environment list");
        }
        if (keyResolver == null) {
            keyResolver = new SystemPropertyRuntimeEnvironmentKeyResolver();
        }
        String environment = determineEnvironment();
        Resource[] blcPropertiesLocation = createBroadleafResource();
        Resource[] sharedPropertiesLocation = createSharedPropertiesResource(environment);
        Resource[] sharedCommonLocation = createSharedCommonResource();
        Resource[] propertiesLocation = createPropertiesResource(environment);
        Resource[] commonLocation = createCommonResource();
        ArrayList<Resource> allLocations = new ArrayList<Resource>();
        for (Resource resource : blcPropertiesLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        for (Resource resource : sharedCommonLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        for (Resource resource : sharedPropertiesLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        for (Resource resource : commonLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        for (Resource resource : propertiesLocation) {
            if (resource.exists()) {
                allLocations.add(resource);
            }
        }
        if (LOG.isDebugEnabled()) {
            Properties props = new Properties();
            for (Resource resource : allLocations) {
                if (resource.exists()) {
                    props = new Properties(props);
                    props.load(resource.getInputStream());
                    for (Entry<Object, Object> entry : props.entrySet()) {
                        LOG.debug("Read " + entry.getKey() + " as " + entry.getValue());
                    }
                } else {
                    LOG.debug("Unable to locate resource: " + resource.getFilename());
                }
            }
        }
        setLocations(allLocations.toArray(new Resource[] {}));
    }

    protected Resource[] createSharedPropertiesResource(String environment) throws IOException {
        String fileName = environment.toString().toLowerCase() + "-shared.properties";
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative(fileName);
            index++;
        }
        return resources;
    }

    protected Resource[] createBroadleafResource() throws IOException {
        Resource[] resources = new Resource[blcPropertyLocations.size()];
        int index = 0;
        for (Resource resource : blcPropertyLocations) {
            resources[index] = resource.createRelative("common.properties");
            index++;
        }
        return resources;
    }

    protected Resource[] createSharedCommonResource() throws IOException {
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative("common-shared.properties");
            index++;
        }
        return resources;
    }

    protected Resource[] createPropertiesResource(String environment) throws IOException {
        String fileName = environment.toString().toLowerCase() + ".properties";
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative(fileName);
            index++;
        }
        return resources;
    }

    protected Resource[] createCommonResource() throws IOException {
        Resource[] resources = new Resource[propertyLocations.size()];
        int index = 0;
        for (Resource resource : propertyLocations) {
            resources[index] = resource.createRelative("common.properties");
            index++;
        }
        return resources;
    }

    public String determineEnvironment() {
        String environment = keyResolver.resolveRuntimeEnvironmentKey();
        if (environment == null) {
            LOG.warn("Unable to determine runtime environment, using default environment '" + defaultEnvironment + "'");
            return defaultEnvironment;
        }
        return environment.toLowerCase();
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        stringValueResolver = new PlaceholderResolvingStringValueResolver(props);
    }

    public void setDefaultEnvironment(String defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    public String getDefaultEnvironment() {
        return defaultEnvironment;
    }

    public void setKeyResolver(RuntimeEnvironmentKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public void setPropertyLocations(Set<Resource> propertyLocations) {
        this.propertyLocations = propertyLocations;
    }

    private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

        private final PropertyPlaceholderHelper helper;

        private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

        public PlaceholderResolvingStringValueResolver(Properties props) {
            this.helper = new PropertyPlaceholderHelper("${", "}", ":", true);
            this.resolver = new PropertyPlaceholderConfigurerResolver(props);
        }

        public String resolveStringValue(String strVal) throws BeansException {
            String value = this.helper.replacePlaceholders(strVal, this.resolver);
            return (value.equals("") ? null : value);
        }
    }

    private class PropertyPlaceholderConfigurerResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final Properties props;

        private PropertyPlaceholderConfigurerResolver(Properties props) {
            this.props = props;
        }

        public String resolvePlaceholder(String placeholderName) {
            return RuntimeEnvironmentPropertiesConfigurer.this.resolvePlaceholder(placeholderName, props, 1);
        }
    }

    public StringValueResolver getStringValueResolver() {
        return stringValueResolver;
    }
}
