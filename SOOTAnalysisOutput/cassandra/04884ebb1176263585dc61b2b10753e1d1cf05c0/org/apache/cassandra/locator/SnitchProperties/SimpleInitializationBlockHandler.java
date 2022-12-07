package org.apache.cassandra.locator;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.cassandra.io.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnitchProperties {

    private static final Logger logger = LoggerFactory.getLogger(SnitchProperties.class);

    public static final String RACKDC_PROPERTY_FILENAME = "cassandra-rackdc.properties";

    private Properties properties;

    public SnitchProperties() {
        properties = new Properties();
        InputStream stream = SnitchProperties.class.getClassLoader().getResourceAsStream(RACKDC_PROPERTY_FILENAME);
        try {
            properties.load(stream);
        } catch (Exception e) {
            logger.warn("Unable to read {}", RACKDC_PROPERTY_FILENAME);
        } finally {
            FileUtils.closeQuietly(stream);
        }
    }

    static {
        properties = new Properties();
        InputStream stream = null;
        String configURL = System.getProperty("cassandra.rackdc.properties");
        try {
            URL url = new URL(configURL);
            if (configURL == null)
                url = SnitchProperties.class.getClassLoader().getResource("cassandra-rackdc.properties");
            stream = url.openStream();
            properties.load(stream);
        } catch (Exception e) {
            logger.warn("Unable to read " + RACKDC_PROPERTY_FILENAME);
        } finally {
            FileUtils.closeQuietly(stream);
        }
    }

    public String get(String propertyName, String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }
}