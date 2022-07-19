package org.apache.accumulo.core.conf;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultConfiguration extends AccumuloConfiguration {

    private static final Map<String, String> resolvedProps = Arrays.stream(Property.values()).filter(p -> p.getType() != PropertyType.PREFIX).collect(Collectors.toMap(p -> p.getKey(), p -> p.getDefaultValue()));

    private DefaultConfiguration() {
    }

    static {
        Map<String, String> m = new HashMap<>();
        for (Property prop : Property.values()) {
            if (!prop.getType().equals(PropertyType.PREFIX)) {
                m.put(prop.getKey(), prop.getDefaultValue());
            }
        }
        resolvedProps = Collections.unmodifiableMap(m);
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
    protected String getArbitrarySystemPropertyImpl(String property) {
        return null;
    }
}