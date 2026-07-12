package com.betvictor.processing.service;

import com.betvictor.processing.data.TextProcessingResponse;
import com.betvictor.processing.exception.InvalidParagraphCountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TextProcessingServiceTest {

    private HipsumParagraphFetcherService hipsumParagraphFetcherService;
    private TextProcessingService textProcessingService;

    @BeforeEach
    void setUp() {
        hipsumParagraphFetcherService = mock(HipsumParagraphFetcherService.class);
        textProcessingService = new TextProcessingService(hipsumParagraphFetcherService, new ParagraphAnalyzerService());
    }

    @Test
    void processesParagraphs() {
        when(hipsumParagraphFetcherService.fetchParagraphs(2)).thenReturn(List.of("Alpha beta beta", "alpha alpha beta gamma"));

        TextProcessingResponse response = textProcessingService.process(2);

        assertThat(response.getFreqWord()).isEqualTo("alpha");
        assertThat(response.getAvgParagraphSize()).isEqualTo(3.5);
        assertThat(response.getAvgParagraphProcessingTime()).isNotNegative();
        assertThat(response.getTotalProcessingTime()).isNotNegative();
        verify(hipsumParagraphFetcherService).fetchParagraphs(2);
    }

    @Test
    void rejectsInvalidParagraphCount() {
        assertThatThrownBy(() -> textProcessingService.process(0))
                .isInstanceOf(InvalidParagraphCountException.class)
                .hasMessage("p must be greater than zero");

        verifyNoInteractions(hipsumParagraphFetcherService);
    }
}
