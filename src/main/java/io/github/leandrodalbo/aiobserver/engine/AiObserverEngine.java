package io.github.leandrodalbo.aiobserver.engine;

import io.github.leandrodalbo.aiobserver.client.ClaudeApiClient;
import io.github.leandrodalbo.aiobserver.collector.SnapshotService;
import io.github.leandrodalbo.aiobserver.config.AiObserverProperties;
import io.github.leandrodalbo.aiobserver.model.RuntimeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AiObserverEngine {

    private static final Logger log = LoggerFactory.getLogger(AiObserverEngine.class);

    private final AiObserverProperties props;
    private final InsightCache cache;
    private final SnapshotService snapshotService;
    private final ClaudeApiClient claudeClient;

    public AiObserverEngine(AiObserverProperties props,
                             InsightCache cache,
                             SnapshotService snapshotService,
                             ClaudeApiClient claudeClient) {
        this.props = props;
        this.cache = cache;
        this.snapshotService = snapshotService;
        this.claudeClient = claudeClient;
    }

    @Scheduled(fixedDelayString = "${ai.observer.interval-seconds:300}000")
    public void scheduledAnalysis() {
        runAnalysisIfAppropriate("scheduled");
    }

    public void triggerEarly(String reason) {
        runAnalysisIfAppropriate("threshold:" + reason);
    }

    private void runAnalysisIfAppropriate(String trigger) {
        if (!props.isEnabled()) return;
        if (!cache.setInFlight(true)) return;

        RuntimeSnapshot snapshot = snapshotService.build();

        claudeClient.analyze(snapshot, props)
                .whenComplete((insights, err) -> {
                    cache.setInFlight(false);
                    if (err != null) {
                        log.warn("[AI Observer] Analysis failed (trigger={}): {}", trigger, err.getMessage());
                    } else if (insights != null && !insights.isEmpty()) {
                        cache.store(insights);
                        log.info("[AI Observer] {} insights generated (trigger={})", insights.size(), trigger);
                    }
                });
    }
}
