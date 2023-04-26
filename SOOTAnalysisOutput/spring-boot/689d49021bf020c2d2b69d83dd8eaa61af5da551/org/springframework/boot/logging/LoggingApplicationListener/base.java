package org.springframework.boot.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.ApplicationPid;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class LoggingApplicationListener implements SmartApplicationListener {

    private static final Map<String, String> ENVIRONMENT_SYSTEM_PROPERTY_MAPPING;

    public static final String PID_KEY = "PID";

    static {
        ENVIRONMENT_SYSTEM_PROPERTY_MAPPING = new HashMap<String, String>();
        ENVIRONMENT_SYSTEM_PROPERTY_MAPPING.put("logging.file", "LOG_FILE");
        ENVIRONMENT_SYSTEM_PROPERTY_MAPPING.put("logging.path", "LOG_PATH");
        ENVIRONMENT_SYSTEM_PROPERTY_MAPPING.put(PID_KEY, PID_KEY);
    }

    private static MultiValueMap<LogLevel, String> LOG_LEVEL_LOGGERS;

    static {
        LOG_LEVEL_LOGGERS = new LinkedMultiValueMap<LogLevel, String>();
        LOG_LEVEL_LOGGERS.add(LogLevel.DEBUG, "org.springframework.boot");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.springframework");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.apache.tomcat");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.apache.catalina");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.eclipse.jetty");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.hibernate.tool.hbm2ddl");
    }

    private static Class<?>[] EVENT_TYPES = { ApplicationStartedEvent.class, ApplicationEnvironmentPreparedEvent.class };

    private final Log logger = LogFactory.getLog(getClass());

    private int order = Ordered.HIGHEST_PRECEDENCE + 11;

    private boolean parseArgs = true;

    private LogLevel springBootLogging = null;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        for (Class<?> type : EVENT_TYPES) {
            if (type.isAssignableFrom(eventType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return SpringApplication.class.isAssignableFrom(sourceType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ApplicationEnvironmentPreparedEvent available = (ApplicationEnvironmentPreparedEvent) event;
            initialize(available.getEnvironment(), available.getSpringApplication().getClassLoader());
        } else {
            if (System.getProperty(PID_KEY) == null) {
                System.setProperty(PID_KEY, new ApplicationPid().toString());
            }
            LoggingSystem loggingSystem = LoggingSystem.get(ClassUtils.getDefaultClassLoader());
            loggingSystem.beforeInitialize();
        }
    }

    protected void initialize(ConfigurableEnvironment environment, ClassLoader classLoader) {
        initializeEarlyLoggingLevel(environment);
        cleanLogTempProperty();
        LoggingSystem system = LoggingSystem.get(classLoader);
        boolean systemEnvironmentChanged = mapSystemPropertiesFromSpring(environment);
        if (systemEnvironmentChanged) {
            system.beforeInitialize();
        }
        initializeSystem(environment, system);
        initializeFinalLoggingLevels(environment, system);
    }

    private void initializeEarlyLoggingLevel(ConfigurableEnvironment environment) {
        if (this.parseArgs && this.springBootLogging == null) {
            if (environment.containsProperty("debug")) {
                this.springBootLogging = LogLevel.DEBUG;
            }
            if (environment.containsProperty("trace")) {
                this.springBootLogging = LogLevel.TRACE;
            }
        }
    }

    private void cleanLogTempProperty() {
        if (!StringUtils.hasLength(System.getProperty("LOG_TEMP"))) {
            String path = System.getProperty("java.io.tmpdir");
            path = StringUtils.cleanPath(path);
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            System.setProperty("LOG_TEMP", path);
        }
    }

    private boolean mapSystemPropertiesFromSpring(Environment environment) {
        boolean changed = false;
        for (Map.Entry<String, String> mapping : ENVIRONMENT_SYSTEM_PROPERTY_MAPPING.entrySet()) {
            String springName = mapping.getKey();
            String systemName = mapping.getValue();
            if (environment.containsProperty(springName)) {
                System.setProperty(systemName, environment.getProperty(springName));
                changed = true;
            }
        }
        return changed;
    }

    private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system) {
        if (environment.containsProperty("logging.config")) {
            String value = environment.getProperty("logging.config");
            try {
                ResourceUtils.getURL(value).openStream().close();
                system.initialize(value);
            } catch (Exception ex) {
                this.logger.warn("Logging environment value '" + value + "' cannot be opened and will be ignored " + "(using default location instead)");
                system.initialize();
            }
        } else {
            system.initialize();
        }
    }

    private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
        if (this.springBootLogging != null) {
            initializeLogLevel(system, this.springBootLogging);
        }
        setLogLevels(system, environment);
    }

    public void setLogLevels(LoggingSystem system, Environment environment) {
        Map<String, Object> levels = new RelaxedPropertyResolver(environment).getSubProperties("logging.level.");
        for (Entry<String, Object> entry : levels.entrySet()) {
            setLogLevel(system, environment, entry.getKey(), entry.getValue().toString());
        }
    }

    private void setLogLevel(LoggingSystem system, Environment environment, String name, String level) {
        try {
            if (name.equalsIgnoreCase("root")) {
                name = null;
            }
            level = environment.resolvePlaceholders(level);
            system.setLogLevel(name, LogLevel.valueOf(level));
        } catch (RuntimeException ex) {
            this.logger.error("Cannot set level: " + level + " for '" + name + "'");
        }
    }

    protected void initializeLogLevel(LoggingSystem system, LogLevel level) {
        List<String> loggers = LOG_LEVEL_LOGGERS.get(level);
        if (loggers != null) {
            for (String logger : loggers) {
                system.setLogLevel(logger, level);
            }
        }
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setSpringBootLogging(LogLevel springBootLogging) {
        this.springBootLogging = springBootLogging;
    }

    public void setParseArgs(boolean parseArgs) {
        this.parseArgs = parseArgs;
    }
}
