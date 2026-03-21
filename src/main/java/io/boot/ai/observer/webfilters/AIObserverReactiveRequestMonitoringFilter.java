package io.boot.ai.observer.webfilters;

import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class AIObserverReactiveRequestMonitoringFilter implements WebFilter {

    private static final String ACTUATOR_PREFIX = "/actuator";

    private final AIObserverWebLatencyCollector latencyCollector;
    private final AIObserverErrorCollector errorCollector;

    public AIObserverReactiveRequestMonitoringFilter(AIObserverWebLatencyCollector latencyCollector,
                                                     AIObserverErrorCollector errorCollector) {
        this.latencyCollector = latencyCollector;
        this.errorCollector   = errorCollector;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith(ACTUATOR_PREFIX)) {
            return chain.filter(exchange);
        }

        long start = System.currentTimeMillis();
        return chain.filter(exchange)
                .doOnError(errorCollector::track)
                .doFinally(signal -> latencyCollector.record(path, System.currentTimeMillis() - start));
    }
}
