package org.springframework.boot.logging;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public abstract class LoggingSystem {

    public static final String SYSTEM_PROPERTY = LoggingSystem.class.getName();

    public static final String NONE = "none";

    public static final String ROOT_LOGGER_NAME = "ROOT";

    private static final LoggingSystemFactory SYSTEM_FACTORY = LoggingSystemFactory.fromSpringFactories();

<<<<<<< MINE
    public LoggingSystemProperties getSystemProperties(ConfigurableEnvironment environment) {
        return new LoggingSystemProperties(environment);
=======
    static {
        Map<String, String> systems = new LinkedHashMap<>();
        systems.put("ch.qos.logback.classic.LoggerContext", "org.springframework.boot.logging.logback.LogbackLoggingSystem");
        systems.put("org.apache.logging.log4j.core.impl.Log4jContextFactory", "org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
        systems.put("java.util.logging.LogManager", "org.springframework.boot.logging.java.JavaLoggingSystem");
        SYSTEMS = Collections.unmodifiableMap(systems);
>>>>>>> YOURS
    }

    public abstract void beforeInitialize();

    public void initialize(LoggingInitializationContext initializationContext, String configLocation, LogFile logFile) {
    }

    public void cleanUp() {
    }

    public Runnable getShutdownHandler() {
        return null;
    }

    public Set<LogLevel> getSupportedLogLevels() {
        return EnumSet.allOf(LogLevel.class);
    }

    public void setLogLevel(String loggerName, LogLevel level) {
        throw new UnsupportedOperationException("Unable to set log level");
    }

    public List<LoggerConfiguration> getLoggerConfigurations() {
        throw new UnsupportedOperationException("Unable to get logger configurations");
    }

    public LoggerConfiguration getLoggerConfiguration(String loggerName) {
        throw new UnsupportedOperationException("Unable to get logger configuration");
    }

    public static LoggingSystem get(ClassLoader classLoader) {
        String loggingSystemClassName = System.getProperty(SYSTEM_PROPERTY);
        if (StringUtils.hasLength(loggingSystemClassName)) {
            if (NONE.equals(loggingSystemClassName)) {
                return new NoOpLoggingSystem();
            }
            return get(classLoader, loggingSystemClassName);
        }
        LoggingSystem loggingSystem = SYSTEM_FACTORY.getLoggingSystem(classLoader);
        Assert.state(loggingSystem != null, "No suitable logging system located");
        return loggingSystem;
    }

    private static LoggingSystem get(ClassLoader classLoader, String loggingSystemClassName) {
        try {
            Class<?> systemClass = ClassUtils.forName(loggingSystemClassName, classLoader);
            Constructor<?> constructor = systemClass.getDeclaredConstructor(ClassLoader.class);
            constructor.setAccessible(true);
            return (LoggingSystem) constructor.newInstance(classLoader);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static class NoOpLoggingSystem extends LoggingSystem {

        @Override
        public void beforeInitialize() {
        }

        @Override
        public void setLogLevel(String loggerName, LogLevel level) {
        }

        @Override
        public List<LoggerConfiguration> getLoggerConfigurations() {
            return Collections.emptyList();
        }

        @Override
        public LoggerConfiguration getLoggerConfiguration(String loggerName) {
            return null;
        }
    }
}
