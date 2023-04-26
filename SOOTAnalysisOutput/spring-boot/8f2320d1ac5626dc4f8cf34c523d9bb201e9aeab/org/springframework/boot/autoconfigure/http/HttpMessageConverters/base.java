package org.springframework.boot.autoconfigure.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

public class HttpMessageConverters implements Iterable<HttpMessageConverter<?>> {

    private static final List<Class<?>> NON_REPLACING_CONVERTERS;

    static {
        List<Class<?>> nonReplacingConverters = new ArrayList<>();
        addClassIfExists(nonReplacingConverters, "org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter");
        NON_REPLACING_CONVERTERS = Collections.unmodifiableList(nonReplacingConverters);
    }

    private static final Map<Class<?>, Class<?>> EQUIVALENT_CONVERTERS;

    static {
        Map<Class<?>, Class<?>> equivalentConverters = new HashMap<>();
        putIfExists(equivalentConverters, "org.springframework.http.converter.json.MappingJackson2HttpMessageConverter", "org.springframework.http.converter.json.GsonHttpMessageConverter");
        EQUIVALENT_CONVERTERS = Collections.unmodifiableMap(equivalentConverters);
    }

    private final List<HttpMessageConverter<?>> converters;

    public HttpMessageConverters(HttpMessageConverter<?>... additionalConverters) {
        this(Arrays.asList(additionalConverters));
    }

    public HttpMessageConverters(Collection<HttpMessageConverter<?>> additionalConverters) {
        this(true, additionalConverters);
    }

    public HttpMessageConverters(boolean addDefaultConverters, Collection<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> combined = getCombinedConverters(converters, addDefaultConverters ? getDefaultConverters() : Collections.emptyList());
        combined = postProcessConverters(combined);
        this.converters = Collections.unmodifiableList(combined);
    }

    private List<HttpMessageConverter<?>> getCombinedConverters(Collection<HttpMessageConverter<?>> converters, List<HttpMessageConverter<?>> defaultConverters) {
        List<HttpMessageConverter<?>> combined = new ArrayList<>();
        List<HttpMessageConverter<?>> processing = new ArrayList<>(converters);
        for (HttpMessageConverter<?> defaultConverter : defaultConverters) {
            Iterator<HttpMessageConverter<?>> iterator = processing.iterator();
            while (iterator.hasNext()) {
                HttpMessageConverter<?> candidate = iterator.next();
                if (isReplacement(defaultConverter, candidate)) {
                    combined.add(candidate);
                    iterator.remove();
                }
            }
            combined.add(defaultConverter);
            if (defaultConverter instanceof AllEncompassingFormHttpMessageConverter) {
                configurePartConverters((AllEncompassingFormHttpMessageConverter) defaultConverter, converters);
            }
        }
        combined.addAll(0, processing);
        return combined;
    }

    private boolean isReplacement(HttpMessageConverter<?> defaultConverter, HttpMessageConverter<?> candidate) {
        for (Class<?> nonReplacingConverter : NON_REPLACING_CONVERTERS) {
            if (nonReplacingConverter.isInstance(candidate)) {
                return false;
            }
        }
        Class<?> converterClass = defaultConverter.getClass();
        if (ClassUtils.isAssignableValue(converterClass, candidate)) {
            return true;
        }
        Class<?> equivalentClass = EQUIVALENT_CONVERTERS.get(converterClass);
        return equivalentClass != null && ClassUtils.isAssignableValue(equivalentClass, candidate);
    }

    private void configurePartConverters(AllEncompassingFormHttpMessageConverter formConverter, Collection<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> partConverters = formConverter.getPartConverters();
        List<HttpMessageConverter<?>> combinedConverters = getCombinedConverters(converters, partConverters);
        combinedConverters = postProcessPartConverters(combinedConverters);
        formConverter.setPartConverters(combinedConverters);
    }

    protected List<HttpMessageConverter<?>> postProcessConverters(List<HttpMessageConverter<?>> converters) {
        return converters;
    }

    protected List<HttpMessageConverter<?>> postProcessPartConverters(List<HttpMessageConverter<?>> converters) {
        return converters;
    }

    private List<HttpMessageConverter<?>> getDefaultConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        if (ClassUtils.isPresent("org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport", null)) {
            converters.addAll(new WebMvcConfigurationSupport() {

                public List<HttpMessageConverter<?>> defaultMessageConverters() {
                    return super.getMessageConverters();
                }
            }.defaultMessageConverters());
        } else {
            converters.addAll(new RestTemplate().getMessageConverters());
        }
        reorderXmlConvertersToEnd(converters);
        return converters;
    }

    private void reorderXmlConvertersToEnd(List<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> xml = new ArrayList<>();
        for (Iterator<HttpMessageConverter<?>> iterator = converters.iterator(); iterator.hasNext(); ) {
            HttpMessageConverter<?> converter = iterator.next();
            if ((converter instanceof AbstractXmlHttpMessageConverter) || (converter instanceof MappingJackson2XmlHttpMessageConverter)) {
                xml.add(converter);
                iterator.remove();
            }
        }
        converters.addAll(xml);
    }

    @Override
    public Iterator<HttpMessageConverter<?>> iterator() {
        return getConverters().iterator();
    }

    public List<HttpMessageConverter<?>> getConverters() {
        return this.converters;
    }

    private static void addClassIfExists(List<Class<?>> list, String className) {
        try {
            list.add(Class.forName(className));
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
        }
    }

    private static void putIfExists(Map<Class<?>, Class<?>> map, String keyClassName, String valueClassName) {
        try {
            map.put(Class.forName(keyClassName), Class.forName(valueClassName));
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
        }
    }
}
