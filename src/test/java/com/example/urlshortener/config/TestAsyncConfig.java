package com.example.urlshortener.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;

@TestConfiguration
@Profile("test")
public class TestAsyncConfig {

    @Bean(name = "clickEventExecutor")
    Executor syncClickEventExecutor() {
        return new SyncTaskExecutor();
    }
}
