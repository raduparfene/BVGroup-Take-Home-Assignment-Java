package com.betvictor.processing.client;

import com.betvictor.processing.configuration.HipsumProperties;
import com.betvictor.processing.exception.HipsumClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HipsumClientTest {

    private static final String HIPSUM_BASE_URL = "https://hipsum.test";

    private MockRestServiceServer server;
    private HipsumClient hipsumClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        HipsumProperties properties = new HipsumProperties();
        properties.setType("hipster-centric");
        hipsumClient = new HipsumClient(builder.baseUrl(HIPSUM_BASE_URL).build(), properties);
    }

    @Test
    void requestsOneParagraph() {
        expectSuccessfulResponse(1, "[\"Hello from Hipsum.\"]");

        List<String> paragraphs = hipsumClient.fetchParagraphs(1);

        assertThat(paragraphs).containsExactly("Hello from Hipsum.");
        server.verify();
    }

    @Test
    void requestsMultipleParagraphs() {
        expectSuccessfulResponse(2, "[\"First paragraph.\",\"Second paragraph.\"]");

        List<String> paragraphs = hipsumClient.fetchParagraphs(2);

        assertThat(paragraphs).containsExactly("First paragraph.", "Second paragraph.");
        server.verify();
    }

    @Test
    void rejectsUnexpectedParagraphCount() {
        expectSuccessfulResponse(2, "[\"Only one paragraph.\"]");

        assertThatThrownBy(() -> hipsumClient.fetchParagraphs(2))
                .isInstanceOf(HipsumClientException.class)
                .hasMessage("Hipsum returned an unexpected number of paragraphs");

        server.verify();
    }

    @Test
    void rejectsEmptyParagraph() {
        expectSuccessfulResponse(1, "[\"   \"]");

        assertThatThrownBy(() -> hipsumClient.fetchParagraphs(1))
                .isInstanceOf(HipsumClientException.class)
                .hasMessage("Hipsum returned an empty paragraph");

        server.verify();
    }

    private void expectSuccessfulResponse(int paragraphCount, String responseBody) {
        server.expect(requestTo(hipsumUrl(paragraphCount)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }

    private String hipsumUrl(int paragraphCount) {
        return HIPSUM_BASE_URL + "/api/?type=hipster-centric&paras=" + paragraphCount;
    }
}
