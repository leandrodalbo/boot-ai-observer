package io.boot.ai.observer.model;

/**
 * Latency statistics for a single HTTP endpoint, collected over one observation window.
 * avg + max together distinguish outlier spikes from general slowness:
 * low avg + high max = occasional slow requests; both high = systemic problem.
 */
public record EndpointStats(
        double avgLatencyMs,
        double maxLatencyMs,
        int requestCount
) {}
