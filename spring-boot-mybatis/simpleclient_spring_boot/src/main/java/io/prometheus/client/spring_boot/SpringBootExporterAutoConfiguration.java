package io.prometheus.client.spring_boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(CollectorRegistry.class)
public class SpringBootExporterAutoConfiguration {
    @Value("${simpleclient_spring_boot.namespace:spring_boot}")
    private String namespace;

    @Autowired(required = false)
    private HealthAggregator healthAggregator = new OrderedHealthAggregator();

    @Autowired(required = false)
    private Map<String, HealthIndicator> healthIndicators = new HashMap<>();

    @Bean
    public SpringBootExporterEndpoint prometheusEndpoint() {
        return new SpringBootExporterEndpoint();
    }

    @Bean
    @ExportMetricWriter
    public SpringBootExporterGaugeWriter prometheusGaugeWriter(CollectorRegistry collectorRegistry) {
        return new SpringBootExporterGaugeWriter(namespace, collectorRegistry);
    }

    @Bean
    @ExportMetricWriter
    public SpringBootExporterCounterWriter prometheusCounterWriter(CollectorRegistry collectorRegistry) {
        return new SpringBootExporterCounterWriter(namespace, collectorRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectorRegistry collectorRegistry(SpringBootHealthCollector springBootHealthCollector) {
        CollectorRegistry registry = CollectorRegistry.defaultRegistry;

        new StandardExports().register(registry);
        new MemoryPoolsExports().register(registry);
        new GarbageCollectorExports().register(registry);

        springBootHealthCollector.register(registry);

        return registry;
    }

    @Bean
    public SpringBootHealthCollector springBootHealthCollector() {
        return new SpringBootHealthCollector(namespace, healthAggregator, healthIndicators);
    }

    @Bean
    public SpringBootExporterMvcEndpoint prometheusMvcEndpoint(SpringBootExporterEndpoint springBootExporterEndpoint, CollectorRegistry collectorRegistry) {
        return new SpringBootExporterMvcEndpoint(springBootExporterEndpoint, collectorRegistry);
    }
}
