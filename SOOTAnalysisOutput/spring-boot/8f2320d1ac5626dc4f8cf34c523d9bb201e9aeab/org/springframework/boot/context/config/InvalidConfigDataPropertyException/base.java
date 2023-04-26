package org.springframework.boot.context.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.AbstractEnvironment;

public class InvalidConfigDataPropertyException extends ConfigDataException {

    private static final Map<ConfigurationPropertyName, ConfigurationPropertyName> WARNINGS;

    static {
        Map<ConfigurationPropertyName, ConfigurationPropertyName> warnings = new LinkedHashMap<>();
        warnings.put(ConfigurationPropertyName.of("spring.profiles"), ConfigurationPropertyName.of("spring.config.activate.on-profile"));
        warnings.put(ConfigurationPropertyName.of("spring.profiles[0]"), ConfigurationPropertyName.of("spring.config.activate.on-profile"));
        WARNINGS = Collections.unmodifiableMap(warnings);
    }

    private static final Set<ConfigurationPropertyName> PROFILE_SPECIFIC_ERRORS;

    static {
        Set<ConfigurationPropertyName> errors = new LinkedHashSet<>();
        errors.add(Profiles.INCLUDE_PROFILES);
        errors.add(Profiles.INCLUDE_PROFILES.append("[0]"));
        errors.add(ConfigurationPropertyName.of(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME));
        errors.add(ConfigurationPropertyName.of(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + "[0]"));
        errors.add(ConfigurationPropertyName.of(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME));
        errors.add(ConfigurationPropertyName.of(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME + "[0]"));
        PROFILE_SPECIFIC_ERRORS = Collections.unmodifiableSet(errors);
    }

    private final ConfigurationProperty property;

    private final ConfigurationPropertyName replacement;

    private final ConfigDataResource location;

    InvalidConfigDataPropertyException(ConfigurationProperty property, boolean profileSpecific, ConfigurationPropertyName replacement, ConfigDataResource location) {
        super(getMessage(property, profileSpecific, replacement, location), null);
        this.property = property;
        this.replacement = replacement;
        this.location = location;
    }

    public ConfigurationProperty getProperty() {
        return this.property;
    }

    public ConfigDataResource getLocation() {
        return this.location;
    }

    public ConfigurationPropertyName getReplacement() {
        return this.replacement;
    }

    static void throwOrWarn(Log logger, ConfigDataEnvironmentContributor contributor) {
        ConfigurationPropertySource propertySource = contributor.getConfigurationPropertySource();
        if (propertySource != null) {
            WARNINGS.forEach((name, replacement) -> {
                ConfigurationProperty property = propertySource.getConfigurationProperty(name);
                if (property != null) {
                    logger.warn(getMessage(property, false, replacement, contributor.getResource()));
                }
            });
            if (contributor.isFromProfileSpecificImport() && !contributor.hasConfigDataOption(ConfigData.Option.IGNORE_PROFILES)) {
                PROFILE_SPECIFIC_ERRORS.forEach((name) -> {
                    ConfigurationProperty property = propertySource.getConfigurationProperty(name);
                    if (property != null) {
                        throw new InvalidConfigDataPropertyException(property, true, null, contributor.getResource());
                    }
                });
            }
        }
    }

    private static String getMessage(ConfigurationProperty property, boolean profileSpecific, ConfigurationPropertyName replacement, ConfigDataResource location) {
        StringBuilder message = new StringBuilder("Property '");
        message.append(property.getName());
        if (location != null) {
            message.append("' imported from location '");
            message.append(location);
        }
        message.append("' is invalid");
        if (profileSpecific) {
            message.append(" in a profile specific resource");
        }
        if (replacement != null) {
            message.append(" and should be replaced with '");
            message.append(replacement);
            message.append("'");
        }
        if (property.getOrigin() != null) {
            message.append(" [origin: ");
            message.append(property.getOrigin());
            message.append("]");
        }
        return message.toString();
    }
}
