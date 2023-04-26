package org.apache.accumulo.core.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultConfiguration extends AccumuloConfiguration {

    private final static Map<String, String> resolvedProps;

    static {
        Map<String, String> m = new HashMap<String, String>();
        for (Property prop : Property.values()) {
            if (!prop.getType().equals(PropertyType.PREFIX)) {
                m.put(prop.getKey(), prop.getDefaultValue());
            }
        }
        ConfigSanityCheck.validate(m.entrySet());
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
    public void getProperties(Map<String, String> props, PropertyFilter filter) {
        for (Entry<String, String> entry : resolvedProps.entrySet()) if (filter.accept(entry.getKey()))
            props.put(entry.getKey(), entry.getValue());
    }
}
