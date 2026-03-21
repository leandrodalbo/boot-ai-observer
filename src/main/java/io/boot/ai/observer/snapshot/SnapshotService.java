package io.boot.ai.observer.snapshot;

import io.boot.ai.observer.collector.error.ErrorCollector;
import io.boot.ai.observer.collector.jvm.MicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.WebLatencyCollector;
import io.boot.ai.observer.model.EndpointStats;
import io.boot.ai.observer.model.RuntimeSnapshot;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {

    private final Optional<MicrometerJvmCollector> jvmCollector;
    private final Optional<WebLatencyCollector> latencyCollector;
    private final Optional<ErrorCollector> errorCollector;

    public SnapshotService(Optional<MicrometerJvmCollector> jvmCollector,
                           Optional<WebLatencyCollector> latencyCollector,
                           Optional<ErrorCollector> errorCollector) {
        this.jvmCollector = jvmCollector;
        this.latencyCollector = latencyCollector;
        this.errorCollector = errorCollector;
    }

    public RuntimeSnapshot build() {
        List<String> errors = errorCollector.map(ErrorCollector::getAndReset).orElse(List.of());
        Map<String, EndpointStats> endpoints = latencyCollector.map(WebLatencyCollector::getAndReset).orElse(Map.of());

        return new RuntimeSnapshot(
                Instant.now(),
                jvmCollector.map(MicrometerJvmCollector::heapUsedPercent).orElse(0.0),
                jvmCollector.map(MicrometerJvmCollector::heapUsedMb).orElse(0L),
                jvmCollector.map(MicrometerJvmCollector::heapMaxMb).orElse(0L),
                jvmCollector.map(MicrometerJvmCollector::threadCount).orElse(0),
                jvmCollector.map(MicrometerJvmCollector::daemonThreadCount).orElse(0),
                jvmCollector.map(MicrometerJvmCollector::cpuUsagePercent).orElse(0.0),
                jvmCollector.map(MicrometerJvmCollector::totalGcPauseMs).orElse(0L),
                jvmCollector.map(MicrometerJvmCollector::totalGcCollections).orElse(0L),
                endpoints,
                errors,
                errors.size()
        );
    }
}
