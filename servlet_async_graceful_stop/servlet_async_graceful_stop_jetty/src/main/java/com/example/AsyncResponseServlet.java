package com.example;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AsyncResponseServlet extends HttpServlet {
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final CountDownLatch latch;

    public AsyncResponseServlet(CountDownLatch latch) {
        this.latch = latch;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Got request: {}", req.getPathInfo());
        this.latch.countDown();

        AsyncContext asyncContext = req.startAsync();

        pool.submit(() -> {
            log.info("Sleeping...");
            try {
                Thread.sleep(3L * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupted();
            }

            log.info("Sending response");
            resp.setStatus(200);
            try {
                resp.getWriter().print("OK\n");
                resp.getWriter().close();
            } catch (IOException e) {
                log.error("Can't send response", e);
            } finally {
                log.info("complete async thread");
                asyncContext.complete();
            }
        });
    }

    @Override
    public void destroy() {
        this.pool.shutdown();
    }
}
