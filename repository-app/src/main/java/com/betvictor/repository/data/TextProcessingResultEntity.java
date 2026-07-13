package com.betvictor.repository.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_text_results")
@Getter
@NoArgsConstructor
public class TextProcessingResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String freqWord;

    @Column(nullable = false)
    private double avgParagraphSize;

    @Column(nullable = false)
    private double avgParagraphProcessingTime;

    @Column(nullable = false)
    private double totalProcessingTime;

    public TextProcessingResultEntity(TextProcessingResponse response) {
        this.freqWord = response.getFreqWord();
        this.avgParagraphSize = response.getAvgParagraphSize();
        this.avgParagraphProcessingTime = response.getAvgParagraphProcessingTime();
        this.totalProcessingTime = response.getTotalProcessingTime();
    }
}
