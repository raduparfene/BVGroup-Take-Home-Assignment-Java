package com.betvictor.repository.service;

import com.betvictor.repository.configuration.HistoryProperties;
import com.betvictor.repository.data.TextProcessingResponse;
import com.betvictor.repository.data.TextProcessingResultEntity;
import com.betvictor.repository.repository.TextProcessingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextProcessingResultService {

    private final TextProcessingResultRepository textProcessingResultRepository;
    private final HistoryProperties historyProperties;

    @Transactional
    public void save(TextProcessingResponse response) {
        textProcessingResultRepository.save(new TextProcessingResultEntity(response));
    }

    @Transactional(readOnly = true)
    public List<TextProcessingResponse> getLatestResults() {
        List<TextProcessingResultEntity> entities = textProcessingResultRepository.findAllByOrderByIdDesc(Limit.of(historyProperties.getLimit()));
        List<TextProcessingResponse> results = new ArrayList<>();

        for (TextProcessingResultEntity entity : entities) {
            results.add(toResponse(entity));
        }

        return results;
    }

    private TextProcessingResponse toResponse(TextProcessingResultEntity entity) {
        return new TextProcessingResponse(entity.getFreqWord(), entity.getAvgParagraphSize(), entity.getAvgParagraphProcessingTime(), entity.getTotalProcessingTime());
    }
}
