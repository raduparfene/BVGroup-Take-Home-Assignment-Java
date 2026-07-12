package com.betvictor.processing.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TextProcessingResponse {
    private final String freqWord;
    private final double avgParagraphSize;
    private final double avgParagraphProcessingTime;
    private final double totalProcessingTime;
}
