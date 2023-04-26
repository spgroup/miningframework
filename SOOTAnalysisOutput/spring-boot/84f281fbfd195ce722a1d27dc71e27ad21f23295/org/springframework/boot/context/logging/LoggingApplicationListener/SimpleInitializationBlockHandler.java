package org.springframework.boot.context.logging;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerGroup;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.log.LogMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public class LoggingApplicationListener implements GenericApplicationListener {

    private static final ConfigurationPropertyName LOGGING_LEVEL = ConfigurationPropertyName.of("logging.level");

    private static final ConfigurationPropertyName LOGGING_GROUP = ConfigurationPropertyName.of("logging.group");

    private static final Bindable<Map<String, LogLevel>> STRING_LOGLEVEL_MAP = Bindable.mapOf(String.class, LogLevel.class);

    private static final Bindable<Map<String, List<String>>> STRING_STRINGS_MAP = Bindable.of(ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, String.class).asMap());

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;

    public static final String CONFIG_PROPERTY = "logging.config";

    public static final String REGISTER_SHUTDOWN_HOOK_PROPERTY = "logging.register-shutdown-hook";

    public static final String LOGGING_SYSTEM_BEAN_NAME = "springBootLoggingSystem";

    public static final String LOG_FILE_BEAN_NAME = "springBootLogFile";

    public static final String LOGGER_GROUPS_BEAN_NAME = "springBootLoggerGroups";

    private static final Map<String, List<String>> DEFAULT_GROUP_LOGGERS;

    static {
        MultiValueMap<String, String> loggers = new LinkedMultiValueMap<>();
        loggers.add("web", "org.springframework.core.codec");
        loggers.add("web", "org.springframework.http");
        loggers.add("web", "org.springframework.web");
        loggers.add("web", "org.springframework.boot.actuate.endpoint.web");
        loggers.add("web", "org.springframework.boot.web.servlet.ServletContextInitializerBeans");
        loggers.add("sql", "org.springframework.jdbc.core");
        loggers.add("sql", "org.hibernate.SQL");
        loggers.add("sql", "org.jooq.tools.LoggerListener");
        DEFAULT_GROUP_LOGGERS = Collections.unmodifiableMap(loggers);
    }

    private static final Map<LogLevel, List<String>> SPRING_BOOT_LOGGING_LOGGERS;

    static {
        MultiValueMap<LogLevel, String> loggers = new LinkedMultiValueMap<>();
        loggers.add(LogLevel.DEBUG, "sql");
        loggers.add(LogLevel.DEBUG, "web");
        loggers.add(LogLevel.DEBUG, "org.springframework.boot");
        loggers.add(LogLevel.TRACE, "org.springframework");
        loggers.add(LogLevel.TRACE, "org.apache.tomcat");
        loggers.add(LogLevel.TRACE, "org.apache.catalina");
        loggers.add(LogLevel.TRACE, "org.eclipse.jetty");
        loggers.add(LogLevel.TRACE, "org.hibernate.tool.hbm2ddl");
        SPRING_BOOT_LOGGING_LOGGERS = Collections.unmodifiableMap(loggers);
    }

    private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class, ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ContextClosedEvent.class, ApplicationFailedEvent.class };

    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };

    private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

    private final Log logger = LogFactory.getLog(getClass());

    private LoggingSystem loggingSystem;

    private LogFile logFile;

    private LoggerGroups loggerGroups;

    private int order = DEFAULT_ORDER;

    private boolean parseArgs = true;

    private LogLevel springBootLogging = null;

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            onApplicationStartingEvent((ApplicationStartingEvent) event);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        } else if (event instanceof ApplicationPreparedEvent) {
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        } else if (event instanceof ContextClosedEvent && ((ContextClosedEvent) event).getApplicationContext().getParent() == null) {
            onContextClosedEvent();
        } else if (event instanceof ApplicationFailedEvent) {
            onApplicationFailedEvent();
        }
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        this.loggingSystem.beforeInitialize();
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        if (this.loggingSystem == null) {
            this.loggingSystem = LoggingSystem.get(event.getSpringApplication().getClassLoader());
        }
        initialize(event.getEnvironment(), event.getSpringApplication().getClassLoader());
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableListableBeanFactory beanFactory = event.getApplicationContext().getBeanFactory();
        if (!beanFactory.containsBean(LOGGING_SYSTEM_BEAN_NAME)) {
            beanFactory.registerSingleton(LOGGING_SYSTEM_BEAN_NAME, this.loggingSystem);
        }
        if (this.logFile != null && !beanFactory.containsBean(LOG_FILE_BEAN_NAME)) {
            beanFactory.registerSingleton(LOG_FILE_BEAN_NAME, this.logFile);
        }
        if (this.loggerGroups != null && !beanFactory.containsBean(LOGGER_GROUPS_BEAN_NAME)) {
            beanFactory.registerSingleton(LOGGER_GROUPS_BEAN_NAME, this.loggerGroups);
        }
    }

    private void onContextClosedEvent() {
        if (this.loggingSystem != null) {
            this.loggingSystem.cleanUp();
        }
    }

    private void onApplicationFailedEvent() {
        if (this.loggingSystem != null) {
            this.loggingSystem.cleanUp();
        }
    }

    protected void initialize(ConfigurableEnvironment environment, ClassLoader classLoader) {
        new LoggingSystemProperties(environment).apply();
        this.logFile = LogFile.get(environment);
        if (this.logFile != null) {
            this.logFile.applyToSystemProperties();
        }
        this.loggerGroups = new LoggerGroups(DEFAULT_GROUP_LOGGERS);
        initializeEarlyLoggingLevel(environment);
        initializeSystem(environment, this.loggingSystem, this.logFile);
        initializeFinalLoggingLevels(environment, this.loggingSystem);
        registerShutdownHookIfNecessary(environment, this.loggingSystem);
    }

    private void initializeEarlyLoggingLevel(ConfigurableEnvironment environment) {
        if (this.parseArgs && this.springBootLogging == null) {
            if (isSet(environment, "debug")) {
                this.springBootLogging = LogLevel.DEBUG;
            }
            if (isSet(environment, "trace")) {
                this.springBootLogging = LogLevel.TRACE;
            }
        }
    }

    private boolean isSet(ConfigurableEnvironment environment, String property) {
        String value = environment.getProperty(property);
        return (value != null && !value.equals("false"));
    }

    private void initializeSystem(ConfigurableEnvironment environment, LoggingSystem system, LogFile logFile) {
        LoggingInitializationContext initializationContext = new LoggingInitializationContext(environment);
        String logConfig = environment.getProperty(CONFIG_PROPERTY);
        if (ignoreLogConfig(logConfig)) {
            system.initialize(initializationContext, null, logFile);
        } else {
            try {
                system.initialize(initializationContext, logConfig, logFile);
            } catch (Exception ex) {
                Throwable exceptionToReport = ex;
                while (exceptionToReport != null && !(exceptionToReport instanceof FileNotFoundException)) {
                    exceptionToReport = exceptionToReport.getCause();
                }
                exceptionToReport = (exceptionToReport != null) ? exceptionToReport : ex;
                System.err.println("Logging system failed to initialize using configuration from '" + logConfig + "'");
                exceptionToReport.printStackTrace(System.err);
                throw new IllegalStateException(ex);
            }
        }
    }

    private boolean ignoreLogConfig(String logConfig) {
        return !StringUtils.hasLength(logConfig) || logConfig.startsWith("-D");
    }

    private void initializeFinalLoggingLevels(ConfigurableEnvironment environment, LoggingSystem system) {
        bindLoggerGroups(environment);
        if (this.springBootLogging != null) {
            initializeSpringBootLogging(system, this.springBootLogging);
        }
        setLogLevels(system, environment);
    }

    private void bindLoggerGroups(ConfigurableEnvironment environment) {
        if (this.loggerGroups != null) {
            Binder binder = Binder.get(environment);
            binder.bind(LOGGING_GROUP, STRING_STRINGS_MAP).ifBound(this.loggerGroups::putAll);
        }
    }

    protected void initializeSpringBootLogging(LoggingSystem system, LogLevel springBootLogging) {
        BiConsumer<String, LogLevel> configurer = getLogLevelConfigurer(system);
        SPRING_BOOT_LOGGING_LOGGERS.getOrDefault(springBootLogging, Collections.emptyList()).forEach((name) -> configureLogLevel(name, springBootLogging, configurer));
    }

    protected void setLogLevels(LoggingSystem system, ConfigurableEnvironment environment) {
        BiConsumer<String, LogLevel> customizer = getLogLevelConfigurer(system);
        Binder binder = Binder.get(environment);
        Map<String, LogLevel> levels = binder.bind(LOGGING_LEVEL, STRING_LOGLEVEL_MAP).orElseGet(Collections::emptyMap);
        levels.forEach((name, level) -> configureLogLevel(name, level, customizer));
    }

    private void configureLogLevel(String name, LogLevel level, BiConsumer<String, LogLevel> configurer) {
        if (this.loggerGroups != null) {
            LoggerGroup group = this.loggerGroups.get(name);
            if (group != null && group.hasMembers()) {
                group.configureLogLevel(level, configurer);
                return;
            }
        }
        configurer.accept(name, level);
    }

    private BiConsumer<String, LogLevel> getLogLevelConfigurer(LoggingSystem system) {
        return (name, level) -> {
            try {
                name = name.equalsIgnoreCase(LoggingSystem.ROOT_LOGGER_NAME) ? null : name;
                system.setLogLevel(name, level);
            } catch (RuntimeException ex) {
                this.logger.error(LogMessage.format("Cannot set level '%s' for '%s'", level, name));
            }
        };
    }

    private void registerShutdownHookIfNecessary(Environment environment, LoggingSystem loggingSystem) {
        boolean registerShutdownHook = environment.getProperty(REGISTER_SHUTDOWN_HOOK_PROPERTY, Boolean.class, false);
        if (registerShutdownHook) {
            Runnable shutdownHandler = loggingSystem.getShutdownHandler();
            if (shutdownHandler != null && shutdownHookRegistered.compareAndSet(false, true)) {
                registerShutdownHook(new Thread(shutdownHandler));
            }
        }
    }

    void registerShutdownHook(Thread shutdownHook) {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
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