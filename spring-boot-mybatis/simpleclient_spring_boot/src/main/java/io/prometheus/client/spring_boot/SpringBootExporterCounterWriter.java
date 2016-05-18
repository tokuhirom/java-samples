package io.prometheus.client.spring_boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.writer.CounterWriter;
import org.springframework.boot.actuate.metrics.writer.Delta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpringBootExporterCounterWriter implements CounterWriter {
    private final Map<String, Counter> counters;
    private String namespace;
    private final CollectorRegistry collectorRegistry;

    public SpringBootExporterCounterWriter(String namespace, CollectorRegistry collectorRegistry) {
        this.namespace = namespace;
        this.collectorRegistry = collectorRegistry;
        this.counters = new ConcurrentHashMap<>();
    }

    @Override
    public void increment(Delta<?> delta) {
        log.debug("INCR {}", delta);
        String name = delta.getName();
        double value = delta.getValue().doubleValue();
        getCounter(name).inc(value);
    }

    @Override
    public void reset(String metricName) {
        log.debug("RESET {}", metricName);

        getCounter(metricName).clear();
    }

    private Counter getCounter(String name) {
        return counters.computeIfAbsent(name,
                it -> Counter.build().name(SpringBootExporterUtils.makeName(name))
                        .namespace(namespace)
                        .help(name)
                        .register(collectorRegistry));
    }
}
