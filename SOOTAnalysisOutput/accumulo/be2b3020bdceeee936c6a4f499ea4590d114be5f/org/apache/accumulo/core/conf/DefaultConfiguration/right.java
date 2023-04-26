package org.apache.accumulo.core.conf;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultConfiguration extends AccumuloConfiguration {

    private static final Map<String, String> resolvedProps = Arrays.stream(Property.values()).filter(p -> p.getType() != PropertyType.PREFIX).collect(Collectors.toMap(Property::getKey, Property::getDefaultValue));

    private DefaultConfiguration() {
    }

    public static DefaultConfiguration getInstance() {
        return new DefaultConfiguration();
    }

    @Override
    public String get(Property property) {
        return resolvedProps.get(property.getKey());
    }

    @Override
    public void getProperties(Map<String, String> props, Predicate<String> filter) {
        resolvedProps.entrySet().stream().filter(p -> filter.test(p.getKey())).forEach(e -> props.put(e.getKey(), e.getValue()));
    }

    @Override
    public boolean isPropertySet(Property prop, boolean cacheAndWatch) {
        return false;
    }
}
