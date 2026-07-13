package com.betvictor.processing.controller;

import com.betvictor.processing.client.HipsumClient;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TextProcessingApiIntegrationTest {

    private static final String TEXT_ENDPOINT = "/betvictor/text";

    @LocalServerPort
    private int port;

    @MockitoBean
    private HipsumClient hipsumClient;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void returnsAndPublishesSameResult() {
        when(hipsumClient.fetchParagraphs(1))
                .thenReturn(List.of("Alpha beta beta"), List.of("alpha alpha beta gamma"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        String httpResponse = requestText(2)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", aMapWithSize(4))
                .body("freq_word", equalTo("alpha"))
                .body("avg_paragraph_size", equalTo(3.5F))
                .body("avg_paragraph_processing_time", greaterThanOrEqualTo(0.0F))
                .body("total_processing_time", greaterThanOrEqualTo(0.0F))
                .extract()
                .asString();

        verify(hipsumClient, times(2)).fetchParagraphs(1);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("words.processed"), eq("alpha"), messageCaptor.capture());
        assertThat(jsonMapper.readTree(messageCaptor.getValue())).isEqualTo(jsonMapper.readTree(httpResponse));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0|p must be greater than zero",
            "-1|p must be greater than zero",
            "not-a-number|p must be a valid integer"
    }, delimiter = '|')
    void rejectsInvalidParagraphCount(String paragraphCount, String expectedMessage) {
        assertError(requestText(paragraphCount), 400, expectedMessage);
        verifyNoInteractions(hipsumClient);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void rejectsMissingParagraphCount() {
        assertError(requestTextWithoutParagraphCount(), 400, "Required request parameter 'p' is missing");
        verifyNoInteractions(hipsumClient);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void returnsBadGatewayForHipsumFailure() {
        when(hipsumClient.fetchParagraphs(1)).thenThrow(new RestClientException("connection failed"));

        assertError(requestText(1), 502, "Failed to retrieve paragraphs from Hipsum");
        verify(hipsumClient).fetchParagraphs(1);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void returnsServiceUnavailableForKafkaFailure() {
        when(hipsumClient.fetchParagraphs(1)).thenReturn(List.of("alpha beta beta"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new KafkaException("broker unavailable")));

        assertError(requestText(1), 503, "Failed to publish the processing result");
        verify(hipsumClient).fetchParagraphs(1);
        verify(kafkaTemplate).send(eq("words.processed"), eq("beta"), anyString());
    }

    private ValidatableResponse requestText(Object paragraphCount) {
        return given()
                .port(port)
                .queryParam("p", paragraphCount)
                .when()
                .get(TEXT_ENDPOINT)
                .then();
    }

    private ValidatableResponse requestTextWithoutParagraphCount() {
        return given()
                .port(port)
                .when()
                .get(TEXT_ENDPOINT)
                .then();
    }

    private void assertError(ValidatableResponse response, int status, String message) {
        response
                .statusCode(status)
                .contentType(ContentType.JSON)
                .body("status", equalTo(status))
                .body("message", equalTo(message))
                .body("path", equalTo(TEXT_ENDPOINT));
    }
}
