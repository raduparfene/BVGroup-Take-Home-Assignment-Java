package com.betvictor.processing.service;

import com.betvictor.processing.data.ParagraphAnalysis;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParagraphAnalyzerService {

    private static final double NANOS_PER_MILLISECOND = 1_000_000.0;
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");

    public List<ParagraphAnalysis> analyzeParagraphs(List<String> paragraphs) {
        List<ParagraphAnalysis> analyses = new ArrayList<>(paragraphs.size());
        for (String paragraph : paragraphs) {
            analyses.add(analyze(paragraph));
        }
        return analyses;
    }

    public String extractMostFrequentWord(List<ParagraphAnalysis> analyses) {
        Map<String, Long> totalFrequencies = calculateTotalFrequencies(analyses);
        return findMostFrequentWord(totalFrequencies);
    }

    public double calculateAverageSize(List<ParagraphAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return 0.0;
        }

        long totalWordCount = 0;
        for (ParagraphAnalysis analysis : analyses) {
            totalWordCount += analysis.getWordCount();
        }
        return (double) totalWordCount / analyses.size();
    }

    public double calculateAverageProcessingTime(List<ParagraphAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return 0.0;
        }

        long totalProcessingTimeNanos = 0;
        for (ParagraphAnalysis analysis : analyses) {
            totalProcessingTimeNanos += analysis.getProcessingTimeNanos();
        }
        return (double) totalProcessingTimeNanos / analyses.size() / NANOS_PER_MILLISECOND;
    }

    private Map<String, Long> calculateTotalFrequencies(List<ParagraphAnalysis> analyses) {
        Map<String, Long> totalFrequencies = new HashMap<>();
        for (ParagraphAnalysis analysis : analyses) {
            for (Map.Entry<String, Long> frequency : analysis.getWordFrequencies().entrySet()) {
                String word = frequency.getKey();
                long total = totalFrequencies.getOrDefault(word, 0L) + frequency.getValue();
                totalFrequencies.put(word, total);
            }
        }
        return totalFrequencies;
    }

    private String findMostFrequentWord(Map<String, Long> frequencies) {
        String mostFrequentWord = null;
        long highestFrequency = 0;

        for (Map.Entry<String, Long> frequency : frequencies.entrySet()) {
            if (shouldReplaceMostFrequentWord(frequency, mostFrequentWord, highestFrequency)) {
                mostFrequentWord = frequency.getKey();
                highestFrequency = frequency.getValue();
            }
        }

        if (mostFrequentWord == null) {
            throw new IllegalStateException("Hipsum paragraphs contained no words");
        }
        return mostFrequentWord;
    }

    private boolean shouldReplaceMostFrequentWord(Map.Entry<String, Long> candidate, String currentWord, long currentFrequency) {
        if (currentWord == null || candidate.getValue() > currentFrequency) {
            return true;
        }
        return candidate.getValue() == currentFrequency
                && candidate.getKey().compareTo(currentWord) < 0;
    }

    private ParagraphAnalysis analyze(String paragraph) {
        long startedAt = System.nanoTime();
        Map<String, Long> frequencies = new HashMap<>();
        int wordCount = 0;

        Matcher matcher = WORD_PATTERN.matcher(paragraph);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            long frequency = frequencies.getOrDefault(word, 0L) + 1;
            frequencies.put(word, frequency);
            ++wordCount;
        }

        long processingTimeNanos = System.nanoTime() - startedAt;
        return new ParagraphAnalysis(frequencies, wordCount, processingTimeNanos);
    }
}
