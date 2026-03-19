package io.github.leandrodalbo.aiobserver.model;

public record EndpointStats(double avgLatencyMs, double maxLatencyMs, int requestCount) {}
