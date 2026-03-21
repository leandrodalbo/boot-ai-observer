package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import io.boot.ai.observer.webfilters.AIObserverReactiveRequestMonitoringFilter;
import io.boot.ai.observer.webfilters.AIObserverServletRequestMonitoringFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

@Configuration
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class AIObserverWebFiltersConfiguration
{


    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    @Bean
    public AIObserverServletRequestMonitoringFilter servletRequestMonitoringFilter(AIObserverWebLatencyCollector latencyCollector) {
        return new AIObserverServletRequestMonitoringFilter(latencyCollector);
    }

    @ConditionalOnClass({WebFilter.class, ServerWebExchange.class})
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    @Bean
    public AIObserverReactiveRequestMonitoringFilter reactiveRequestMonitoringFilter(AIObserverWebLatencyCollector latencyCollector,
                                                                                     AIObserverErrorCollector errorCollector) {
        return new AIObserverReactiveRequestMonitoringFilter(latencyCollector, errorCollector);
    }
}
