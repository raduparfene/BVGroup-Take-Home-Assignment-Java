package com.betvictor.processing.client;

import com.betvictor.processing.configuration.HipsumProperties;
import com.betvictor.processing.exception.HipsumClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HipsumClient {

    private final RestClient restClient;
    private final HipsumProperties hipsumProperties;

    public List<String> fetchParagraphs(int paragraphCount) {
        if (paragraphCount <= 0) {
            throw new IllegalArgumentException("paragraphCount must be greater than zero");
        }

        String[] paragraphsArray = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/")
                        .queryParam("type", hipsumProperties.getType())
                        .queryParam("paras", paragraphCount)
                        .build())
                .retrieve()
                .body(String[].class);
        List<String> paragraphs = paragraphsArray != null ? Arrays.asList(paragraphsArray) : List.of();

        validateResponse(paragraphs, paragraphCount);
        return paragraphs;
    }

    private void validateResponse(List<String> paragraphs, int expectedParagraphCount) {
        if (CollectionUtils.isEmpty(paragraphs) || paragraphs.size() != expectedParagraphCount) {
            throw new HipsumClientException("Hipsum returned an unexpected number of paragraphs");
        }

        for (String paragraph : paragraphs) {
            if (!StringUtils.hasText(paragraph)) {
                throw new HipsumClientException("Hipsum returned an empty paragraph");
            }
        }
    }
}
