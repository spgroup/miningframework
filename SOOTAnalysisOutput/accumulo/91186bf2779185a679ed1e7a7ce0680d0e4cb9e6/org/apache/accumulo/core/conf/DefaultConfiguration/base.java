package org.apache.accumulo.core.conf;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.accumulo.core.Constants;

public class DefaultConfiguration extends AccumuloConfiguration {

    private static DefaultConfiguration instance = null;

    private Map<String, String> resolvedProps = null;

    synchronized public static DefaultConfiguration getInstance() {
        if (instance == null) {
            instance = new DefaultConfiguration();
            ConfigSanityCheck.validate(instance);
        }
        return instance;
    }

    @Override
    public String get(Property property) {
        return getResolvedProps().get(property.getKey());
    }

    private synchronized Map<String, String> getResolvedProps() {
        if (resolvedProps == null) {
            resolvedProps = new HashMap<String, String>();
            for (Property prop : Property.values()) if (!prop.getType().equals(PropertyType.PREFIX))
                resolvedProps.put(prop.getKey(), prop.getDefaultValue());
        }
        return resolvedProps;
    }

    @Override
    public void getProperties(Map<String, String> props, PropertyFilter filter) {
        for (Entry<String, String> entry : getResolvedProps().entrySet()) if (filter.accept(entry.getKey()))
            props.put(entry.getKey(), entry.getValue());
    }

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length == 2 && args[0].equals("--generate-html")) {
            new ConfigurationDocGen(new PrintStream(args[1], Constants.UTF8.name())).generateHtml();
        } else if (args.length == 2 && args[0].equals("--generate-latex")) {
            new ConfigurationDocGen(new PrintStream(args[1], Constants.UTF8.name())).generateLaTeX();
        } else {
            throw new IllegalArgumentException("Usage: " + DefaultConfiguration.class.getName() + " --generate-html <filename> | --generate-latex <filename>");
        }
    }
}
