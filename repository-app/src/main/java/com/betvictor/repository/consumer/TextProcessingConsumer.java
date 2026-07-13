package com.betvictor.repository.consumer;

import com.betvictor.repository.data.TextProcessingResponse;
import com.betvictor.repository.service.TextProcessingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class TextProcessingConsumer {

    private final JsonMapper jsonMapper;
    private final TextProcessingResultService textProcessingResultService;

    @KafkaListener(topics = "${app.kafka.topic-name}", concurrency = "${app.kafka.consumer-concurrency}")
    public void consume(String message) {
        TextProcessingResponse response = jsonMapper.readValue(message, TextProcessingResponse.class);
        textProcessingResultService.save(response);
    }
}
