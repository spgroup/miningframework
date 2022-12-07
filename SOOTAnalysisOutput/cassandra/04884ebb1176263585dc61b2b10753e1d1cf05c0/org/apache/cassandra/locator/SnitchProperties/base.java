package org.apache.cassandra.locator;

import java.io.InputStream;
import java.util.Properties;
import org.apache.cassandra.io.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnitchProperties {

    private static final Logger logger = LoggerFactory.getLogger(SnitchProperties.class);

    public static final String RACKDC_PROPERTY_FILENAME = "cassandra-rackdc.properties";

    private static Properties properties = new Properties();

    static {
        InputStream stream = SnitchProperties.class.getClassLoader().getResourceAsStream(RACKDC_PROPERTY_FILENAME);
        try {
            properties.load(stream);
        } catch (Exception e) {
            logger.warn("Unable to read " + RACKDC_PROPERTY_FILENAME);
        } finally {
            FileUtils.closeQuietly(stream);
        }
    }

    public static String get(String propertyName, String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }
}
