package com.betvictor.processing.controller;

import com.betvictor.processing.data.TextProcessingResponse;
import com.betvictor.processing.service.TextProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/betvictor")
@RequiredArgsConstructor
public class TextProcessingController {

    private final TextProcessingService textProcessingService;

    @GetMapping("/text")
    public TextProcessingResponse processText(@RequestParam int p) {
        return textProcessingService.process(p);
    }
}
