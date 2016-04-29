package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {
    @Autowired
    private JobBuilderFactory jobs;
    @Autowired
    private StepBuilderFactory steps;


    @Bean
    SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
    @Bean
    public SimpleJobLauncher simpleJobLauncher(JobRepository jobRepository,
                                               SimpleAsyncTaskExecutor executor) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(executor);
        return jobLauncher;
    }

    @Bean(name = "job1")
    public Job job() {
        Step step =  steps.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info("Start step1");
                    Thread.sleep(10_000);
                    log.info("done!");
                    return RepeatStatus.FINISHED;
                })
                .build();

        Job myJob = jobs.get("myJob")
                .start(step)
                .preventRestart()
                .build();

        return myJob;
    }
}
