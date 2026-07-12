package com.betvictor.processing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class HipsumConfiguration {

    @Bean
    RestClient hipsumRestClient(HipsumProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl().toString())
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    ExecutorService hipsumExecutor(HipsumProperties properties) {
        return Executors.newFixedThreadPool(
                properties.getMaxConcurrency(),
                Thread.ofPlatform().name("hipsum-", 0).factory()
        );
    }
}
