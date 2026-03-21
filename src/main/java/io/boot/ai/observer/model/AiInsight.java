package io.boot.ai.observer.model;

import java.time.Instant;

public record AiInsight(
        String level,
        String message,
        String recommendation,
        Instant generatedAt
) {}
