package io.boot.ai.observer.config;

public record PromptProperties(
        String systemPrompt,
        String userTemplate,
        String extraContext
) {}
