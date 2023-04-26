package org.springframework.boot.logging;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class LoggingApplicationListener implements SmartApplicationListener {

    public static final String CONFIG_PROPERTY = "logging.config";

    public static final String PATH_PROPERTY = LogFile.PATH_PROPERTY;

    public static final String FILE_PROPERTY = LogFile.FILE_PROPERTY;

    public static final String PID_KEY = "PID";

    private static MultiValueMap<LogLevel, String> LOG_LEVEL_LOGGERS;

    static {
        LOG_LEVEL_LOGGERS = new LinkedMultiValueMap<LogLevel, String>();
        LOG_LEVEL_LOGGERS.add(LogLevel.DEBUG, "org.springframework.boot");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.springframework");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.apache.tomcat");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.apache.catalina");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.eclipse.jetty");
        LOG_LEVEL_LOGGERS.add(LogLevel.TRACE, "org.hibernate.tool.hbm2ddl");
        LOG_LEVEL_LOGGERS.add(LogLevel.DEBUG, "org.hibernate.SQL");
    }

    private static Class<?>[] EVENT_TYPES = { ApplicationStartedEvent.class, ApplicationEnvironmentPreparedEvent.class, ContextClosedEvent.class };

    private static Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };

    private final Log logger = LogFactory.getLog(getClass());

    private LoggingSystem loggingSystem;

    private int order = Ordered.HIGHEST_PRECEDENCE + 11;

    private boolean parseArgs = true;

    private LogLevel springBootLogging = null;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return isAssignableFrom(eventType, EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        for (Class<?> supportedType : supportedTypes) {
            if (supportedType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartedEvent) {
            onApplicationStartedEvent((ApplicationStartedEvent) event);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent();
        }
    }

    private void onApplicationStartedEvent(ApplicationStartedEvent event) {
        this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        this.loggingSystem.beforeInitialize();
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        if (this.loggingSystem == null) {
            this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        }
        initialize(event.getEnvironment(), event.getSpringApplication().getClassLoader());
    }

    private void onContextClosedEvent() {
        if (this.loggingSystem != null) {
            this.loggingSystem.cleanUp();
        }
    }

    protected void initialize(ConfigurableEnvironment environment, ClassLoader classLoader) {
        if (System.getProperty(PID_KEY) == null) {
            System.setProperty(PID_KEY, new ApplicationPid().toString());
        }
        initializeEarlyLoggingLevel(environment);
        initializeSystem(environment, this.loggingSystem);
        initializeFinalLoggingLevels(environment, this.loggingSystem);
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

    private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system) {
        LogFile logFile = LogFile.get(environment);
        String logConfig = environment.getProperty(CONFIG_PROPERTY);
        if (StringUtils.hasLength(logConfig)) {
            try {
                ResourceUtils.getURL(logConfig).openStream().close();
                system.initialize(logConfig, logFile);
            } catch (Exception ex) {
                this.logger.warn("Logging environment value '" + logConfig + "' cannot be opened and will be ignored " + "(using default location instead)");
                system.initialize(null, logFile);
            }
        } else {
            system.initialize(null, logFile);
        }
    }

    private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
        if (this.springBootLogging != null) {
            initializeLogLevel(system, this.springBootLogging);
        }
        setLogLevels(system, environment);
    }

    protected void initializeLogLevel(LoggingSystem system, LogLevel level) {
        List<String> loggers = LOG_LEVEL_LOGGERS.get(level);
        if (loggers != null) {
            for (String logger : loggers) {
                system.setLogLevel(logger, level);
            }
        }
    }

    protected void setLogLevels(LoggingSystem system, Environment environment) {
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
