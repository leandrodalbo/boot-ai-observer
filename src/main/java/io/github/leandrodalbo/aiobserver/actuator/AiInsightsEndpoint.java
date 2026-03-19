package io.github.leandrodalbo.aiobserver.actuator;

import io.github.leandrodalbo.aiobserver.config.AiObserverProperties;
import io.github.leandrodalbo.aiobserver.engine.InsightCache;
import io.github.leandrodalbo.aiobserver.model.AiInsight;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Endpoint(id = "ai-insights")
@Component
public class AiInsightsEndpoint {

    private final InsightCache cache;
    private final AiObserverProperties props;

    public AiInsightsEndpoint(InsightCache cache, AiObserverProperties props) {
        this.cache = cache;
        this.props = props;
    }

    @ReadOperation
    public InsightsResponse insights() {
        return new InsightsResponse(
                cache.get(),
                cache.getLastUpdated(),
                props.isEnabled()
        );
    }

    public record InsightsResponse(
            List<AiInsight> insights,
            Instant lastUpdated,
            boolean enabled
    ) {}
}
