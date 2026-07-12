package com.betvictor.processing.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties("hipsum")
@Getter
@Setter
@Validated
public class HipsumProperties {

    @NotNull(message = "hipsum.base-url must be configured")
    private URI baseUrl;

    @Min(value = 1, message = "hipsum.max-concurrency must be greater than zero")
    private int maxConcurrency;

    @NotBlank(message = "hipsum.type must be configured")
    private String type;
}
