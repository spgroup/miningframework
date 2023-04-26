package org.springframework.boot.autoconfigure;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.validation.Configuration;
import jakarta.validation.Validation;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.NativeDetector;
import org.springframework.core.Ordered;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

public class BackgroundPreinitializer implements ApplicationListener<SpringApplicationEvent>, Ordered {

    public static final String IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME = "spring.backgroundpreinitializer.ignore";

    private static final AtomicBoolean preinitializationStarted = new AtomicBoolean();

    private static final CountDownLatch preinitializationComplete = new CountDownLatch(1);

    private static final boolean ENABLED = !Boolean.getBoolean(IGNORE_BACKGROUNDPREINITIALIZER_PROPERTY_NAME) && Runtime.getRuntime().availableProcessors() > 1;

    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER + 1;
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (!ENABLED || NativeDetector.inNativeImage()) {
            return;
        }
        if (event instanceof ApplicationEnvironmentPreparedEvent && preinitializationStarted.compareAndSet(false, true)) {
            performPreinitialization();
        }
        if ((event instanceof ApplicationReadyEvent || event instanceof ApplicationFailedEvent) && preinitializationStarted.get()) {
            try {
                preinitializationComplete.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void performPreinitialization() {
        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    runSafely(new ConversionServiceInitializer());
                    runSafely(new ValidationInitializer());
                    if (!runSafely(new MessageConverterInitializer())) {
                        runSafely(new JacksonInitializer());
                    }
                    runSafely(new CharsetInitializer());
                    preinitializationComplete.countDown();
                }

                boolean runSafely(Runnable runnable) {
                    try {
                        runnable.run();
                        return true;
                    } catch (Throwable ex) {
                        return false;
                    }
                }
            }, "background-preinit");
            thread.start();
        } catch (Exception ex) {
            preinitializationComplete.countDown();
        }
    }

    private static class MessageConverterInitializer implements Runnable {

        @Override
        public void run() {
            new AllEncompassingFormHttpMessageConverter();
        }
    }

    private static class ValidationInitializer implements Runnable {

        @Override
        public void run() {
            Configuration<?> configuration = Validation.byDefaultProvider().configure();
            configuration.buildValidatorFactory().getValidator();
        }
    }

    private static class JacksonInitializer implements Runnable {

        @Override
        public void run() {
            Jackson2ObjectMapperBuilder.json().build();
        }
    }

    private static class ConversionServiceInitializer implements Runnable {

        @Override
        public void run() {
            new DefaultFormattingConversionService();
        }
    }

    private static class CharsetInitializer implements Runnable {

        @Override
        public void run() {
            StandardCharsets.UTF_8.name();
        }
    }
}
