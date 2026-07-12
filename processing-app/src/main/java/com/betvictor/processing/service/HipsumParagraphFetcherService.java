package com.betvictor.processing.service;

import com.betvictor.processing.client.HipsumClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class HipsumParagraphFetcherService {

    private static final int PARAGRAPHS_PER_REQUEST = 1;

    private final HipsumClient hipsumClient;
    private final ExecutorService hipsumExecutor;

    public List<String> fetchParagraphs(int requestCount) {
        List<CompletableFuture<List<String>>> pendingRequests = new ArrayList<>(requestCount);

        for (int requestNumber = 0; requestNumber < requestCount; ++requestNumber) {
            pendingRequests.add(CompletableFuture.supplyAsync(
                    () -> hipsumClient.fetchParagraphs(PARAGRAPHS_PER_REQUEST),
                    hipsumExecutor
            ));
        }

        List<String> paragraphs = new ArrayList<>(requestCount);
        for (CompletableFuture<List<String>> pendingRequest : pendingRequests) {
            paragraphs.addAll(pendingRequest.join());
        }
        return paragraphs;
    }
}
