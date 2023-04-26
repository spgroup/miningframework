package org.springframework.boot.web.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.BeanUtils;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.ClassUtils;

public class ClientHttpRequestFactorySupplier implements Supplier<ClientHttpRequestFactory> {

    private static final Map<String, String> REQUEST_FACTORY_CANDIDATES;

    static {
        Map<String, String> candidates = new LinkedHashMap<>();
        candidates.put("org.apache.http.client.HttpClient", "org.springframework.http.client.HttpComponentsClientHttpRequestFactory");
        candidates.put("okhttp3.OkHttpClient", "org.springframework.http.client.OkHttp3ClientHttpRequestFactory");
        REQUEST_FACTORY_CANDIDATES = Collections.unmodifiableMap(candidates);
    }

    @Override
    public ClientHttpRequestFactory get() {
        for (Map.Entry<String, String> candidate : REQUEST_FACTORY_CANDIDATES.entrySet()) {
            ClassLoader classLoader = getClass().getClassLoader();
            if (ClassUtils.isPresent(candidate.getKey(), classLoader)) {
                Class<?> factoryClass = ClassUtils.resolveClassName(candidate.getValue(), classLoader);
                return (ClientHttpRequestFactory) BeanUtils.instantiateClass(factoryClass);
            }
        }
        return new SimpleClientHttpRequestFactory();
    }
}
