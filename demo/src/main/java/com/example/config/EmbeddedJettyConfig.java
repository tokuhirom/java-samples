package com.example.config;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedJettyConfig {
    @Bean
    public EmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(
            @Value("${server.port:8080}") final int port,
            @Value("${jetty.threadPool.minThreads:8}") final int minThreads,
            @Value("${jetty.threadPool.maxThreads:200}") final int maxThreads,
            @Value("${jetty.threadPool.idleTimeout:60000}") final int idleTimeout) {
        JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
        factory.setPort(port);
        factory.addServerCustomizers((JettyServerCustomizer) server -> {
            HandlerCollection handlers = new HandlerCollection();
            for (Handler handler : server.getHandlers()) {
                handlers.addHandler(handler);
            }
            // request logging handler
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            Slf4jRequestLog slf4jRequestLog = new Slf4jRequestLog();
            slf4jRequestLog.setLogTimeZone("JST");
            slf4jRequestLog.setExtended(true);
            slf4jRequestLog.setLogLatency(true);
            requestLogHandler.setRequestLog(slf4jRequestLog);
            handlers.addHandler(requestLogHandler);

            // statistics handler
            StatisticsHandler statisticsHandler = new StatisticsHandler();
            statisticsHandler.setServer(server);

            handlers.addHandler(statisticsHandler);
            // set handlers
            server.setHandler(handlers);

            // embedded jetty server thread settings
            QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMaxThreads(maxThreads);
            threadPool.setMinThreads(minThreads);
            threadPool.setIdleTimeout(idleTimeout);
        });
        return factory;
    }
}
