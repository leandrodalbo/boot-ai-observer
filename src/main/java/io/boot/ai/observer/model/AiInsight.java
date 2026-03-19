package io.boot.ai.observer.model;

import java.time.Instant;

/**
 * A single insight returned by the AI after analyzing a RuntimeSnapshot.
 * This is the public-facing model — exposed at /actuator/ai-insights.
 */
public record AiInsight(
        String level,
        String message,
        String recommendation,
        Instant generatedAt

) {}
