package org.opengrok.indexer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdMeterRegistry;
import io.micrometer.statsd.StatsdFlavor;
import org.opengrok.indexer.configuration.RuntimeEnvironment;
import org.opengrok.indexer.index.Indexer;
import org.opengrok.indexer.logger.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Metrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Metrics.class);

    private static final StatsdConfig statsdConfig = new StatsdConfig() {

        @Override
        public String get(String k) {
            return null;
        }

        @Override
        public StatsdFlavor flavor() {
            return RuntimeEnvironment.getInstance().getStatsdConfig().getFlavor();
        }

        @Override
        public int port() {
            return RuntimeEnvironment.getInstance().getStatsdConfig().getPort();
        }

        @Override
        public String host() {
            return RuntimeEnvironment.getInstance().getStatsdConfig().getHost();
        }

        @Override
        public boolean buffered() {
            return true;
        }
    };

    private static PrometheusMeterRegistry prometheusRegistry;

    private static StatsdMeterRegistry statsdRegistry;

    static {
        MeterRegistry registry = null;
        if (RuntimeEnvironment.getInstance().getStatsdConfig().isEnabled()) {
            LOGGER.log(Level.INFO, "configuring StatsdRegistry");
            statsdRegistry = new StatsdMeterRegistry(statsdConfig, Clock.SYSTEM);
            registry = statsdRegistry;
        } else if (!RuntimeEnvironment.getInstance().isIndexer()) {
            LOGGER.log(Level.INFO, "configuring PrometheusRegistry");
            prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            registry = prometheusRegistry;
        }
        if (registry != null) {
            new ClassLoaderMetrics().bindTo(registry);
            new JvmMemoryMetrics().bindTo(registry);
            new JvmGcMetrics().bindTo(registry);
            new ProcessorMetrics().bindTo(registry);
            new JvmThreadMetrics().bindTo(registry);
        }
    }

    private Metrics() {
    }

    public static void updateSubFiles(List<String> subFiles) {
        if (statsdRegistry != null && !subFiles.isEmpty()) {
            String projects = subFiles.stream().map(s -> s.startsWith(Indexer.PATH_SEPARATOR_STRING) ? s.substring(1) : s).collect(Collectors.joining(","));
            Tag commonTag = Tag.of("projects", projects);
            LOGGER.log(Level.FINE, "updating statsdRegistry with common tag: {}", commonTag);
            statsdRegistry.config().commonTags(Collections.singleton(commonTag));
        }
    }

    public static PrometheusMeterRegistry getPrometheusRegistry() {
        return prometheusRegistry;
    }

    private static StatsdMeterRegistry getStatsdRegistry() {
        return statsdRegistry;
    }

    public static MeterRegistry getRegistry() {
        if (RuntimeEnvironment.getInstance().isIndexer()) {
            return getStatsdRegistry();
        } else {
            return getPrometheusRegistry();
        }
    }
}
