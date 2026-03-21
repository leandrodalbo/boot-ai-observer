package io.boot.ai.observer.collector.latency;

import io.boot.ai.observer.model.AIObserverEndpointStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AIObserverWebLatencyCollectorTest {

    private AIObserverWebLatencyCollector collector;

    @BeforeEach
    void setUp() {
        collector = new AIObserverWebLatencyCollector();
    }

    @Test
    void record_singleSample_producesCorrectStats() {
        collector.record("/api/orders", 100L);

        AIObserverEndpointStats stats = collector.getAndReset().get("/api/orders");

        assertThat(stats.avgLatencyMs()).isCloseTo(100.0, within(0.01));
        assertThat(stats.maxLatencyMs()).isCloseTo(100.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(1);
    }

    @Test
    void record_multipleSamples_computesAvgAndMax() {
        collector.record("/api/orders", 100L);
        collector.record("/api/orders", 200L);
        collector.record("/api/orders", 300L);

        AIObserverEndpointStats stats = collector.getAndReset().get("/api/orders");

        assertThat(stats.avgLatencyMs()).isCloseTo(200.0, within(0.01));
        assertThat(stats.maxLatencyMs()).isCloseTo(300.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(3);
    }

    @Test
    void record_multiplePaths_tracksEachPathIndependently() {
        collector.record("/api/orders", 100L);
        collector.record("/api/users", 50L);

        Map<String, AIObserverEndpointStats> result = collector.getAndReset();

        assertThat(result).hasSize(2);
        assertThat(result.get("/api/orders").requestCount()).isEqualTo(1);
        assertThat(result.get("/api/users").requestCount()).isEqualTo(1);
    }

    @Test
    void getAndReset_clearsStore_subsequentCallReturnsEmpty() {
        collector.record("/api/orders", 100L);

        collector.getAndReset();

        assertThat(collector.getAndReset()).isEmpty();
    }

    @Test
    void getAndReset_afterReset_newSamplesAreTracked() {
        collector.record("/api/orders", 100L);
        collector.getAndReset();

        collector.record("/api/orders", 999L);
        AIObserverEndpointStats stats = collector.getAndReset().get("/api/orders");

        assertThat(stats.avgLatencyMs()).isCloseTo(999.0, within(0.01));
        assertThat(stats.requestCount()).isEqualTo(1);
    }

    @Test
    void getAndReset_emptyStore_returnsEmptyMap() {
        assertThat(collector.getAndReset()).isEmpty();
    }

    @Test
    void record_concurrentWrites_noSamplesLost() throws InterruptedException {
        int threadCount      = 10;
        int samplesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < samplesPerThread; j++) {
                    collector.record("/api/orders", 100L);
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        AIObserverEndpointStats stats = collector.getAndReset().get("/api/orders");
        assertThat(stats.requestCount()).isEqualTo(threadCount * samplesPerThread);
    }
}
