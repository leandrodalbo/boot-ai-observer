package io.boot.ai.observer.config;

/**
 * Prompt configuration for AI analysis calls.
 * All fields are optional — unset fields fall back to the starter's bundled defaults
 * loaded from classpath resources at startup.
 *
 * Minimal useful config — just describe your app:
 *
 *   ai:
 *     observer:
 *       prompt:
 *         extra-context: |
 *           This is a payment processing service handling ~500 req/min.
 *           It uses PostgreSQL and Redis. Peak traffic is 9am–6pm UTC.
 *
 * Full override example:
 *
 *   ai:
 *     observer:
 *       prompt:
 *         system-prompt: |
 *           You are a JVM expert for a fintech application.
 *           Focus on latency outliers and GC pauses above 50ms.
 *           Return JSON only: [{"level":"...","message":"...","recommendation":"..."}]
 *         user-template: |
 *           Current state of our service:
 *           {snapshot}
 *           Flag anything that could affect transaction success rates.
 *         extra-context: |
 *           Peak load: 9am–6pm UTC. PostgreSQL + Redis. ~500 req/min.
 */
public record PromptProperties(

        /**
         * Overrides the system prompt sent to the AI model.
         * If not set, the bundled default (ai-observer/default-system-prompt.txt) is used.
         */
        String systemPrompt,

        /**
         * Overrides the user message template. Use {snapshot} as the placeholder
         * for the serialized runtime snapshot JSON.
         * If not set, the bundled default (ai-observer/default-user-template.txt) is used.
         */
        String userTemplate,

        /**
         * Free-text description of your application, injected into every AI call.
         * This is the easiest way to get specific, actionable insights instead of
         * generic JVM advice. Leave empty if not needed.
         */
        String extraContext

) {}
