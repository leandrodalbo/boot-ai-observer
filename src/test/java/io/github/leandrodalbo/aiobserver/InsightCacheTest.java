package io.github.leandrodalbo.aiobserver;

import io.github.leandrodalbo.aiobserver.engine.InsightCache;
import io.github.leandrodalbo.aiobserver.model.AiInsight;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InsightCacheTest {

    @Test
    void storeAndGet() {
        InsightCache cache = new InsightCache();
        assertThat(cache.get()).isEmpty();
        assertThat(cache.isInFlight()).isFalse();

        List<AiInsight> insights = List.of(
                new AiInsight("INFO", "All good", "Nothing to do", Instant.now())
        );
        cache.store(insights);
        assertThat(cache.get()).hasSize(1);
        assertThat(cache.getLastUpdated()).isNotNull();
    }

    @Test
    void inFlightFlag() {
        InsightCache cache = new InsightCache();
        assertThat(cache.setInFlight(true)).isTrue();
        assertThat(cache.isInFlight()).isTrue();
        assertThat(cache.setInFlight(true)).isFalse();
        cache.setInFlight(false);
        assertThat(cache.isInFlight()).isFalse();
    }
}
