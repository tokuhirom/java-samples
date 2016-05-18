package io.prometheus.client.spring_boot;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;

public class SpringBootExporterEndpoint extends AbstractEndpoint
        implements Endpoint {
    public SpringBootExporterEndpoint() {
        super("prometheus");
    }

    @Override
    public Object invoke() {
        return null;
    }
}
