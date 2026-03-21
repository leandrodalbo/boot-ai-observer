package io.boot.ai.observer.model;

public record AIObserverEndpointStats(
        double avgLatencyMs,
        double maxLatencyMs,
        int requestCount
) {}
