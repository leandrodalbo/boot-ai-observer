package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ai.observer")
public record AIObserverProperties(
        @DefaultValue("true") boolean enabled,
        AIObserverProvider aiProvider,
        String aiApiKey,
        String aiModel,
        String aiApiUrl,
        String aiApiVersion,
        @DefaultValue("1024") int maxTokens,
        @DefaultValue("900") long intervalSeconds,
        AIObserverThresholdsProperties thresholds,
        AIObserverPromptProperties prompt,
        AIObserverCollectorsProperties collectors

) {}
