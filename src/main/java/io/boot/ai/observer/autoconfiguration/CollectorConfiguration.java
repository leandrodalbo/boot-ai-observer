package io.boot.ai.observer.autoconfiguration;

import io.boot.ai.observer.collector.error.ErrorCollector;
import io.boot.ai.observer.collector.error.NonWebErrorCollector;
import io.boot.ai.observer.collector.error.ReactiveErrorCollector;
import io.boot.ai.observer.collector.error.ServletErrorCollector;
import io.boot.ai.observer.collector.jvm.MicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.WebLatencyCollector;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
public class CollectorConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    public WebLatencyCollector servletLatencyCollector() {
        return new WebLatencyCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = REACTIVE)
    public WebLatencyCollector reactiveLatencyCollector() {
        return new WebLatencyCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    public ErrorCollector servletErrorCollector() {
        return new ServletErrorCollector();
    }

    @Bean
    @ConditionalOnWebApplication(type = REACTIVE)
    public ErrorCollector reactiveErrorCollector() {
        return new ReactiveErrorCollector();
    }

    @Bean
    @ConditionalOnMissingBean(ErrorCollector.class)
    public ErrorCollector nonWebErrorCollector() {
        return new NonWebErrorCollector();
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MicrometerJvmCollector micrometerJvmCollector(MeterRegistry registry) {
        return new MicrometerJvmCollector(registry);
    }
}
