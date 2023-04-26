package org.springframework.boot;

import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.diagnostics.FailureAnalyzers;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;

public class SpringApplication {

    public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context." + "annotation.AnnotationConfigApplicationContext";

    public static final String DEFAULT_WEB_CONTEXT_CLASS = "org.springframework." + "boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext";

    private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet", "org.springframework.web.context.ConfigurableWebApplicationContext" };

    public static final String BANNER_LOCATION_PROPERTY_VALUE = SpringApplicationBannerPrinter.DEFAULT_BANNER_LOCATION;

    public static final String BANNER_LOCATION_PROPERTY = SpringApplicationBannerPrinter.BANNER_LOCATION_PROPERTY;

    private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless";

    private static final Log logger = LogFactory.getLog(SpringApplication.class);

    private final Set<Object> sources = new LinkedHashSet<Object>();

    private Class<?> mainApplicationClass;

    private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

    private boolean logStartupInfo = true;

    private boolean addCommandLineProperties = true;

    private Banner banner;

    private ResourceLoader resourceLoader;

    private BeanNameGenerator beanNameGenerator;

    private ConfigurableEnvironment environment;

    private Class<? extends ConfigurableApplicationContext> applicationContextClass;

    private boolean webEnvironment;

    private boolean headless = true;

    private boolean registerShutdownHook = true;

    private List<ApplicationContextInitializer<?>> initializers;

    private List<ApplicationListener<?>> listeners;

    private Map<String, Object> defaultProperties;

    private Set<String> additionalProfiles = new HashSet<String>();

    public SpringApplication(Object... sources) {
        initialize(sources);
    }

    public SpringApplication(ResourceLoader resourceLoader, Object... sources) {
        this.resourceLoader = resourceLoader;
        initialize(sources);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initialize(Object[] sources) {
        if (sources != null && sources.length > 0) {
            this.sources.addAll(Arrays.asList(sources));
        }
        this.webEnvironment = deduceWebEnvironment();
        setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        this.mainApplicationClass = deduceMainApplicationClass();
    }

    private boolean deduceWebEnvironment() {
        for (String className : WEB_ENVIRONMENT_CLASSES) {
            if (!ClassUtils.isPresent(className, null)) {
                return false;
            }
        }
        return true;
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    public ConfigurableApplicationContext run(String... args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        FailureAnalyzers analyzers = null;
        configureHeadlessProperty();
        SpringApplicationRunListeners listeners = getRunListeners(args);
        listeners.starting();
        try {
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
            Banner printedBanner = printBanner(environment);
            context = createApplicationContext();
            analyzers = new FailureAnalyzers(context);
            prepareContext(context, environment, listeners, applicationArguments, printedBanner);
            refreshContext(context);
            afterRefresh(context, applicationArguments);
            listeners.finished(context, null);
            stopWatch.stop();
            if (this.logStartupInfo) {
                new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
            }
            return context;
        } catch (Throwable ex) {
            handleRunFailure(context, listeners, analyzers, ex);
            throw new IllegalStateException(ex);
        }
    }

    private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments) {
        ConfigurableEnvironment environment = getOrCreateEnvironment();
        configureEnvironment(environment, applicationArguments.getSourceArgs());
        listeners.environmentPrepared(environment);
        if (!this.webEnvironment) {
            environment = new EnvironmentConverter(getClassLoader()).convertToStandardEnvironmentIfNecessary(environment);
        }
        return environment;
    }

    private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment, SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
        context.setEnvironment(environment);
        postProcessApplicationContext(context);
        applyInitializers(context);
        listeners.contextPrepared(context);
        if (this.logStartupInfo) {
            logStartupInfo(context.getParent() == null);
            logStartupProfileInfo(context);
        }
        context.getBeanFactory().registerSingleton("springApplicationArguments", applicationArguments);
        if (printedBanner != null) {
            context.getBeanFactory().registerSingleton("springBootBanner", printedBanner);
        }
        Set<Object> sources = getSources();
        Assert.notEmpty(sources, "Sources must not be empty");
        load(context, sources.toArray(new Object[sources.size()]));
        listeners.contextLoaded(context);
    }

    private void refreshContext(ConfigurableApplicationContext context) {
        refresh(context);
        if (this.registerShutdownHook) {
            try {
                context.registerShutdownHook();
            } catch (AccessControlException ex) {
            }
        }
    }

    private void configureHeadlessProperty() {
        System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
    }

    private SpringApplicationRunListeners getRunListeners(String[] args) {
        Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
        return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
    }

