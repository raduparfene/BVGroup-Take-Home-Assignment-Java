package com.betvictor.repository.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextProcessingResponse {
    private String freqWord;
    private double avgParagraphSize;
    private double avgParagraphProcessingTime;
    private double totalProcessingTime;
}
