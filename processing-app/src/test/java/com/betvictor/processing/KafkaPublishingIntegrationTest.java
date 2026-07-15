package com.betvictor.processing;

import com.betvictor.processing.data.TextProcessingResponse;
import com.betvictor.processing.service.ProcessedTextPublisherService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest
@EmbeddedKafka(partitions = 4, topics = KafkaPublishingIntegrationTest.TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class KafkaPublishingIntegrationTest {

    static final String TOPIC = "words.processed";

    private final ProcessedTextPublisherService publisherService;
    private final EmbeddedKafkaBroker embeddedKafka;
    private final JsonMapper jsonMapper;

    private Consumer<String, String> consumer;

    @Autowired
    KafkaPublishingIntegrationTest(ProcessedTextPublisherService publisherService, EmbeddedKafkaBroker embeddedKafka, JsonMapper jsonMapper) {
        this.publisherService = publisherService;
        this.embeddedKafka = embeddedKafka;
        this.jsonMapper = jsonMapper;
    }

    @BeforeEach
    void createConsumer() {
        // This consumer reads back what the application publisher sends to the temporary broker.
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(embeddedKafka, "processing-kafka-test", false);
        consumer = new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TOPIC);
    }

    @AfterEach
    void closeConsumer() {
        consumer.close();
    }

    @Test
    void publishesSameWordsInOrder() {
        TextProcessingResponse firstResponse = new TextProcessingResponse("artisan", 10.0, 1.0, 100.0);
        TextProcessingResponse secondResponse = new TextProcessingResponse("artisan", 20.0, 2.0, 200.0);

        publisherService.publish(firstResponse);
        publisherService.publish(secondResponse);

        List<ConsumerRecord<String, String>> publishedRecords = new ArrayList<>();
        KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10), 2)
                .records(TOPIC)
                .forEach(publishedRecords::add);

        assertThat(embeddedKafka.getPartitionsPerTopic()).isEqualTo(4);
        assertThat(publishedRecords).hasSize(2);
        assertThat(publishedRecords).extracting(ConsumerRecord::key).containsExactly("artisan", "artisan");
        assertThat(publishedRecords).extracting(ConsumerRecord::partition).containsOnly(publishedRecords.getFirst().partition());
        assertThat(jsonMapper.readTree(publishedRecords.getFirst().value())).isEqualTo(jsonMapper.readTree(jsonMapper.writeValueAsString(firstResponse)));
        assertThat(jsonMapper.readTree(publishedRecords.getLast().value())).isEqualTo(jsonMapper.readTree(jsonMapper.writeValueAsString(secondResponse)));
    }
}
