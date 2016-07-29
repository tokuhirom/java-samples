package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

@Slf4j
public class TomcatApp {
    public static void main(String[] args) throws Exception {
        new TomcatApp().run();
    }

    private void run() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(18080);
        org.apache.catalina.Context ctx = tomcat.addContext("",
                new File(".").getAbsolutePath());

        AsyncGracefulShutdownFilter asyncGracefulShutdownFilter = new AsyncGracefulShutdownFilter();

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(AsyncGracefulShutdownFilter.class.getSimpleName());
        filterDef.setFilter(asyncGracefulShutdownFilter);
        filterDef.setAsyncSupported("true");
        ctx.addFilterDef(filterDef);

        FilterMap filter1mapping = new FilterMap();
        filter1mapping.setFilterName(AsyncGracefulShutdownFilter.class.getSimpleName());
        filter1mapping.addURLPattern("/*");
        ctx.addFilterMap(filter1mapping);

        Tomcat.addServlet(ctx, "async", new AsyncResponseServlet(latch))
                .setAsyncSupported(true);
        ctx.addServletMapping("/*", "async");

        ctx.addLifecycleListener(event -> {
            LifecycleState state = event.getLifecycle().getState();
            log.info("Lifecycle: {}", event.getLifecycle().getStateName());

            if (state == LifecycleState.STOPPING) {
                log.info("Stopping!!!");

            }
        });

        tomcat.start();

        log.info("Send request");
        Thread clientThread = new Thread(this::startClient);
        clientThread.setName("http client");
        clientThread.start();

        log.info("Waiting request");
        latch.await();

        log.info("Shutdown asyncGracefulShutdownFilter");
        asyncGracefulShutdownFilter.shutdown();
        asyncGracefulShutdownFilter.awaitShutdown();

        log.info("Finished all worker threads");
        tomcat.stop();

        log.info("joining client thread");
        clientThread.join();
    }

    private void startClient() {
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

    @Slf4j
    public static class AsyncResponseServlet extends HttpServlet {
        private final ExecutorService pool = Executors.newFixedThreadPool(10);
        private final CountDownLatch latch;

        public AsyncResponseServlet(CountDownLatch latch) {
            this.latch = latch;
        }


        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            AsyncResponseServlet.log.info("Got request: {}", req.getPathInfo());
            this.latch.countDown();

            AsyncContext asyncContext = req.startAsync();

            pool.submit(() -> {
                AsyncResponseServlet.log.info("Sleeping...");
                try {
                    Thread.sleep(3L * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupted();
                }

                AsyncResponseServlet.log.info("Sending response");
                resp.setStatus(200);
                try {
                    resp.getWriter().print("OK\n");
                    resp.getWriter().close();
                } catch (IOException e) {
                    AsyncResponseServlet.log.error("Can't send response", e);
                } finally {
                    AsyncResponseServlet.log.info("complete async thread");
                    asyncContext.complete();
                }
            });
        }

        @Override
        public void destroy() {
            AsyncResponseServlet.log.info("Shutdown servlet");
            this.pool.shutdown();
        }
    }

}
