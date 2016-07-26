package com.example;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class Httpd {
    private StatisticsHandler statisticsHandler;

    public static void main(String[] args) throws Exception {
        Httpd httpd = new Httpd();
        httpd.run();
    }

    private void run() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);

        Server server = startServer(startSignal);

        Thread clientThread = new Thread(this::startClient);
        clientThread.setName("http client");
        clientThread.start();

        log.info("Waiting request");
        startSignal.await();

        server.setStopTimeout(7000);
        server.setDumpBeforeStop(true);

        log.info("async threads: {}, active requests: {}",
                statisticsHandler.getAsyncRequestsWaiting(),
                statisticsHandler.getRequestsActive());
        server.stop();

        log.info("Joining server threads");
        server.join();

        log.info("Joining client thread");
        clientThread.join();

        log.info("Finished");
    }

    private void startClient() {
//        try {
//            OkHttpClient build = new OkHttpClient.Builder()
//                    .build();
//            Response execute = build.newCall(
//                    new Request.Builder()
//                            .url("http://localhost:18080/foo")
//                            .build())
//                    .execute();
//            log.info("wtf {}", execute.body().string());
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
        try (Socket clientSocket = new Socket("localhost", 18080);
             DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            outToServer.write("GET / HTTP/1.0\015\012Content-Length: 0\015\012\015\012".getBytes(StandardCharsets.UTF_8));
            log.info("Sent request");
            clientSocket.shutdownOutput();

            StringBuilder builder = new StringBuilder();
            while (true) {
                byte[] buf = new byte[1024];
                int read = clientSocket.getInputStream().read(buf);
                if (read == -1) {
                    log.info("Got response: {}, {},{},{}, {}",
                            builder.toString(),
                            clientSocket.isConnected(),
                            clientSocket.isBound(),
                            clientSocket.isInputShutdown(),
                            clientSocket.isClosed());
                    break;
                }
                builder.append(new String(buf, 0, read)).append("\\n");
            }
        } catch (IOException e) {
            log.error("IOException", e);
            throw new UncheckedIOException(e);
        }
    }

    private Server startServer(CountDownLatch countDownLatch) throws Exception {
        int port = 18080;
        Server server = new Server(port);

        HandlerCollection handlers = new HandlerCollection();

        handlers.addHandler(servletHandler(countDownLatch));
        handlers.addHandler(requestLogHandler());

        statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handlers);

        AsyncGracefulStopHandler asyncGracefulStopHandler = new AsyncGracefulStopHandler(statisticsHandler);
        asyncGracefulStopHandler.setHandler(statisticsHandler);
        server.setHandler(asyncGracefulStopHandler);

        // start jmx
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addEventListener(mbContainer);
        server.addBean(mbContainer);

        server.start();
        log.info("URI: {}", server.getURI());
        return server;
    }

    private Handler servletHandler(CountDownLatch countDownLatch) {
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(new AsyncResponseServlet(countDownLatch));
        log.info("async supported: {}", servletHolder.isAsyncSupported());
        servletContextHandler.addServlet(servletHolder, "/*");
        return servletContextHandler;
    }

    private RequestLogHandler requestLogHandler() {
        Slf4jRequestLog requestLog = new Slf4jRequestLog();
        requestLog.setExtended(true);
        requestLog.setLogCookies(false);
        requestLog.setLogTimeZone("GMT");

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        return requestLogHandler;
    }
}