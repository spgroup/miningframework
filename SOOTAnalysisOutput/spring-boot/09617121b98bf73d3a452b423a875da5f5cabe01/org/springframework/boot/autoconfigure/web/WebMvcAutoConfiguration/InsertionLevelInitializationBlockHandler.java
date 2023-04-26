package org.springframework.boot.autoconfigure.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.OrderedHiddenHttpMethodFilter;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurerAdapter.class })
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
public class WebMvcAutoConfiguration {

    private static final String[] SERVLET_RESOURCE_LOCATIONS = { "/" };

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

    private static final String[] RESOURCE_LOCATIONS;

    static {
        RESOURCE_LOCATIONS = new String[CLASSPATH_RESOURCE_LOCATIONS.length + SERVLET_RESOURCE_LOCATIONS.length];
        System.arraycopy(SERVLET_RESOURCE_LOCATIONS, 0, RESOURCE_LOCATIONS, 0, SERVLET_RESOURCE_LOCATIONS.length);
        System.arraycopy(CLASSPATH_RESOURCE_LOCATIONS, 0, RESOURCE_LOCATIONS, SERVLET_RESOURCE_LOCATIONS.length, CLASSPATH_RESOURCE_LOCATIONS.length);
    }

    private static final String[] STATIC_INDEX_HTML_RESOURCES;

    static {
        STATIC_INDEX_HTML_RESOURCES = new String[RESOURCE_LOCATIONS.length];
        for (int i = 0; i < STATIC_INDEX_HTML_RESOURCES.length; i++) {
            STATIC_INDEX_HTML_RESOURCES[i] = RESOURCE_LOCATIONS[i] + "index.html";
        }
    }

    public static String DEFAULT_PREFIX = "";

    public static String DEFAULT_SUFFIX = "";

