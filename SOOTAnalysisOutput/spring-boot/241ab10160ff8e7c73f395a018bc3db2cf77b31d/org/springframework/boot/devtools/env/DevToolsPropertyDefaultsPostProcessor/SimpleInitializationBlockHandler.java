package org.springframework.boot.devtools.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

@Order(Ordered.LOWEST_PRECEDENCE)
public class DevToolsPropertyDefaultsPostProcessor implements EnvironmentPostProcessor {

    private static final Map<String, Object> PROPERTIES;

    static {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("spring.thymeleaf.cache", "false");
        properties.put("spring.freemarker.cache", "false");
        properties.put("spring.groovy.template.cache", "false");
        properties.put("spring.mustache.cache", "false");
        properties.put("server.session.persistent", "true");
        properties.put("spring.h2.console.enabled", "true");
        properties.put("spring.resources.cache-period", "0");
        properties.put("spring.template.provider.cache", "false");
        properties.put("spring.mvc.log-resolved-exception", "true");
        PROPERTIES = Collections.unmodifiableMap(properties);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isLocalApplication(environment)) {
            PropertySource<?> propertySource = new MapPropertySource("refresh", PROPERTIES);
            environment.getPropertySources().addLast(propertySource);
        }
    }

    private boolean isLocalApplication(ConfigurableEnvironment environment) {
        return environment.getPropertySources().get("remoteUrl") == null;
    }
}