package io.boot.ai.observer.aiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.boot.ai.observer.config.AIObserverProperties;
import io.boot.ai.observer.config.AIObserverProvider;
import io.boot.ai.observer.config.AIObserverCollectorsProperties;
import io.boot.ai.observer.config.AIObserverPromptProperties;
import io.boot.ai.observer.config.AIObserverThresholdsProperties;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;
import io.boot.ai.observer.utils.AIObserverPrompts;
import io.boot.ai.observer.utils.AIObserverSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AIObserverCopilotClientTest {

    private static final ObjectMapper MAPPER        = new ObjectMapper();
    private static final String       INTRO         = "You are monitoring a logistics platform.";
    private static final String       EXTRA_CONTEXT = "Flag anything that affects order processing.";


    @Test
    void buildSystemPrompt_withNoIntro_returnsResponseFormatOnly() {
        assertThat(client(null, null).buildSystemPrompt())
                .isEqualTo(AIObserverPrompts.RESPONSE_FORMAT.text());
    }

    @Test
    void buildSystemPrompt_withBlankIntro_returnsResponseFormatOnly() {
        assertThat(client("   ", null).buildSystemPrompt())
                .isEqualTo(AIObserverPrompts.RESPONSE_FORMAT.text());
    }

    @Test
    void buildSystemPrompt_withIntro_prependsIntroBeforeResponseFormat() {
        String result = client(INTRO, null).buildSystemPrompt();

        assertThat(result).startsWith(INTRO);
        assertThat(result).contains(AIObserverPrompts.RESPONSE_FORMAT.text());
    }

    @Test
    void buildSystemPrompt_intro_isTrimmed() {
        assertThat(client("  " + INTRO + "  ", null).buildSystemPrompt())
                .startsWith(INTRO);
    }

    @Test
    void buildUserMessage_containsSnapshotJson() {
        String result = client(null, null).buildUserMessage(emptySnapshot());

        assertThat(result).contains("heapUsedMb");
        assertThat(result).doesNotContain("{snapshot}");
    }

    @Test
    void buildUserMessage_withExtraContext_appendsAfterSnapshot() {
        String result = client(null, EXTRA_CONTEXT).buildUserMessage(emptySnapshot());

        assertThat(result).contains("heapUsedMb");
        assertThat(result).endsWith(EXTRA_CONTEXT);
    }


    @Test
    void buildRequestBody_containsModelAndMaxTokens() throws Exception {
        JsonNode body = MAPPER.readTree(client(null, null).buildRequestBody(emptySnapshot()));

        assertThat(body.get("model").asText()).isEqualTo("gpt-4o");
        assertThat(body.get("max_tokens").asInt()).isEqualTo(1024);
    }

    @Test
    void buildRequestBody_firstMessageIsSystemRole() throws Exception {
        JsonNode body   = MAPPER.readTree(client(null, null).buildRequestBody(emptySnapshot()));
        JsonNode sysMsg = body.get("messages").get(0);

        assertThat(sysMsg.get("role").asText()).isEqualTo("system");
        assertThat(sysMsg.get("content").asText()).contains(AIObserverPrompts.RESPONSE_FORMAT.text());
    }

    @Test
    void buildRequestBody_secondMessageIsUserRole() throws Exception {
        JsonNode body    = MAPPER.readTree(client(null, null).buildRequestBody(emptySnapshot()));
        JsonNode userMsg = body.get("messages").get(1);

        assertThat(userMsg.get("role").asText()).isEqualTo("user");
        assertThat(userMsg.get("content").asText()).contains("heapUsedMb");
    }


    @Test
    void extractText_extractsContentFromCopilotEnvelope() throws Exception {
        String response = """
                {"choices":[{"index":0,"message":{"role":"assistant","content":"Thread count is within normal range."},"finish_reason":"stop"}]}
                """;

        assertThat(client(null, null).extractText(response)).isEqualTo("Thread count is within normal range.");
    }

    @Test
    void extractText_missingChoicesField_returnsEmptyString() throws Exception {
        String response = """
                {"model":"gpt-4o"}
                """;

        assertThat(client(null, null).extractText(response)).isEmpty();
    }

    @Test
    void extractText_emptyChoicesArray_returnsEmptyString() throws Exception {
        String response = """
                {"choices":[]}
                """;

        assertThat(client(null, null).extractText(response)).isEmpty();
    }

    private AIObserverCopilotAiClient client(String intro, String extraContext) {
        AIObserverProperties props = new AIObserverProperties(
                true,
                AIObserverProvider.COPILOT,
                "test-github-token",
                null,
                null,
                null,
                1024,
                900L,
                new AIObserverThresholdsProperties(80.0, 200, 5.0),
                new AIObserverPromptProperties(intro, extraContext),
                new AIObserverCollectorsProperties(true, true, true)
        );
        return new AIObserverCopilotAiClient(MAPPER, new AIObserverSerializer(MAPPER), props);
    }

    private AIObserverRuntimeSnapshot emptySnapshot() {
        return new AIObserverRuntimeSnapshot(
                Instant.now(), 0.0, 0L, 0L, 0, 0, 0.0, 0L, 0L,
                Map.of(), List.of(), 0
        );
    }
}
