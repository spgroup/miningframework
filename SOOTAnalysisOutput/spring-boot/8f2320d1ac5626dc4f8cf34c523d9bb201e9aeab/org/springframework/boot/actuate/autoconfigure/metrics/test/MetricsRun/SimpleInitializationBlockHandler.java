package org.springframework.boot.actuate.autoconfigure.metrics.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.atlas.AtlasMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.datadog.DatadogMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ganglia.GangliaMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.jmx.JmxMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.newrelic.NewRelicMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx.SignalFxMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.util.Assert;

public final class MetricsRun {

    private static final Set<Class<?>> EXPORT_AUTO_CONFIGURATIONS;

    static {
        Set<Class<?>> implementations = new LinkedHashSet<>();
        implementations.add(AtlasMetricsExportAutoConfiguration.class);
        implementations.add(DatadogMetricsExportAutoConfiguration.class);
        implementations.add(GangliaMetricsExportAutoConfiguration.class);
        implementations.add(GraphiteMetricsExportAutoConfiguration.class);
        implementations.add(InfluxMetricsExportAutoConfiguration.class);
        implementations.add(JmxMetricsExportAutoConfiguration.class);
        implementations.add(NewRelicMetricsExportAutoConfiguration.class);
        implementations.add(OtlpMetricsExportAutoConfiguration.class);
        implementations.add(PrometheusMetricsExportAutoConfiguration.class);
        implementations.add(SimpleMetricsExportAutoConfiguration.class);
        implementations.add(SignalFxMetricsExportAutoConfiguration.class);
        implementations.add(StatsdMetricsExportAutoConfiguration.class);
        EXPORT_AUTO_CONFIGURATIONS = Collections.unmodifiableSet(implementations);
    }

    private static final AutoConfigurations AUTO_CONFIGURATIONS = AutoConfigurations.of(MetricsAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class);

    private MetricsRun() {
    }

    public static <T extends AbstractApplicationContextRunner<?, ?, ?>> Function<T, T> simple() {
        return limitedTo(SimpleMetricsExportAutoConfiguration.class);
    }

    public static <T extends AbstractApplicationContextRunner<?, ?, ?>> Function<T, T> limitedTo(Class<?>... exportAutoConfigurations) {
        return (contextRunner) -> apply(contextRunner, exportAutoConfigurations);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractApplicationContextRunner<?, ?, ?>> T apply(T contextRunner, Class<?>[] exportAutoConfigurations) {
        for (Class<?> configuration : exportAutoConfigurations) {
            Assert.state(EXPORT_AUTO_CONFIGURATIONS.contains(configuration), () -> "Unknown export auto-configuration " + configuration.getName());
        }
        return (T) contextRunner.withPropertyValues("management.metrics.use-global-registry=false").withConfiguration(AUTO_CONFIGURATIONS).withConfiguration(AutoConfigurations.of(exportAutoConfigurations));
    }
}