package com.totoro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class EmailApplication {

    /**
     * Main method to run a standalone application server
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(EmailApplication.class, args);
    }

    /**
     * Task executor that is used to run 'Future - @Async' method
     *
     * @return Executor
     */
    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}
