package io.github.leandrodalbo.aiobserver.collector;

import io.github.leandrodalbo.aiobserver.model.RuntimeSnapshot;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SnapshotService {

    private final JvmMetricsCollector jvmMetricsCollector;
    private final LatencyTracker latencyTracker;
    private final ErrorTracker errorTracker;

    public SnapshotService(JvmMetricsCollector jvmMetricsCollector,
                           LatencyTracker latencyTracker,
                           ErrorTracker errorTracker) {
        this.jvmMetricsCollector = jvmMetricsCollector;
        this.latencyTracker = latencyTracker;
        this.errorTracker = errorTracker;
    }

    public RuntimeSnapshot build() {
        var errors = errorTracker.getAndReset();
        var endpoints = latencyTracker.getAndReset();

        return new RuntimeSnapshot(
                Instant.now(),
                jvmMetricsCollector.getHeapUsedPercent(),
                jvmMetricsCollector.getHeapUsedMb(),
                jvmMetricsCollector.getHeapMaxMb(),
                jvmMetricsCollector.getThreadCount(),
                jvmMetricsCollector.getDaemonThreadCount(),
                jvmMetricsCollector.getCpuUsage(),
                jvmMetricsCollector.getGcPauseMs(),
                jvmMetricsCollector.getGcCollections(),
                endpoints,
                errors,
                errors.size()
        );
    }
}
