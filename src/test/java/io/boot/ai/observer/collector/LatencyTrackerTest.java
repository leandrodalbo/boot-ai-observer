package io.boot.ai.observer.collector;

import io.boot.ai.observer.model.EndpointStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LatencyTrackerTest {

    private LatencyTracker tracker =  new LatencyTracker();;

    @Test
    void recordLatency_singleSample_producesCorrectStats() {
        tracker.recordLatency("/api/orders", 100L);

        Map<String, EndpointStats> result = tracker.getAndReset();

        assertThat(result).containsKey("/api/orders");
        EndpointStats stats = result.get("/api/orders");
        assertThat(stats.avgLatencyMs()).isCloseTo(100.0, within(0.01));
        assertThat(stats.maxLatencyMs()).isCloseTo(100.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(1);
    }

    @Test
    void recordLatency_multipleSamples_computesAvgAndMax() {
        tracker.recordLatency("/api/orders", 100L);
        tracker.recordLatency("/api/orders", 200L);
        tracker.recordLatency("/api/orders", 300L);

        EndpointStats stats = tracker.getAndReset().get("/api/orders");

        assertThat(stats.avgLatencyMs()).isCloseTo(200.0, within(0.01));
        assertThat(stats.maxLatencyMs()).isCloseTo(300.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(3);
    }

    @Test
    void recordLatency_multiplePaths_tracksEachPathIndependently() {
        tracker.recordLatency("/api/orders", 100L);
        tracker.recordLatency("/api/users", 50L);

        Map<String, EndpointStats> result = tracker.getAndReset();

        assertThat(result).hasSize(2);
        assertThat(result.get("/api/orders").requestCount()).isEqualTo(1);
        assertThat(result.get("/api/users").requestCount()).isEqualTo(1);
    }

    @Test
    void getAndReset_clearsStore_subsequentCallReturnsEmpty() {
        tracker.recordLatency("/api/orders", 100L);

        tracker.getAndReset();
        Map<String, EndpointStats> second = tracker.getAndReset();

        assertThat(second).isEmpty();
    }

    @Test
    void getAndReset_afterReset_newSamplesAreTracked() {
        tracker.recordLatency("/api/orders", 100L);
        tracker.getAndReset();

        tracker.recordLatency("/api/orders", 999L);
        EndpointStats stats = tracker.getAndReset().get("/api/orders");

        assertThat(stats.avgLatencyMs()).isCloseTo(999.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(1);
    }

    @Test
    void getAndReset_emptyStore_returnsEmptyMap() {
        assertThat(tracker.getAndReset()).isEmpty();
    }

    @Test
    void recordLatency_concurrentWrites_noSamplesLost() throws InterruptedException {
        int threadCount = 10;
        int samplesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < samplesPerThread; j++) {
                    tracker.recordLatency("/api/orders", 100L);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        EndpointStats stats = tracker.getAndReset().get("/api/orders");
        assertThat(stats.requestCount()).isEqualTo(threadCount * samplesPerThread);
    }
}
