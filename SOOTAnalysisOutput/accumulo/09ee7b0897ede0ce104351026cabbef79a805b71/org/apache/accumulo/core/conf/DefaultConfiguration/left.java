package org.apache.accumulo.core.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.base.Predicate;

public class DefaultConfiguration extends AccumuloConfiguration {

    private final static Map<String, String> resolvedProps;

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
        for (Entry<String, String> entry : resolvedProps.entrySet()) if (filter.apply(entry.getKey()))
            props.put(entry.getKey(), entry.getValue());
    }
}
