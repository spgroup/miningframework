package org.springframework.boot.cli;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

public class CliTesterSpringApplication extends SpringApplication {

    static {
        if (ClassUtils.isPresent("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory", CliTesterSpringApplication.class.getClassLoader())) {
            TomcatURLStreamHandlerFactory.disable();
        }
    }

    public CliTesterSpringApplication(Class<?>... sources) {
        super(sources);
    }

    @Override
    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        context.addApplicationListener(new WebServerPortFileWriter());
    }
}