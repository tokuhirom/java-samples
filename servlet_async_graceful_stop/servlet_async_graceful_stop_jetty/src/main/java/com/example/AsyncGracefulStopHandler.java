package com.example;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.FutureCallback;
import org.eclipse.jetty.util.component.Graceful;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AsyncGracefulStopHandler extends HandlerWrapper implements Graceful {
    private StatisticsHandler statisticsHandler;
    private final AtomicReference<FutureCallback> _shutdown = new AtomicReference<>();

    public AsyncGracefulStopHandler(StatisticsHandler statisticsHandler) {
        this.statisticsHandler = statisticsHandler;
    }

    private final AsyncListener _onCompletion = new AsyncListener() {
        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getAsyncContext().addListener(this);
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            log.info("Finished async request: {}", event.toString());
            event.getSuppliedResponse().flushBuffer();

            FutureCallback shutdown = _shutdown.get();
            if (shutdown != null) {
                if (isFinished()) {
                    log.info("shutdown on onComplete");
                    shutdown.succeeded();
                }
            }
        }
    };

    @Override
    public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        super.handle(target, request, httpServletRequest, response);

        HttpChannelState state = request.getHttpChannelState();
        if (state.isAsync()) {
            state.addListener(_onCompletion);
        }

        FutureCallback shutdown = _shutdown.get();
        if (shutdown != null) {
            if (isFinished()) {
                log.info("shutdown on handle");
                shutdown.succeeded();
            }
        }

    }

    private boolean isFinished() {
        return statisticsHandler.getDispatchedActive() == 0 && statisticsHandler.getAsyncRequestsWaiting() == 0;
    }

    @Override
    public Future<Void> shutdown() {
        FutureCallback shutdown = new FutureCallback(false);
        _shutdown.compareAndSet(null, shutdown);
        shutdown = _shutdown.get();
        if (isFinished()) {
            shutdown.succeeded();
        }
        return shutdown;
    }
}
