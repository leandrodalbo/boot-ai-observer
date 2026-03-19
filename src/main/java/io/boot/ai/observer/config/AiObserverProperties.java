package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Root configuration for AI Observer, bound from the "ai.observer" prefix.
 *
 * Minimal required configuration (add to your application.yml):
 *
 *   ai:
 *     observer:
 *       anthropic-api-key: ${ANTHROPIC_API_KEY}
 *       model: claude-haiku-4-5-20251001
 *
 * See application.yml in the starter project for the full reference with all options.
 */
@ConfigurationProperties(prefix = "ai.observer")
public record AiObserverProperties(

        /** Set to false to disable AI Observer without removing the dependency (e.g. in CI). */
        @DefaultValue("true") boolean enabled,

        /**
         * Required. Your Anthropic API key.
         * Always use an environment variable — never hardcode in source.
         *
         *   ai.observer.anthropic-api-key: ${ANTHROPIC_API_KEY}
         */
        String anthropicApiKey,

        /**
         * Required. The Claude model to use for analysis.
         * Must be explicitly set — no default is provided.
         *
         * Recommended options:
         *   claude-haiku-4-5-20251001   — cheapest, fast, good for routine analysis
         *   claude-sonnet-4-6           — more capable, better for complex correlations
         *
         *   ai.observer.model: claude-haiku-4-5-20251001
         */
        String model,

        /**
         * Max tokens allowed in the AI response.
         * 1024 is sufficient for a JSON array of insights.
         *
         *   ai.observer.max-tokens: 1024
         */
        @DefaultValue("1024") int maxTokens,

        /**
         * How often to run scheduled analysis, in seconds.
         * Default: 900 (15 minutes) — conservative to keep API costs low.
         * Thresholds can still trigger early analysis between scheduled runs.
         *
         *   ai.observer.interval-seconds: 300   # 5 minutes
         */
        @DefaultValue("900") long intervalSeconds,

        /** Metric thresholds that trigger early analysis. See ThresholdsProperties. */
        ThresholdsProperties thresholds,

        /** Prompt customization. See PromptProperties. */
        PromptProperties prompt

) {}
