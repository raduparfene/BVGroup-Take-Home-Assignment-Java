package com.betvictor.processing.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class HipsumConfiguration {

    @Bean
    RestClient hipsumRestClient(HipsumProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl().toString())
                .requestFactory(requestFactory)
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
