package com.betvictor.repository.configuration;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.history")
@Getter
@Setter
@Validated
public class HistoryProperties {

    @Positive(message = "app.history.limit must be greater than zero")
    private int limit;
}
