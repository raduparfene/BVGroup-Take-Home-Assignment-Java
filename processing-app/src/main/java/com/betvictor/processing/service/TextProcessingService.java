package com.betvictor.processing.service;

import com.betvictor.processing.data.ParagraphAnalysis;
import com.betvictor.processing.data.TextProcessingResponse;
import com.betvictor.processing.exception.InvalidParagraphCountException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TextProcessingService {

    private static final double NANOS_PER_MILLISECOND = 1_000_000.0;

    private final HipsumParagraphFetcherService hipsumParagraphFetcherService;
    private final ParagraphAnalyzerService paragraphAnalyzerService;

    public TextProcessingResponse process(int paragraphCount) {
        validateParagraphCount(paragraphCount);
        long startedAt = System.nanoTime();

        List<String> allParagraphs = hipsumParagraphFetcherService.fetchParagraphs(paragraphCount);
        List<ParagraphAnalysis> analyses = paragraphAnalyzerService.analyzeParagraphs(allParagraphs);

        String mostFrequentWord = paragraphAnalyzerService.extractMostFrequentWord(analyses);
        double averageParagraphSize = paragraphAnalyzerService.calculateAverageSize(analyses);
        double averageProcessingTime = paragraphAnalyzerService.calculateAverageProcessingTime(analyses);
        double totalProcessingTime = (System.nanoTime() - startedAt) / NANOS_PER_MILLISECOND;

        return new TextProcessingResponse(mostFrequentWord, averageParagraphSize, averageProcessingTime, totalProcessingTime);
    }

    private void validateParagraphCount(int paragraphCount) {
        if (paragraphCount <= 0) {
            throw new InvalidParagraphCountException();
        }
    }
}
