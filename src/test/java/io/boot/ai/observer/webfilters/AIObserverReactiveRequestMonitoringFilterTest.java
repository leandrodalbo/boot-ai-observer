package io.boot.ai.observer.webfilters;

import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AIObserverReactiveRequestMonitoringFilterTest {

    @Mock
    AIObserverWebLatencyCollector latencyCollector;
    @Mock
    AIObserverErrorCollector errorCollector;
    @Mock WebFilterChain   chain;

    private AIObserverReactiveRequestMonitoringFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AIObserverReactiveRequestMonitoringFilter(latencyCollector, errorCollector);
    }

    @Test
    void filter_regularPath_recordsLatency() {
        MockServerWebExchange exchange = exchangeFor("/api/orders");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(latencyCollector).record(eq("/api/orders"), anyLong());
    }

    @Test
    void filter_actuatorPath_skipsLatencyAndErrorTracking() {
        MockServerWebExchange exchange = exchangeFor("/actuator/health");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verifyNoInteractions(latencyCollector, errorCollector);
    }

    @Test
    void filter_chainError_tracksErrorAndRecordsLatency() {
        MockServerWebExchange exchange = exchangeFor("/api/orders");
        RuntimeException cause = new RuntimeException("upstream failure");
        when(chain.filter(exchange)).thenReturn(Mono.error(cause));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyError(RuntimeException.class);

        verify(errorCollector).track(cause);
        verify(latencyCollector).record(eq("/api/orders"), anyLong());
    }

    @Test
    void filter_chainError_doesNotSwallowException() {
        MockServerWebExchange exchange = exchangeFor("/api/orders");
        when(chain.filter(exchange)).thenReturn(Mono.error(new IllegalStateException("boom")));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyError(IllegalStateException.class);
    }

    private static MockServerWebExchange exchangeFor(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }
}
