package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record ThresholdsProperties(
        @DefaultValue("80.0") double heapUsagePercent,
        @DefaultValue("200") int threadCount,
        @DefaultValue("5.0") double errorRatePercent
) {}
