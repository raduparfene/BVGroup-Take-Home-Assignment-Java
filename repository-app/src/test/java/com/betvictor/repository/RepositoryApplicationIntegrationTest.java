package com.betvictor.repository;

import com.betvictor.repository.data.TextProcessingResponse;
import com.betvictor.repository.data.TextProcessingResultEntity;
import com.betvictor.repository.repository.TextProcessingResultRepository;
import com.betvictor.repository.service.TextProcessingResultService;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 4, topics = RepositoryApplicationIntegrationTest.TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class RepositoryApplicationIntegrationTest {

    static final String TOPIC = "words.processed";
    private static final String HISTORY_ENDPOINT = "/betvictor/history";
    private static final String CONSUMER_GROUP = "repository-app-integration-test";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String KAFKA_MESSAGE = """
            {
              "freq_word": "artisan",
              "avg_paragraph_size": 42.5,
              "avg_paragraph_processing_time": 0.3,
              "total_processing_time": 210.5
            }
            """;
    // 12 results to verify that only the latest 10 are returned
    private static final List<String> RESULT_WORDS = List.of( "alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet", "kilo", "lima");

    private final int port;
    private final TextProcessingResultService textProcessingResultService;
    private final TextProcessingResultRepository textProcessingResultRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    RepositoryApplicationIntegrationTest(@LocalServerPort int port,
                                         TextProcessingResultService textProcessingResultService,
                                         TextProcessingResultRepository textProcessingResultRepository,
                                         KafkaTemplate<String, String> kafkaTemplate,
                                         EmbeddedKafkaBroker embeddedKafka) {
        this.port = port;
        this.textProcessingResultService = textProcessingResultService;
        this.textProcessingResultRepository = textProcessingResultRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.embeddedKafka = embeddedKafka;
    }

    @BeforeEach
    void clearDatabase() {
        textProcessingResultRepository.deleteAll();
    }

    @Test
    void storesKafkaMessage() {
        SendResult<String, String> sendResult = kafkaTemplate.send(TOPIC, "artisan", KAFKA_MESSAGE).join();

        waitUntilMessageIsStored();
        waitUntilOffsetIsCommitted(sendResult);

        assertThat(embeddedKafka.getPartitionsPerTopic()).isEqualTo(4);
    }

    private void waitUntilMessageIsStored() {
        // Producing and consuming happen on different threads, so persistence is not immediate
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            List<TextProcessingResultEntity> storedResults = textProcessingResultRepository.findAll();

            assertThat(storedResults).hasSize(1);
            TextProcessingResultEntity storedResult = storedResults.getFirst();
            assertThat(storedResult.getFreqWord()).isEqualTo("artisan");
            assertThat(storedResult.getAvgParagraphSize()).isEqualTo(42.5);
            assertThat(storedResult.getAvgParagraphProcessingTime()).isEqualTo(0.3);
            assertThat(storedResult.getTotalProcessingTime()).isEqualTo(210.5);
        });
    }

    private void waitUntilOffsetIsCommitted(SendResult<String, String> sendResult) {
        // Record acknowledgment commits the next offset only after the listener finishes successfully
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            OffsetAndMetadata committedOffset = KafkaTestUtils.getCurrentOffset(
                    embeddedKafka.getBrokersAsString(),
                    CONSUMER_GROUP,
                    TOPIC,
                    sendResult.getRecordMetadata().partition()
            );

            assertThat(committedOffset).isNotNull();
            assertThat(committedOffset.offset()).isEqualTo(sendResult.getRecordMetadata().offset() + 1);
        });
    }

    @Test
    void returnsLastTenResults() {
        for (int resultIndex = 0; resultIndex < RESULT_WORDS.size(); resultIndex++) {
            int resultNumber = resultIndex + 1;
            textProcessingResultService.save(new TextProcessingResponse(RESULT_WORDS.get(resultIndex), resultNumber, resultNumber, resultNumber));
        }

        given()
                .port(port)
                .when()
                .get(HISTORY_ENDPOINT)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(10))
                .body("[0]", aMapWithSize(4))
                .body("[0].freq_word", equalTo("lima"))
                .body("[0].avg_paragraph_size", equalTo(12.0F))
                .body("[9].freq_word", equalTo("charlie"));
    }
}
