package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.LatencyTracker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

@Configuration
@ConditionalOnClass({WebFilter.class, ServerWebExchange.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class ReactiveRequestMonitoring
{

    private static final String ACTUATOR_PREFIX = "/actuator";

    @Bean
    public WebFilter requestMonitoringFilter(LatencyTracker latencyTracker) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            if (path.startsWith(ACTUATOR_PREFIX)) {
                return chain.filter(exchange);
            }

            long start = System.currentTimeMillis();
            return chain.filter(exchange)
                    .doFinally(signal ->
                            latencyTracker.recordLatency(path, System.currentTimeMillis() - start)
                    );
        };
    }
}
