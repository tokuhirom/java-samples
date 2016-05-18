package io.prometheus.client.spring_boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.boot.actuate.endpoint.mvc.AbstractEndpointMvcAdapter;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.StringWriter;

public class SpringBootExporterMvcEndpoint extends AbstractEndpointMvcAdapter
        implements MvcEndpoint {
    private final CollectorRegistry collectorRegistry;

    public SpringBootExporterMvcEndpoint(
            SpringBootExporterEndpoint delegate,
            CollectorRegistry registry) {
        super(delegate);
        this.collectorRegistry = registry;
    }

    @RequestMapping(value = "",
            method = RequestMethod.GET,
            produces = TextFormat.CONTENT_TYPE_004)
    @ResponseBody
    public String getDocumentation() throws IOException {
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
        return writer.toString();
    }
}
