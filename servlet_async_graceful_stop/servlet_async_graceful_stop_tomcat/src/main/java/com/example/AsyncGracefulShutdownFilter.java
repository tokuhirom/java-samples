package com.example;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Phaser;

@Slf4j
public class AsyncGracefulShutdownFilter implements Filter {
    private AsyncListener listener = new Listener();
    private Phaser phaser = new Phaser();
    private boolean shutdown = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("filitering");

        if (shutdown) {
            ((HttpServletResponse) response).sendError(503, "Service Temporary Unavailable");
            return;
        }

        phaser.register();
        log.info("register!!!!!: {}", phaser.getUnarrivedParties());
        chain.doFilter(request, response);
        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(listener);
        } else {
            phaser.arrive();
        }
    }

    @Override
    public void destroy() {
        log.info("DESTROY");
    }

    public void shutdown() {
        this.shutdown = true;
    }

    public void awaitShutdown() {
        log.info("Working async requests: {}", phaser.getUnarrivedParties());
        phaser.register();
        phaser.arriveAndAwaitAdvance();
    }

    public class Listener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            log.info("onComplete");
            phaser.arrive();
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            log.info("onTimeout");
            phaser.arrive();
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            log.info("onError");
            phaser.arrive();
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            log.info("onStartAsync");
        }
    }
}
