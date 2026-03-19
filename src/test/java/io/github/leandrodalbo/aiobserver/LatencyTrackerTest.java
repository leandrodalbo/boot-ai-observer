package io.github.leandrodalbo.aiobserver;

import io.github.leandrodalbo.aiobserver.collector.LatencyTracker;
import io.github.leandrodalbo.aiobserver.model.EndpointStats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LatencyTrackerTest {

    @Test
    void recordsAndResets() {
        LatencyTracker tracker = new LatencyTracker();
        tracker.recordLatency("/api/test", 100L);
        tracker.recordLatency("/api/test", 200L);
        tracker.recordLatency("/api/other", 50L);

        Map<String, EndpointStats> stats = tracker.getAndReset();
        assertThat(stats).containsKey("/api/test");
        assertThat(stats.get("/api/test").requestCount()).isEqualTo(2);
        assertThat(stats.get("/api/test").avgLatencyMs()).isEqualTo(150.0);
        assertThat(stats.get("/api/test").maxLatencyMs()).isEqualTo(200.0);

        Map<String, EndpointStats> afterReset = tracker.getAndReset();
        assertThat(afterReset).isEmpty();
    }
}
