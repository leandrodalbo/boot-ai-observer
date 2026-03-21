package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record AIObserverCollectorsProperties(
        @DefaultValue("true") boolean jvmEnabled,
        @DefaultValue("true") boolean latencyEnabled,
        @DefaultValue("true") boolean errorsEnabled
) {}
