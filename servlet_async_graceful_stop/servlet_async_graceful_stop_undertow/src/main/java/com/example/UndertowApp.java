package com.example;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.protocol.http.HttpOpenListener;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import lombok.extern.slf4j.Slf4j;
import org.xnio.*;
import org.xnio.channels.AcceptingChannel;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class UndertowApp {
    public static void main(String[] args) throws ServletException, InterruptedException, IOException {
        new UndertowApp().run();
    }

    private void run() throws ServletException, InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);

        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(UndertowApp.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("async.war")
                .addServlets(
                        Servlets.servlet("MessageServlet", AsyncResponseServlet.class, () -> new InstanceHandle<Servlet>() {
                            @Override
                            public Servlet getInstance() {
                                log.info("Getting instance");
                                return new AsyncResponseServlet(latch);
                            }

                            @Override
                            public void release() {
                                /* nop */
                            }
                        })
                                .setAsyncSupported(true)
                                .addInitParam("message", "Hello World")
                                .addMapping("/*")
                );

        DeploymentManager manager = Servlets.defaultContainer()
                .addDeployment(servletBuilder);
        manager.deploy();
        PathHandler path = Handlers
                .path()
                .addExactPath("/", manager.start());

        int ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), 2);
        int workerThreads = ioThreads * 8;

        Xnio xnio = Xnio.getInstance(Undertow.class.getClassLoader());

        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, ioThreads)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, workerThreads)
                .set(Options.WORKER_TASK_MAX_THREADS, workerThreads)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());

        OptionMap socketOptions = OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, ioThreads)
                .set(Options.TCP_NODELAY, true)
                .set(Options.REUSE_ADDRESSES, true)
                .set(Options.BALANCING_TOKENS, 1)
                .set(Options.BALANCING_CONNECTIONS, 2)
                .set(Options.BACKLOG, 1000)
                .getMap();

        OptionMap serverOptions = OptionMap.builder()
                .set(UndertowOptions.NO_REQUEST_TIMEOUT, 60000000)
                .getMap();

        ByteBufferPool buffers = new DefaultByteBufferPool(true, 1024 * 16, -1, 4);

        GracefulShutdownHandler gracefulShutdownHandler = Handlers.gracefulShutdown(path);

        OptionMap undertowOptions = OptionMap.builder().set(UndertowOptions.BUFFER_PIPELINED_DATA, true).addAll(serverOptions).getMap();
        HttpOpenListener openListener = new HttpOpenListener(buffers, undertowOptions);
        openListener.setRootHandler(gracefulShutdownHandler);
        ChannelListener<AcceptingChannel<StreamConnection>> acceptListener = ChannelListeners.openListenerAdapter(openListener);
        AcceptingChannel<? extends StreamConnection> server = worker.createStreamConnectionServer(new InetSocketAddress(Inet4Address.getByName("localhost"), 18080), acceptListener, socketOptions);
        server.resumeAccepts();

        log.info("Send request");
        Thread clientThread = new Thread(this::startClient);
        clientThread.setName("http client");
        clientThread.start();

        log.info("Waiting request");
        latch.await();

        log.info("Stopping listening channel");
        IoUtils.safeClose(server);

        log.info("Entering shutdown state");
        gracefulShutdownHandler.shutdown();

        log.info("Await all requests(7sec)");
        gracefulShutdownHandler.awaitShutdown(7 * 1000);

        log.info("Shutdown workers");
        worker.shutdown();

        log.info("Stopped");

        manager.stop();
        manager.undeploy();

        log.info("Undeployed");

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
                    Thread.currentThread().interrupt();
                }

                AsyncResponseServlet.log.info("Sending response");
                resp.setStatus(200);
                try {
                    resp.getWriter().print("OK\n");
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
