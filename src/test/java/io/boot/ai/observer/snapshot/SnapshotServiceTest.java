package io.boot.ai.observer.snapshot;

import io.boot.ai.observer.collector.error.ErrorCollector;
import io.boot.ai.observer.collector.jvm.MicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.WebLatencyCollector;
import io.boot.ai.observer.model.EndpointStats;
import io.boot.ai.observer.model.RuntimeSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock MicrometerJvmCollector jvmCollector;
    @Mock WebLatencyCollector    latencyCollector;
    @Mock ErrorCollector         errorCollector;

    @Test
    void build_capturedAt_isNotNull() {
        stubEmptyCollectors();
        assertThat(service().build().capturedAt()).isNotNull();
    }

    @Test
    void build_jvmMetrics_arePopulatedFromJvmCollector() {
        when(jvmCollector.heapUsedPercent()).thenReturn(65.5);
        when(jvmCollector.heapUsedMb()).thenReturn(512L);
        when(jvmCollector.heapMaxMb()).thenReturn(1024L);
        when(jvmCollector.threadCount()).thenReturn(42);
        when(jvmCollector.daemonThreadCount()).thenReturn(10);
        when(jvmCollector.cpuUsagePercent()).thenReturn(33.3);
        when(jvmCollector.totalGcPauseMs()).thenReturn(150L);
        when(jvmCollector.totalGcCollections()).thenReturn(3L);
        when(latencyCollector.getAndReset()).thenReturn(Map.of());
        when(errorCollector.getAndReset()).thenReturn(List.of());

        RuntimeSnapshot snapshot = service().build();

        assertThat(snapshot.heapUsedPercent()).isCloseTo(65.5, within(0.01));
        assertThat(snapshot.heapUsedMb()).isEqualTo(512L);
        assertThat(snapshot.heapMaxMb()).isEqualTo(1024L);
        assertThat(snapshot.threadCount()).isEqualTo(42);
        assertThat(snapshot.daemonThreadCount()).isEqualTo(10);
        assertThat(snapshot.cpuUsagePercent()).isCloseTo(33.3, within(0.01));
        assertThat(snapshot.gcPauseMs()).isEqualTo(150L);
        assertThat(snapshot.gcCollections()).isEqualTo(3L);
    }

    @Test
    void build_endpoints_arePopulatedFromLatencyCollector() {
        stubEmptyJvmMetrics();
        when(errorCollector.getAndReset()).thenReturn(List.of());

        EndpointStats stats = new EndpointStats(120.0, 350.0, 5);
        when(latencyCollector.getAndReset()).thenReturn(Map.of("/api/orders", stats));

        RuntimeSnapshot snapshot = service().build();

        assertThat(snapshot.endpoints()).containsKey("/api/orders");
        assertThat(snapshot.endpoints().get("/api/orders")).isEqualTo(stats);
    }

    @Test
    void build_errors_arePopulatedFromErrorCollector() {
        stubEmptyJvmMetrics();
        when(latencyCollector.getAndReset()).thenReturn(Map.of());
        when(errorCollector.getAndReset()).thenReturn(List.of("NullPointerException: oops", "TimeoutException: too slow"));

        RuntimeSnapshot snapshot = service().build();

        assertThat(snapshot.recentErrors()).containsExactly("NullPointerException: oops", "TimeoutException: too slow");
        assertThat(snapshot.errorCount()).isEqualTo(2);
    }

    @Test
    void build_errorCount_matchesErrorListSize() {
        stubEmptyJvmMetrics();
        when(latencyCollector.getAndReset()).thenReturn(Map.of());
        when(errorCollector.getAndReset()).thenReturn(List.of("Ex1", "Ex2", "Ex3"));

        RuntimeSnapshot snapshot = service().build();

        assertThat(snapshot.errorCount()).isEqualTo(snapshot.recentErrors().size());
    }

    @Test
    void build_withoutJvmCollector_jvmFieldsAreZero() {
        when(latencyCollector.getAndReset()).thenReturn(Map.of());
        when(errorCollector.getAndReset()).thenReturn(List.of());

        RuntimeSnapshot snapshot = serviceWithout(false, true, true).build();

        assertThat(snapshot.heapUsedPercent()).isZero();
        assertThat(snapshot.heapUsedMb()).isZero();
        assertThat(snapshot.heapMaxMb()).isZero();
        assertThat(snapshot.threadCount()).isZero();
        assertThat(snapshot.daemonThreadCount()).isZero();
        assertThat(snapshot.cpuUsagePercent()).isZero();
        assertThat(snapshot.gcPauseMs()).isZero();
        assertThat(snapshot.gcCollections()).isZero();
    }

    @Test
    void build_withoutLatencyCollector_endpointsAreEmpty() {
        stubEmptyJvmMetrics();
        when(errorCollector.getAndReset()).thenReturn(List.of());

        RuntimeSnapshot snapshot = serviceWithout(true, false, true).build();

        assertThat(snapshot.endpoints()).isEmpty();
    }

    @Test
    void build_withoutErrorCollector_errorsAreEmptyAndCountIsZero() {
        stubEmptyJvmMetrics();
        when(latencyCollector.getAndReset()).thenReturn(Map.of());

        RuntimeSnapshot snapshot = serviceWithout(true, true, false).build();

        assertThat(snapshot.recentErrors()).isEmpty();
        assertThat(snapshot.errorCount()).isZero();
    }

    @Test
    void build_withNoCollectors_snapshotIsStillBuilt() {
        RuntimeSnapshot snapshot = serviceWithout(false, false, false).build();

        assertThat(snapshot.capturedAt()).isNotNull();
        assertThat(snapshot.endpoints()).isEmpty();
        assertThat(snapshot.recentErrors()).isEmpty();
        assertThat(snapshot.errorCount()).isZero();
    }

    private SnapshotService service() {
        return serviceWithout(true, true, true);
    }

    private SnapshotService serviceWithout(boolean jvm, boolean latency, boolean errors) {
        return new SnapshotService(
                jvm     ? Optional.of(jvmCollector)     : Optional.empty(),
                latency ? Optional.of(latencyCollector) : Optional.empty(),
                errors  ? Optional.of(errorCollector)   : Optional.empty()
        );
    }

    private void stubEmptyCollectors() {
        stubEmptyJvmMetrics();
        when(latencyCollector.getAndReset()).thenReturn(Map.of());
        when(errorCollector.getAndReset()).thenReturn(List.of());
    }

    private void stubEmptyJvmMetrics() {
        when(jvmCollector.heapUsedPercent()).thenReturn(0.0);
        when(jvmCollector.heapUsedMb()).thenReturn(0L);
        when(jvmCollector.heapMaxMb()).thenReturn(0L);
        when(jvmCollector.threadCount()).thenReturn(0);
        when(jvmCollector.daemonThreadCount()).thenReturn(0);
        when(jvmCollector.cpuUsagePercent()).thenReturn(0.0);
        when(jvmCollector.totalGcPauseMs()).thenReturn(0L);
        when(jvmCollector.totalGcCollections()).thenReturn(0L);
    }
}
