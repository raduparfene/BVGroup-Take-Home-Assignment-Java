package com.betvictor.repository.controller;

import com.betvictor.repository.data.TextProcessingResponse;
import com.betvictor.repository.service.TextProcessingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/betvictor")
@RequiredArgsConstructor
public class HistoryController {

    private final TextProcessingResultService textProcessingResultService;

    @GetMapping("/history")
    public List<TextProcessingResponse> getHistory() {
        return textProcessingResultService.getLatestResults();
    }
}
