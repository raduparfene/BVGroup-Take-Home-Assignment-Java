package com.betvictor.processing.data;

import lombok.Getter;

import java.util.Map;

@Getter
public class ParagraphAnalysis {
    private final Map<String, Long> wordFrequencies;
    private final int wordCount;
    private final long processingTimeNanos;

    public ParagraphAnalysis(Map<String, Long> wordFrequencies, int wordCount, long processingTimeNanos) {
        this.wordFrequencies = Map.copyOf(wordFrequencies);
        this.wordCount = wordCount;
        this.processingTimeNanos = processingTimeNanos;
    }
}
