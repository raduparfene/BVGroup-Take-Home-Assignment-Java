package com.betvictor.repository;

import com.betvictor.repository.consumer.TextProcessingConsumer;
import com.betvictor.repository.data.TextProcessingResponse;
import com.betvictor.repository.data.TextProcessingResultEntity;
import com.betvictor.repository.repository.TextProcessingResultRepository;
import com.betvictor.repository.service.TextProcessingResultService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RepositoryApplicationIntegrationTest {

    private static final String HISTORY_ENDPOINT = "/betvictor/history";
    // 12 results to verify that only the latest 10 are returned
    private static final List<String> RESULT_WORDS = List.of( "alpha", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet", "kilo", "lima");

    private final int port;
    private final TextProcessingConsumer textProcessingConsumer;
    private final TextProcessingResultService textProcessingResultService;
    private final TextProcessingResultRepository textProcessingResultRepository;

    @Autowired
    RepositoryApplicationIntegrationTest(@LocalServerPort int port,
                                         TextProcessingConsumer textProcessingConsumer,
                                         TextProcessingResultService textProcessingResultService,
                                         TextProcessingResultRepository textProcessingResultRepository) {
        this.port = port;
        this.textProcessingConsumer = textProcessingConsumer;
        this.textProcessingResultService = textProcessingResultService;
        this.textProcessingResultRepository = textProcessingResultRepository;
    }

    @BeforeEach
    void clearDatabase() {
        textProcessingResultRepository.deleteAll();
    }

    @Test
    void storesMessage() {
        textProcessingConsumer.consume("""
                {
                  "freq_word": "artisan",
                  "avg_paragraph_size": 42.5,
                  "avg_paragraph_processing_time": 0.3,
                  "total_processing_time": 210.5
                }
                """);

        List<TextProcessingResultEntity> storedResults = textProcessingResultRepository.findAll();

        assertThat(storedResults).hasSize(1);
        TextProcessingResultEntity storedResult = storedResults.getFirst();
        assertThat(storedResult.getFreqWord()).isEqualTo("artisan");
        assertThat(storedResult.getAvgParagraphSize()).isEqualTo(42.5);
        assertThat(storedResult.getAvgParagraphProcessingTime()).isEqualTo(0.3);
        assertThat(storedResult.getTotalProcessingTime()).isEqualTo(210.5);
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
