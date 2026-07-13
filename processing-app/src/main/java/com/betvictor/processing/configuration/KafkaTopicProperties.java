package com.betvictor.processing.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.kafka")
@Getter
@Setter
@Validated
public class KafkaTopicProperties {

    @NotBlank(message = "app.kafka.topic-name must be configured")
    private String topicName;
}
