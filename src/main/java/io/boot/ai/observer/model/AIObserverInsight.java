package io.boot.ai.observer.model;

import java.time.Instant;

public record AIObserverInsight(
        String  text,
        Instant generatedAt
) {}
