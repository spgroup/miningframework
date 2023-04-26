package org.springframework.boot.test.autoconfigure.web.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.autoconfigure.filter.StandardAnnotationCustomizableTypeExcludeFilter;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class WebMvcTypeExcludeFilter extends StandardAnnotationCustomizableTypeExcludeFilter<WebMvcTest> {

    private static final Class<?>[] NO_CONTROLLERS = {};

    private static final String[] OPTIONAL_INCLUDES = { "org.springframework.security.config.annotation.web.WebSecurityConfigurer" };

    private static final Set<Class<?>> DEFAULT_INCLUDES;

    static {
        Set<Class<?>> includes = new LinkedHashSet<>();
        includes.add(ControllerAdvice.class);
        includes.add(JsonComponent.class);
        includes.add(WebMvcConfigurer.class);
        includes.add(javax.servlet.Filter.class);
        includes.add(FilterRegistrationBean.class);
        includes.add(DelegatingFilterProxyRegistrationBean.class);
        includes.add(HandlerMethodArgumentResolver.class);
        includes.add(HttpMessageConverter.class);
        includes.add(ErrorAttributes.class);
        includes.add(Converter.class);
        includes.add(GenericConverter.class);
        for (String optionalInclude : OPTIONAL_INCLUDES) {
            try {
                includes.add(ClassUtils.forName(optionalInclude, null));
            } catch (Exception ex) {
            }
        }
        DEFAULT_INCLUDES = Collections.unmodifiableSet(includes);
    }

    private static final Set<Class<?>> DEFAULT_INCLUDES_AND_CONTROLLER;

    static {
        Set<Class<?>> includes = new LinkedHashSet<>(DEFAULT_INCLUDES);
        includes.add(Controller.class);
        DEFAULT_INCLUDES_AND_CONTROLLER = Collections.unmodifiableSet(includes);
    }

    private final Class<?>[] controllers;

    WebMvcTypeExcludeFilter(Class<?> testClass) {
        super(testClass);
        this.controllers = getAnnotation().getValue("controllers", Class[].class).orElse(NO_CONTROLLERS);
    }

    @Override
    protected Set<Class<?>> getDefaultIncludes() {
        if (ObjectUtils.isEmpty(this.controllers)) {
            return DEFAULT_INCLUDES_AND_CONTROLLER;
        }
        return DEFAULT_INCLUDES;
    }

    @Override
    protected Set<Class<?>> getComponentIncludes() {
        return new LinkedHashSet<>(Arrays.asList(this.controllers));
    }
}
