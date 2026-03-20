package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.LatencyTracker;
import io.boot.ai.observer.request.RequestMonitoringFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@ConditionalOnClass({OncePerRequestFilter.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class RequestMonitoring
{

    @Bean
    public RequestMonitoringFilter requestMonitoringFilter(LatencyTracker latencyTracker) {
        return new RequestMonitoringFilter(latencyTracker);
    }
}
