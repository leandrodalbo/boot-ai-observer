package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ai.observer")
public record AiObserverProperties(
        @DefaultValue("true") boolean enabled,
        String anthropicApiKey,
        String model,
        @DefaultValue("1024") int maxTokens,
        @DefaultValue("900") long intervalSeconds,
        ThresholdsProperties thresholds,
        PromptProperties prompt,
        CollectorsProperties collectors
) {}
