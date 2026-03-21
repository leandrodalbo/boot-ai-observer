package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.error.AIObserverNonWebErrorCollector;
import io.boot.ai.observer.collector.error.AIObserverReactiveErrorCollector;
import io.boot.ai.observer.collector.error.AIObserverServletErrorCollector;
import io.boot.ai.observer.collector.jvm.AIObserverMicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class AIObserverCollectorConfiguration
{

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    public AIObserverWebLatencyCollector servletLatencyCollector() {
        return new AIObserverWebLatencyCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = REACTIVE)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "latency-enabled", matchIfMissing = true)
    public AIObserverWebLatencyCollector reactiveLatencyCollector() {
        return new AIObserverWebLatencyCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "errors-enabled", matchIfMissing = true)
    public AIObserverErrorCollector servletErrorCollector() {
        return new AIObserverServletErrorCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = REACTIVE)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "errors-enabled", matchIfMissing = true)
    public AIObserverErrorCollector reactiveErrorCollector() {
        return new AIObserverReactiveErrorCollector();
    }

    @Bean
    @ConditionalOnMissingBean(AIObserverErrorCollector.class)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "errors-enabled", matchIfMissing = true)
    public AIObserverErrorCollector nonWebErrorCollector() {
        return new AIObserverNonWebErrorCollector();
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "ai.observer.collectors", name = "jvm-enabled", matchIfMissing = true)
    public AIObserverMicrometerJvmCollector micrometerJvmCollector(MeterRegistry registry) {
        return new AIObserverMicrometerJvmCollector(registry);
    }
}
