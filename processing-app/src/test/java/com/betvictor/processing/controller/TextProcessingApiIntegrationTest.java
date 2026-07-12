package com.betvictor.processing.controller;

import com.betvictor.processing.client.HipsumClient;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;

import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

    @Test
    void returnsProcessedText() {
        when(hipsumClient.fetchParagraphs(1))
                .thenReturn(List.of("Alpha beta beta"), List.of("alpha alpha beta gamma"));

        requestText(2)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", aMapWithSize(4))
                .body("freq_word", equalTo("alpha"))
                .body("avg_paragraph_size", equalTo(3.5F))
                .body("avg_paragraph_processing_time", greaterThanOrEqualTo(0.0F))
                .body("total_processing_time", greaterThanOrEqualTo(0.0F));

        verify(hipsumClient, times(2)).fetchParagraphs(1);
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
    }

    @Test
    void rejectsMissingParagraphCount() {
        assertError(requestTextWithoutParagraphCount(), 400, "Required request parameter 'p' is missing");
        verifyNoInteractions(hipsumClient);
    }

    @Test
    void returnsBadGatewayForHipsumFailure() {
        when(hipsumClient.fetchParagraphs(1)).thenThrow(new RestClientException("connection failed"));

        assertError(requestText(1), 502, "Failed to retrieve paragraphs from Hipsum");
        verify(hipsumClient).fetchParagraphs(1);
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
