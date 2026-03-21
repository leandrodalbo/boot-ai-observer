package io.boot.ai.observer.snapshot;

import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.jvm.AIObserverMicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import io.boot.ai.observer.model.AIObserverEndpointStats;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AIObserverSnapshotService
{

    private final Optional<AIObserverMicrometerJvmCollector> jvmCollector;
    private final Optional<AIObserverWebLatencyCollector> latencyCollector;
    private final Optional<AIObserverErrorCollector> errorCollector;

    public AIObserverSnapshotService(Optional<AIObserverMicrometerJvmCollector> jvmCollector,
                                     Optional<AIObserverWebLatencyCollector> latencyCollector,
                                     Optional<AIObserverErrorCollector> errorCollector) {
        this.jvmCollector = jvmCollector;
        this.latencyCollector = latencyCollector;
        this.errorCollector = errorCollector;
    }

    public AIObserverRuntimeSnapshot build() {
        List<String> errors = errorCollector.map(AIObserverErrorCollector::getAndReset).orElse(List.of());
        Map<String, AIObserverEndpointStats> endpoints = latencyCollector.map(AIObserverWebLatencyCollector::getAndReset).orElse(Map.of());

        return new AIObserverRuntimeSnapshot(
                Instant.now(),
                jvmCollector.map(AIObserverMicrometerJvmCollector::heapUsedPercent).orElse(0.0),
                jvmCollector.map(AIObserverMicrometerJvmCollector::heapUsedMb).orElse(0L),
                jvmCollector.map(AIObserverMicrometerJvmCollector::heapMaxMb).orElse(0L),
                jvmCollector.map(AIObserverMicrometerJvmCollector::threadCount).orElse(0),
                jvmCollector.map(AIObserverMicrometerJvmCollector::daemonThreadCount).orElse(0),
                jvmCollector.map(AIObserverMicrometerJvmCollector::cpuUsagePercent).orElse(0.0),
                jvmCollector.map(AIObserverMicrometerJvmCollector::totalGcPauseMs).orElse(0L),
                jvmCollector.map(AIObserverMicrometerJvmCollector::totalGcCollections).orElse(0L),
                endpoints,
                errors,
                errors.size()
        );
    }
}
