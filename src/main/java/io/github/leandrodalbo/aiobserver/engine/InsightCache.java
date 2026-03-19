package io.github.leandrodalbo.aiobserver.engine;

import io.github.leandrodalbo.aiobserver.model.AiInsight;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InsightCache {

    private volatile List<AiInsight> insights = List.of();
    private volatile Instant lastUpdated;
    private final AtomicBoolean analysisInFlight = new AtomicBoolean(false);

    public List<AiInsight> get() {
        return insights;
    }

    public void store(List<AiInsight> newInsights) {
        this.insights = List.copyOf(newInsights);
        this.lastUpdated = Instant.now();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public boolean isInFlight() {
        return analysisInFlight.get();
    }

    public boolean setInFlight(boolean value) {
        if (value) {
            return analysisInFlight.compareAndSet(false, true);
        } else {
            analysisInFlight.set(false);
            return true;
        }
    }
}
