package io.github.leandrodalbo.aiobserver.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(AiObserverProperties.class)
@EnableScheduling
@EnableAspectJAutoProxy
@ComponentScan("io.github.leandrodalbo.aiobserver")
public class AiObserverAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiObserverAutoConfiguration.class);

    private final AiObserverProperties props;

    public AiObserverAutoConfiguration(AiObserverProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void validate() {
        if (props.getAnthropicApiKey() == null || props.getAnthropicApiKey().isBlank()) {
            log.warn("[AI Observer] anthropic-api-key is not set. Add ai.observer.anthropic-api-key to your config. AI Observer will be inactive.");
            props.setEnabled(false);
            return;
        }
        log.info("[AI Observer] Started. Model: {} | Interval: {}s | Endpoint: /actuator/ai-insights",
                props.getModel(), props.getIntervalSeconds());
    }
}
