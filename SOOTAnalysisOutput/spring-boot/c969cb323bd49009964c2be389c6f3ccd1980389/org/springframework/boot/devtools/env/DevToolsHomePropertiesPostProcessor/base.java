package org.springframework.boot.devtools.env;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

public class DevToolsHomePropertiesPostProcessor implements EnvironmentPostProcessor {

    private static final String FILE_NAME = ".spring-boot-devtools.properties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File home = getHomeFolder();
        File propertyFile = (home != null) ? new File(home, FILE_NAME) : null;
        if (propertyFile != null && propertyFile.exists() && propertyFile.isFile()) {
            FileSystemResource resource = new FileSystemResource(propertyFile);
            Properties properties;
            try {
                properties = PropertiesLoaderUtils.loadProperties(resource);
                environment.getPropertySources().addFirst(new PropertiesPropertySource("devtools-local", properties));
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load " + FILE_NAME, ex);
            }
        }
    }

    protected File getHomeFolder() {
        String home = System.getProperty("user.home");
        if (StringUtils.hasLength(home)) {
            return new File(home);
        }
        return null;
    }
}
