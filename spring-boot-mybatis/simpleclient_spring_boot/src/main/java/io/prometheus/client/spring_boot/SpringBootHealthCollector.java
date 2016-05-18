package io.prometheus.client.spring_boot;

import io.prometheus.client.Collector;
import org.springframework.boot.actuate.health.*;

import java.util.*;

public class SpringBootHealthCollector extends Collector {
    private final CompositeHealthIndicator healthIndicator;
    private String namespace;

    public SpringBootHealthCollector(String namespace, HealthAggregator healthAggregator, Map<String, HealthIndicator> healthIndicators) {
        this.namespace = namespace;

        this.healthIndicator = new CompositeHealthIndicator(healthAggregator);
        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            healthIndicator.addHealthIndicator(getKey(entry.getKey()), entry.getValue());
        }
    }

    private String getKey(String name) {
        int index = name.toLowerCase().indexOf("healthindicator");
        if (index > 0) {
            return name.substring(0, index);
        }
        return name;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<>();
        renderHealth("health", healthIndicator.health(), samples);
        return samples;
    }

    private void renderHealth(String prefix, Health health, List<MetricFamilySamples> samples) {
        String name = namespace + "_" + prefix;
        double value = Objects.equals(Status.UP, health.getStatus()) ? 1 : 0;
        samples.add(new MetricFamilySamples(name, Type.GAUGE, name,
                Collections.singletonList(new MetricFamilySamples.Sample(name, Collections.emptyList(), Collections.emptyList(), value))));

        for (Map.Entry<String, Object> entry : health.getDetails().entrySet()) {
            // key: diskSpace or db
            Object v = entry.getValue();
            if (v instanceof Health) {
                renderHealth(prefix + "_" + entry.getKey(), (Health) v, samples);
            }
        }
    }

}
