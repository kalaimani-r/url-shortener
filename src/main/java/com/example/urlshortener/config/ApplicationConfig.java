package com.example.urlshortener.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
public class ApplicationConfig {

    @Bean
    CacheManager cacheManager(AppProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager("redirectTargets");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.getCache().getRedirectTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(properties.getCache().getMaximumSize())
                .recordStats());
        return manager;
    }

    @Bean(name = "clickEventExecutor")
    @Profile("!test")
    ThreadPoolTaskExecutor clickEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("click-event-");
        executor.initialize();
        return executor;
    }
}
