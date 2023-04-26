package org.springframework.boot.test.web.reactive.server;

import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.util.ClassUtils;

class WebTestClientContextCustomizerFactory implements ContextCustomizerFactory {

    private static final boolean reactorClientPresent;

    private static final boolean jettyClientPresent;

    private static final boolean httpComponentsClientPresent;

    private static final boolean webClientPresent;

    static {
        ClassLoader loader = WebTestClientContextCustomizerFactory.class.getClassLoader();
        reactorClientPresent = ClassUtils.isPresent("reactor.netty.http.client.HttpClient", loader);
        jettyClientPresent = ClassUtils.isPresent("org.eclipse.jetty.client.HttpClient", loader);
        httpComponentsClientPresent = ClassUtils.isPresent("org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient", loader) && ClassUtils.isPresent("org.apache.hc.core5.reactive.ReactiveDataConsumer", loader);
        webClientPresent = ClassUtils.isPresent("org.springframework.web.reactive.function.client.WebClient", loader);
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        SpringBootTest springBootTest = TestContextAnnotationUtils.findMergedAnnotation(testClass, SpringBootTest.class);
        return (springBootTest != null && webClientPresent && (reactorClientPresent || jettyClientPresent || httpComponentsClientPresent)) ? new WebTestClientContextCustomizer() : null;
    }
}