    private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type) {
        return getSpringFactoriesInstances(type, new Class<?>[] {});
    }

    private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> names = new LinkedHashSet<String>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
        List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
        AnnotationAwareOrderComparator.sort(instances);
        return instances;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args, Set<String> names) {
        List<T> instances = new ArrayList<T>(names.size());
        for (String name : names) {
            try {
                Class<?> instanceClass = ClassUtils.forName(name, classLoader);
                Assert.isAssignable(type, instanceClass);
                Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
                T instance = (T) BeanUtils.instantiateClass(constructor, args);
                instances.add(instance);
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
            }
        }
        return instances;
    }

    private ConfigurableEnvironment getOrCreateEnvironment() {
        if (this.environment != null) {
            return this.environment;
        }
        if (this.webEnvironment) {
            return new StandardServletEnvironment();
        }
        return new StandardEnvironment();
    }

    protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
        configurePropertySources(environment, args);
        configureProfiles(environment, args);
    }

    protected void configurePropertySources(ConfigurableEnvironment environment, String[] args) {
        MutablePropertySources sources = environment.getPropertySources();
        if (this.defaultProperties != null && !this.defaultProperties.isEmpty()) {
            sources.addLast(new MapPropertySource("defaultProperties", this.defaultProperties));
        }
        if (this.addCommandLineProperties && args.length > 0) {
            String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
            if (sources.contains(name)) {
                PropertySource<?> source = sources.get(name);
                CompositePropertySource composite = new CompositePropertySource(name);
                composite.addPropertySource(new SimpleCommandLinePropertySource(name + "-" + args.hashCode(), args));
                composite.addPropertySource(source);
                sources.replace(name, composite);
            } else {
                sources.addFirst(new SimpleCommandLinePropertySource(args));
            }
        }
    }

    protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
        environment.getActiveProfiles();
        Set<String> profiles = new LinkedHashSet<String>(this.additionalProfiles);
        profiles.addAll(Arrays.asList(environment.getActiveProfiles()));
        environment.setActiveProfiles(profiles.toArray(new String[profiles.size()]));
    }

    private Banner printBanner(ConfigurableEnvironment environment) {
        if (this.bannerMode == Banner.Mode.OFF) {
            return null;
        }
        ResourceLoader resourceLoader = this.resourceLoader != null ? this.resourceLoader : new DefaultResourceLoader(getClassLoader());
        SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(resourceLoader, this.banner);
        if (this.bannerMode == Mode.LOG) {
            return bannerPrinter.print(environment, this.mainApplicationClass, logger);
        }
        return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        Class<?> contextClass = this.applicationContextClass;
        if (contextClass == null) {
            try {
                contextClass = Class.forName(this.webEnvironment ? DEFAULT_WEB_CONTEXT_CLASS : DEFAULT_CONTEXT_CLASS);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Unable create a default ApplicationContext, " + "please specify an ApplicationContextClass", ex);
            }
        }
        return (ConfigurableApplicationContext) BeanUtils.instantiate(contextClass);
    }

    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        if (this.beanNameGenerator != null) {
            context.getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, this.beanNameGenerator);
        }
        if (this.resourceLoader != null) {
            if (context instanceof GenericApplicationContext) {
                ((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
            }
            if (context instanceof DefaultResourceLoader) {
                ((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(), ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
            initializer.initialize(context);
        }
    }

    protected void logStartupInfo(boolean isRoot) {
        if (isRoot) {
            new StartupInfoLogger(this.mainApplicationClass).logStarting(getApplicationLog());
        }
    }

    protected void logStartupProfileInfo(ConfigurableApplicationContext context) {
        Log log = getApplicationLog();
        if (log.isInfoEnabled()) {
            String[] activeProfiles = context.getEnvironment().getActiveProfiles();
            if (ObjectUtils.isEmpty(activeProfiles)) {
                String[] defaultProfiles = context.getEnvironment().getDefaultProfiles();
                log.info("No active profile set, falling back to default profiles: " + StringUtils.arrayToCommaDelimitedString(defaultProfiles));
            } else {
                log.info("The following profiles are active: " + StringUtils.arrayToCommaDelimitedString(activeProfiles));
            }
        }
    }

    protected Log getApplicationLog() {
        if (this.mainApplicationClass == null) {
            return logger;
        }
        return LogFactory.getLog(this.mainApplicationClass);
    }

    protected void load(ApplicationContext context, Object[] sources) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
        }
        BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
        if (this.beanNameGenerator != null) {
            loader.setBeanNameGenerator(this.beanNameGenerator);
        }
        if (this.resourceLoader != null) {
            loader.setResourceLoader(this.resourceLoader);
        }
        if (this.environment != null) {
            loader.setEnvironment(this.environment);
        }
        loader.load();
    }

    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    public ClassLoader getClassLoader() {
        if (this.resourceLoader != null) {
            return this.resourceLoader.getClassLoader();
        }
        return ClassUtils.getDefaultClassLoader();
    }

    private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
        if (context instanceof BeanDefinitionRegistry) {
            return (BeanDefinitionRegistry) context;
        }
        if (context instanceof AbstractApplicationContext) {
            return (BeanDefinitionRegistry) ((AbstractApplicationContext) context).getBeanFactory();
        }
        throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
    }

    protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
        return new BeanDefinitionLoader(registry, sources);
    }

    protected void refresh(ApplicationContext applicationContext) {
        Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
        ((AbstractApplicationContext) applicationContext).refresh();
    }

    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
        callRunners(context, args);
    }

    private void callRunners(ApplicationContext context, ApplicationArguments args) {
        List<Object> runners = new ArrayList<Object>();
        runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
        runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
        AnnotationAwareOrderComparator.sort(runners);
        for (Object runner : new LinkedHashSet<Object>(runners)) {
            if (runner instanceof ApplicationRunner) {
                callRunner((ApplicationRunner) runner, args);
            }
            if (runner instanceof CommandLineRunner) {
                callRunner((CommandLineRunner) runner, args);
            }
        }
    }

    private void callRunner(ApplicationRunner runner, ApplicationArguments args) {
        try {
            (runner).run(args);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute ApplicationRunner", ex);
        }
    }

    private void callRunner(CommandLineRunner runner, ApplicationArguments args) {
        try {
            (runner).run(args.getSourceArgs());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to execute CommandLineRunner", ex);
        }
    }

    private void handleRunFailure(ConfigurableApplicationContext context, SpringApplicationRunListeners listeners, FailureAnalyzers analyzers, Throwable exception) {
        try {
            try {
                handleExitCode(context, exception);
                listeners.finished(context, exception);
            } finally {
                reportFailure(analyzers, exception);
                if (context != null) {
                    context.close();
                }
            }
        } catch (Exception ex) {
            logger.warn("Unable to close ApplicationContext", ex);
        }
        ReflectionUtils.rethrowRuntimeException(exception);
    }

    private void reportFailure(FailureAnalyzers analyzers, Throwable failure) {
        try {
            if (analyzers != null && analyzers.analyzeAndReport(failure)) {
                registerLoggedException(failure);
                return;
            }
        } catch (Throwable ex) {
        }
        if (logger.isErrorEnabled()) {
            logger.error("Application startup failed", failure);
            registerLoggedException(failure);
        }
    }

    protected void registerLoggedException(Throwable exception) {
        SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
        if (handler != null) {
            handler.registerLoggedException(exception);
        }
    }

    private void handleExitCode(ConfigurableApplicationContext context, Throwable exception) {
        int exitCode = getExitCodeFromException(context, exception);
        if (exitCode != 0) {
            if (context != null) {
                context.publishEvent(new ExitCodeEvent(context, exitCode));
            }
            SpringBootExceptionHandler handler = getSpringBootExceptionHandler();
            if (handler != null) {
                handler.registerExitCode(exitCode);
            }
        }
    }

    private int getExitCodeFromException(ConfigurableApplicationContext context, Throwable exception) {
        int exitCode = getExitCodeFromMappedException(context, exception);
        if (exitCode == 0) {
            exitCode = getExitCodeFromExitCodeGeneratorException(exception);
        }
        return exitCode;
    }

    private int getExitCodeFromMappedException(ConfigurableApplicationContext context, Throwable exception) {
        if (context == null || !context.isActive()) {
            return 0;
        }
        ExitCodeGenerators generators = new ExitCodeGenerators();
        Collection<ExitCodeExceptionMapper> beans = context.getBeansOfType(ExitCodeExceptionMapper.class).values();
        generators.addAll(exception, beans);
        return generators.getExitCode();
    }

    private int getExitCodeFromExitCodeGeneratorException(Throwable exception) {
        if (exception == null) {
            return 0;
        }
        if (exception instanceof ExitCodeGenerator) {
            return ((ExitCodeGenerator) exception).getExitCode();
        }
        return getExitCodeFromExitCodeGeneratorException(exception.getCause());
    }

    SpringBootExceptionHandler getSpringBootExceptionHandler() {
        if (isMainThread(Thread.currentThread())) {
            return SpringBootExceptionHandler.forCurrentThread();
        }
        return null;
    }

    private boolean isMainThread(Thread currentThread) {
        return ("main".equals(currentThread.getName()) || "restartedMain".equals(currentThread.getName())) && "main".equals(currentThread.getThreadGroup().getName());
    }

    public Class<?> getMainApplicationClass() {
        return this.mainApplicationClass;
    }

    public void setMainApplicationClass(Class<?> mainApplicationClass) {
        this.mainApplicationClass = mainApplicationClass;
    }

    public boolean isWebEnvironment() {
        return this.webEnvironment;
    }

    public void setWebEnvironment(boolean webEnvironment) {
        this.webEnvironment = webEnvironment;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void setRegisterShutdownHook(boolean registerShutdownHook) {
        this.registerShutdownHook = registerShutdownHook;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public void setBannerMode(Banner.Mode bannerMode) {
        this.bannerMode = bannerMode;
    }

    public void setLogStartupInfo(boolean logStartupInfo) {
        this.logStartupInfo = logStartupInfo;
    }

    public void setAddCommandLineProperties(boolean addCommandLineProperties) {
        this.addCommandLineProperties = addCommandLineProperties;
    }

    public void setDefaultProperties(Map<String, Object> defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    public void setDefaultProperties(Properties defaultProperties) {
        this.defaultProperties = new HashMap<String, Object>();
        for (Object key : Collections.list(defaultProperties.propertyNames())) {
            this.defaultProperties.put((String) key, defaultProperties.get(key));
        }
    }

    public void setAdditionalProfiles(String... profiles) {
        this.additionalProfiles = new LinkedHashSet<String>(Arrays.asList(profiles));
    }

    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator;
    }

    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    public Set<Object> getSources() {
        return this.sources;
    }

    public void setSources(Set<Object> sources) {
        Assert.notNull(sources, "Sources must not be null");
        this.sources.addAll(sources);
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        Assert.notNull(resourceLoader, "ResourceLoader must not be null");
        this.resourceLoader = resourceLoader;
    }

    public void setApplicationContextClass(Class<? extends ConfigurableApplicationContext> applicationContextClass) {
        this.applicationContextClass = applicationContextClass;
        if (!isWebApplicationContext(applicationContextClass)) {
            this.webEnvironment = false;
        }
    }

    private boolean isWebApplicationContext(Class<?> applicationContextClass) {
        try {
            return WebApplicationContext.class.isAssignableFrom(applicationContextClass);
        } catch (NoClassDefFoundError ex) {
            return false;
        }
    }

    public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
        this.initializers = new ArrayList<ApplicationContextInitializer<?>>();
        this.initializers.addAll(initializers);
    }

    public void addInitializers(ApplicationContextInitializer<?>... initializers) {
        this.initializers.addAll(Arrays.asList(initializers));
    }

    public Set<ApplicationContextInitializer<?>> getInitializers() {
        return asUnmodifiableOrderedSet(this.initializers);
    }

    public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
        this.listeners = new ArrayList<ApplicationListener<?>>();
        this.listeners.addAll(listeners);
    }

    public void addListeners(ApplicationListener<?>... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public Set<ApplicationListener<?>> getListeners() {
        return asUnmodifiableOrderedSet(this.listeners);
    }

    public static ConfigurableApplicationContext run(Object source, String... args) {
        return run(new Object[] { source }, args);
    }

    public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
        return new SpringApplication(sources).run(args);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Object[0], args);
    }

    public static int exit(ApplicationContext context, ExitCodeGenerator... exitCodeGenerators) {
        Assert.notNull(context, "Context must not be null");
        int exitCode = 0;
        try {
            try {
                ExitCodeGenerators generators = new ExitCodeGenerators();
                Collection<ExitCodeGenerator> beans = context.getBeansOfType(ExitCodeGenerator.class).values();
                generators.addAll(exitCodeGenerators);
                generators.addAll(beans);
                exitCode = generators.getExitCode();
                if (exitCode != 0) {
                    context.publishEvent(new ExitCodeEvent(context, exitCode));
                }
            } finally {
                close(context);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            exitCode = (exitCode == 0 ? 1 : exitCode);
        }
        return exitCode;
    }

    private static void close(ApplicationContext context) {
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext closable = (ConfigurableApplicationContext) context;
            closable.close();
        }
    }

    private static <E> Set<E> asUnmodifiableOrderedSet(Collection<E> elements) {
        List<E> list = new ArrayList<E>();
        list.addAll(elements);
        Collections.sort(list, AnnotationAwareOrderComparator.INSTANCE);
        return new LinkedHashSet<E>(list);
    }
}
