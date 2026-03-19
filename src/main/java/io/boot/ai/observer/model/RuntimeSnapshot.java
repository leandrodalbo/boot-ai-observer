package io.boot.ai.observer.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A point-in-time snapshot of the application's runtime state.
 * Built by SnapshotService and serialized to JSON before being sent to Claude.
 * Never exposed directly to users — AiInsight is what they see at /actuator/ai-insights.
 */
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
