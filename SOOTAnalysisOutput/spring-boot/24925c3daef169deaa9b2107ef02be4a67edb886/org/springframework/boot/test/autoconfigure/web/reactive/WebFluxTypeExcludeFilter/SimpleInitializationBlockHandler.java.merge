package org.springframework.boot.test.autoconfigure.web.reactive;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.autoconfigure.filter.AnnotationCustomizableTypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebExceptionHandler;

class WebFluxTypeExcludeFilter extends AnnotationCustomizableTypeExcludeFilter {

    private static final Set<Class<?>> DEFAULT_INCLUDES;

    static {
        Set<Class<?>> includes = new LinkedHashSet<>();
        includes.add(ControllerAdvice.class);
        includes.add(JsonComponent.class);
        includes.add(WebFluxConfigurer.class);
        includes.add(Converter.class);
        includes.add(GenericConverter.class);
        includes.add(WebExceptionHandler.class);
        DEFAULT_INCLUDES = Collections.unmodifiableSet(includes);
    }

    private static final Set<Class<?>> DEFAULT_INCLUDES_AND_CONTROLLER;

    static {
        Set<Class<?>> includes = new LinkedHashSet<>(DEFAULT_INCLUDES);
        includes.add(Controller.class);
        DEFAULT_INCLUDES_AND_CONTROLLER = Collections.unmodifiableSet(includes);
    }

    private final WebFluxTest annotation;

    WebFluxTypeExcludeFilter(Class<?> testClass) {
        this.annotation = AnnotatedElementUtils.getMergedAnnotation(testClass, WebFluxTest.class);
    }

    @Override
    protected boolean hasAnnotation() {
        return this.annotation != null;
    }

    @Override
    protected ComponentScan.Filter[] getFilters(FilterType type) {
        switch(type) {
            case INCLUDE:
                return this.annotation.includeFilters();
            case EXCLUDE:
                return this.annotation.excludeFilters();
        }
        throw new IllegalStateException("Unsupported type " + type);
    }

    @Override
    protected boolean isUseDefaultFilters() {
        return this.annotation.useDefaultFilters();
    }

    @Override
    protected Set<Class<?>> getDefaultIncludes() {
        if (ObjectUtils.isEmpty(this.annotation.controllers())) {
            return DEFAULT_INCLUDES_AND_CONTROLLER;
        }
        return DEFAULT_INCLUDES;
    }

    @Override
    protected Set<Class<?>> getComponentIncludes() {
        return new LinkedHashSet<>(Arrays.asList(this.annotation.controllers()));
    }
}