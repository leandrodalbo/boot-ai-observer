package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.error.ErrorCollector;
import io.boot.ai.observer.collector.latency.WebLatencyCollector;
import io.boot.ai.observer.webfilters.ReactiveRequestMonitoringFilter;
import io.boot.ai.observer.webfilters.ServletRequestMonitoringFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebFiltersConfiguration
{


    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    @Bean
    public ServletRequestMonitoringFilter servletRequestMonitoringFilter(WebLatencyCollector latencyCollector) {
        return new ServletRequestMonitoringFilter(latencyCollector);
    }

    @ConditionalOnClass({WebFilter.class, ServerWebExchange.class})
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    @Bean
    public ReactiveRequestMonitoringFilter reactiveRequestMonitoringFilter(WebLatencyCollector latencyCollector,
                                                                           ErrorCollector errorCollector) {
        return new ReactiveRequestMonitoringFilter(latencyCollector, errorCollector);
    }
}
