package io.boot.ai.observer.autoconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.boot.ai.observer.aiclient.AIObserverClient;
import io.boot.ai.observer.aiclient.AIObserverClaudeClient;
import io.boot.ai.observer.aiclient.AIObserverCopilotAiClient;
import io.boot.ai.observer.aiclient.AIObserverOpenAiClient;
import io.boot.ai.observer.config.AIObserverProperties;
import io.boot.ai.observer.utils.AIObserverSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class AIObserverClientConfiguration
{

    @Bean
    @ConditionalOnMissingBean
    public AIObserverClient aiClient(AIObserverProperties props, ObjectMapper mapper, AIObserverSerializer serializer) {
        if (props.aiProvider() == null) {
            throw new IllegalStateException(
                    "[AI Observer] 'ai.observer.ai-provider' is required. " +
                    "Accepted values: claude, openai, copilot");
        }
        return switch (props.aiProvider()) {
            case CLAUDE  -> new AIObserverClaudeClient(mapper, serializer, props);
            case OPENAI  -> new AIObserverOpenAiClient(mapper, serializer, props);
            case COPILOT -> new AIObserverCopilotAiClient(mapper, serializer, props);
        };
    }
}
