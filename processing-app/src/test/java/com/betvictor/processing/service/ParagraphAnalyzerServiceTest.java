package com.betvictor.processing.service;

import com.betvictor.processing.data.ParagraphAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParagraphAnalyzerServiceTest {

    private final ParagraphAnalyzerService analyzerService = new ParagraphAnalyzerService();

    @Test
    void normalizesWords() {
        List<ParagraphAnalysis> analyses = analyzerService.analyzeParagraphs(List.of("Java, JAVA! high-quality."));

        ParagraphAnalysis analysis = analyses.getFirst();
        assertThat(analysis.getWordFrequencies()).containsExactlyInAnyOrderEntriesOf(
                Map.of("java", 2L, "high", 1L, "quality", 1L)
        );
        assertThat(analysis.getWordCount()).isEqualTo(4);
        assertThat(analysis.getProcessingTimeNanos()).isNotNegative();
        assertThat(analyzerService.extractMostFrequentWord(analyses)).isEqualTo("java");
        assertThat(analyzerService.calculateAverageSize(analyses)).isEqualTo(4.0);
        assertThat(analyzerService.calculateAverageProcessingTime(analyses)).isNotNegative();
    }

    @Test
    void resolvesTieAlphabetically() {
        List<ParagraphAnalysis> analyses = analyzerService.analyzeParagraphs(List.of("beta alpha", "alpha beta"));

        assertThat(analyzerService.extractMostFrequentWord(analyses)).isEqualTo("alpha");
        assertThat(analyzerService.calculateAverageSize(analyses)).isEqualTo(2.0);
    }

    @Test
    void rejectsParagraphsWithoutWords() {
        List<ParagraphAnalysis> analyses = analyzerService.analyzeParagraphs(List.of("..."));

        assertThatThrownBy(() -> analyzerService.extractMostFrequentWord(analyses))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Hipsum paragraphs contained no words");
    }
}
