package io.boot.ai.observer.model;

public record EndpointStats(
        double avgLatencyMs,
        double maxLatencyMs,
        int requestCount
) {}
