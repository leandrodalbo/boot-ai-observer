package io.github.leandrodalbo.aiobserver.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RuntimeSnapshot(
        Instant capturedAt,
        double heapUsedPercent,
        long heapUsedMb,
        long heapMaxMb,
        int threadCount,
        int daemonThreadCount,
        double cpuUsagePercent,
        long gcPauseMs,
        long gcCollections,
        Map<String, EndpointStats> endpoints,
        List<String> recentErrors,
        int errorCount
) {}
