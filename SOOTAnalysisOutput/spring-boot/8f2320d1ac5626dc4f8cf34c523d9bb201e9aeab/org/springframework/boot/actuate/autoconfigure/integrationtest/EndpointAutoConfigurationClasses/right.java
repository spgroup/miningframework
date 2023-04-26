package org.springframework.boot.actuate.autoconfigure.integrationtest;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration;
import org.springframework.util.ClassUtils;

final class EndpointAutoConfigurationClasses {

    static final Class<?>[] ALL;

    static {
        List<Class<?>> all = new ArrayList<>();
        all.add(AuditEventsEndpointAutoConfiguration.class);
        all.add(BeansEndpointAutoConfiguration.class);
        all.add(ConditionsReportEndpointAutoConfiguration.class);
        all.add(ConfigurationPropertiesReportEndpointAutoConfiguration.class);
        all.add(ShutdownEndpointAutoConfiguration.class);
        all.add(EnvironmentEndpointAutoConfiguration.class);
        all.add(HealthEndpointAutoConfiguration.class);
        all.add(InfoEndpointAutoConfiguration.class);
        all.add(ThreadDumpEndpointAutoConfiguration.class);
        all.add(HttpExchangesEndpointAutoConfiguration.class);
        all.add(MappingsEndpointAutoConfiguration.class);
        ALL = ClassUtils.toClassArray(all);
    }

    private EndpointAutoConfigurationClasses() {
    }
}
