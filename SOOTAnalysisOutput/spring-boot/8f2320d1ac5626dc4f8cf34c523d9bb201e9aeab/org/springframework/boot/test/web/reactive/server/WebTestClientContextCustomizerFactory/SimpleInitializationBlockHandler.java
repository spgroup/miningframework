package org.springframework.boot.test.web.reactive.server;

import java.util.List;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.util.ClassUtils;

class WebTestClientContextCustomizerFactory implements ContextCustomizerFactory {

    private static final boolean webClientPresent;

    static {
        ClassLoader loader = WebTestClientContextCustomizerFactory.class.getClassLoader();
        webClientPresent = ClassUtils.isPresent("org.springframework.web.reactive.function.client.WebClient", loader);
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        SpringBootTest springBootTest = TestContextAnnotationUtils.findMergedAnnotation(testClass, SpringBootTest.class);
        return (springBootTest != null && webClientPresent) ? new WebTestClientContextCustomizer() : null;
    }
}