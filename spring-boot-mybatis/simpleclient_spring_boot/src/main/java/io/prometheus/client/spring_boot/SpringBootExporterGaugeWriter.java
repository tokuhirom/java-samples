package io.prometheus.client.spring_boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpringBootExporterGaugeWriter implements GaugeWriter {
    private final Map<String, Gauge> gauges;
    private final String namespace;
    private final CollectorRegistry collectorRegistry;

    public SpringBootExporterGaugeWriter(String namespace, CollectorRegistry collectorRegistry) {
        this.namespace = namespace;
        this.collectorRegistry = collectorRegistry;
        this.gauges = new ConcurrentHashMap<>();
    }

    private Gauge getGauge(String name) {
        return gauges.computeIfAbsent(name,
                it -> Gauge.build().name(SpringBootExporterUtils.makeName(name))
                        .namespace(namespace)
                        .help(name)
                        .register(collectorRegistry));
    }

    @Override
    public void set(Metric<?> value) {
        log.debug("SET {}", value);
        String name = value.getName();
        double v = value.getValue().doubleValue();

        getGauge(name).set(v);
    }
}
