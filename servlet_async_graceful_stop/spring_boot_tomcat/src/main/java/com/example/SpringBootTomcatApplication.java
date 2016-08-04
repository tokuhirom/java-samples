package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class SpringBootTomcatApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootTomcatApplication.class, args);
    }

    @Bean(name = "async1")
    public AsyncTaskExecutor mvcAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(7);
        return executor;
    }

    @RestController
    public static class MyController {
        @Autowired
        @Qualifier("async1")
        AsyncTaskExecutor asyncTaskExecutor;

        @GetMapping("/")
        public String ok() {
            return "OK";
        }

        @GetMapping("/sleep")
        public DeferredResult<String> sleep() {
            DeferredResult<String> objectDeferredResult = new DeferredResult<>();
            asyncTaskExecutor.submit(() -> {
                try {
                    log.info("Sleeping");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Send result");
                objectDeferredResult.setResult("OK");
            });
            return objectDeferredResult;
        }
    }
}
