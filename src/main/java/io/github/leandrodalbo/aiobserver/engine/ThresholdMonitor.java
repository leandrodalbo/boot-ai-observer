package io.github.leandrodalbo.aiobserver.engine;

import io.github.leandrodalbo.aiobserver.collector.JvmMetricsCollector;
import io.github.leandrodalbo.aiobserver.config.AiObserverProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ThresholdMonitor {

    private static final Logger log = LoggerFactory.getLogger(ThresholdMonitor.class);
    private static final long COOLDOWN_MS = 60_000L;

    private final AiObserverProperties props;
    private final JvmMetricsCollector jvmMetricsCollector;
    private final AiObserverEngine engine;
    private volatile Instant lastTriggered = Instant.EPOCH;

    public ThresholdMonitor(AiObserverProperties props,
                             JvmMetricsCollector jvmMetricsCollector,
                             AiObserverEngine engine) {
        this.props = props;
        this.jvmMetricsCollector = jvmMetricsCollector;
        this.engine = engine;
    }

    @Scheduled(fixedDelay = 30_000)
    public void checkThresholds() {
        if (!props.isEnabled()) return;

        long now = System.currentTimeMillis();
        long lastMs = lastTriggered.toEpochMilli();
        if (now - lastMs < COOLDOWN_MS) return;

        double heap = jvmMetricsCollector.getHeapUsedPercent();
        int threads = jvmMetricsCollector.getThreadCount();

        if (heap > props.getThresholds().getHeapUsagePercent()) {
            log.info("[AI Observer] Heap threshold exceeded ({}%), triggering early analysis", String.format("%.1f", heap));
            lastTriggered = Instant.now();
            engine.triggerEarly("high-heap:" + heap);
        } else if (threads > props.getThresholds().getThreadCount()) {
            log.info("[AI Observer] Thread count threshold exceeded ({}), triggering early analysis", threads);
            lastTriggered = Instant.now();
            engine.triggerEarly("high-threads:" + threads);
        }
    }
}