    @Bean
    @ConditionalOnMissingBean(HiddenHttpMethodFilter.class)
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new OrderedHiddenHttpMethodFilter();
    }

    @Configuration
    @Import(EnableWebMvcConfiguration.class)
    @EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
    public static class WebMvcAutoConfigurationAdapter extends WebMvcConfigurerAdapter {

        private static Log logger = LogFactory.getLog(WebMvcConfigurerAdapter.class);

        @Value("${spring.view.prefix:}")
        private String prefix = "";

        @Value("${spring.view.suffix:}")
        private String suffix = "";

        @Autowired
        private ResourceProperties resourceProperties = new ResourceProperties();

        @Autowired
        private WebMvcProperties mvcProperties = new WebMvcProperties();

        @Autowired
        private ListableBeanFactory beanFactory;

        @Autowired
        private ResourceLoader resourceLoader;

        @Autowired
        private HttpMessageConverters messageConverters;

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.addAll(this.messageConverters.getConverters());
        }

        @Bean
        @ConditionalOnMissingBean(InternalResourceViewResolver.class)
        public InternalResourceViewResolver defaultViewResolver() {
            InternalResourceViewResolver resolver = new InternalResourceViewResolver();
            resolver.setPrefix(this.prefix);
            resolver.setSuffix(this.suffix);
            return resolver;
        }

        @Bean
        @ConditionalOnMissingBean(RequestContextListener.class)
        public RequestContextListener requestContextListener() {
            return new RequestContextListener();
        }

        @Bean
        @ConditionalOnBean(View.class)
        public BeanNameViewResolver beanNameViewResolver() {
            BeanNameViewResolver resolver = new BeanNameViewResolver();
            resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
            return resolver;
        }

        @Bean
        @ConditionalOnBean(ViewResolver.class)
        @ConditionalOnMissingBean(name = "viewResolver", value = ContentNegotiatingViewResolver.class)
        public ContentNegotiatingViewResolver viewResolver(BeanFactory beanFactory) {
            ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
            resolver.setContentNegotiationManager(beanFactory.getBean(ContentNegotiationManager.class));
            resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return resolver;
        }

        @Bean
        @ConditionalOnMissingBean(LocaleResolver.class)
        @ConditionalOnProperty(prefix = "spring.mvc", name = "locale")
        public LocaleResolver localeResolver() {
            return new FixedLocaleResolver(StringUtils.parseLocaleString(this.mvcProperties.getLocale()));
        }

        @Bean
        @ConditionalOnProperty(prefix = "spring.mvc", name = "date-format")
        public Formatter<Date> dateFormatter() {
            return new DateFormatter(this.mvcProperties.getDateFormat());
        }

        @Override
        public MessageCodesResolver getMessageCodesResolver() {
            if (this.mvcProperties.getMessageCodesResolverFormat() != null) {
                DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
                resolver.setMessageCodeFormatter(this.mvcProperties.getMessageCodesResolverFormat());
                return resolver;
            }
            return null;
        }

        @Override
        public void addFormatters(FormatterRegistry registry) {
            for (Converter<?, ?> converter : getBeansOfType(Converter.class)) {
                registry.addConverter(converter);
            }
            for (GenericConverter converter : getBeansOfType(GenericConverter.class)) {
                registry.addConverter(converter);
            }
            for (Formatter<?> formatter : getBeansOfType(Formatter.class)) {
                registry.addFormatter(formatter);
            }
        }

        private <T> Collection<T> getBeansOfType(Class<T> type) {
            return this.beanFactory.getBeansOfType(type).values();
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            if (!this.resourceProperties.isAddMappings()) {
                logger.debug("Default resource handling disabled");
                return;
            }
            Integer cachePeriod = this.resourceProperties.getCachePeriod();
            if (!registry.hasMappingForPattern("/webjars/**")) {
                registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/").setCachePeriod(cachePeriod);
            }
            if (!registry.hasMappingForPattern("/**")) {
                registry.addResourceHandler("/**").addResourceLocations(RESOURCE_LOCATIONS).setCachePeriod(cachePeriod);
            }
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            addStaticIndexHtmlViewControllers(registry);
        }

        private void addStaticIndexHtmlViewControllers(ViewControllerRegistry registry) {
            for (String resource : STATIC_INDEX_HTML_RESOURCES) {
                if (this.resourceLoader.getResource(resource).exists()) {
                    try {
                        logger.info("Adding welcome page: " + this.resourceLoader.getResource(resource).getURL());
                    } catch (IOException ex) {
                    }
                    registry.addViewController("/").setViewName("forward:index.html");
                    return;
                }
            }
        }

        @Configuration
        @ConditionalOnProperty(value = "spring.mvc.favicon.enabled", matchIfMissing = true)
        public static class FaviconConfiguration implements ResourceLoaderAware {

            private ResourceLoader resourceLoader;

            @Bean
            public SimpleUrlHandlerMapping faviconHandlerMapping() {
                SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
                mapping.setOrder(Integer.MIN_VALUE + 1);
                mapping.setUrlMap(Collections.singletonMap("**/favicon.ico", faviconRequestHandler()));
                return mapping;
            }

            @Override
            public void setResourceLoader(ResourceLoader resourceLoader) {
                this.resourceLoader = resourceLoader;
            }

            @Bean
            public ResourceHttpRequestHandler faviconRequestHandler() {
                ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
                requestHandler.setLocations(getLocations());
                return requestHandler;
            }

            private List<Resource> getLocations() {
                List<Resource> locations = new ArrayList<Resource>(CLASSPATH_RESOURCE_LOCATIONS.length + 1);
                for (String location : CLASSPATH_RESOURCE_LOCATIONS) {
                    locations.add(this.resourceLoader.getResource(location));
                }
                locations.add(new ClassPathResource("/"));
                return Collections.unmodifiableList(locations);
            }
        }
    }

    @Configuration
    public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration {

        @Autowired(required = false)
        private WebMvcProperties mvcProperties;

        @Bean
        @Override
        public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
            RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
            adapter.setIgnoreDefaultModelOnRedirect(this.mvcProperties == null ? true : this.mvcProperties.isIgnoreDefaultModelOnRedirect());
            return adapter;
        }

        @Bean
        @Primary
        @Override
        public RequestMappingHandlerMapping requestMappingHandlerMapping() {
            return super.requestMappingHandlerMapping();
        }
    }
}