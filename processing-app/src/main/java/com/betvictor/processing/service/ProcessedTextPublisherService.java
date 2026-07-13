package com.betvictor.processing.service;

import com.betvictor.processing.configuration.KafkaTopicProperties;
import com.betvictor.processing.data.TextProcessingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class ProcessedTextPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final KafkaTopicProperties kafkaTopicProperties;

    public void publish(TextProcessingResponse response) {
        String message = jsonMapper.writeValueAsString(response);
        kafkaTemplate.send(kafkaTopicProperties.getTopicName(), response.getFreqWord(), message).join();
    }
}
