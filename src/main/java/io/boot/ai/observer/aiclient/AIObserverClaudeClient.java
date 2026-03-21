package io.boot.ai.observer.aiclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.boot.ai.observer.config.AIObserverProperties;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;
import io.boot.ai.observer.utils.AIObserverClientConstants;
import io.boot.ai.observer.utils.AIObserverSerializer;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Objects;

public class AIObserverClaudeClient extends AIObserverClient
{

    private static final String DEFAULT_URL     = "https://api.anthropic.com/v1/messages";
    private static final String DEFAULT_MODEL   = "claude-haiku-4-5-20251001";
    private static final String DEFAULT_VERSION = "2023-06-01";

    private static final String HEADER_API_KEY = "x-api-key";
    private static final String HEADER_VERSION = "anthropic-version";
    private static final String REQ_SYSTEM     = "system";
    private static final String RES_CONTENT    = "content";
    private static final String RES_TEXT       = "text";

    public AIObserverClaudeClient(ObjectMapper mapper, AIObserverSerializer serializer, AIObserverProperties props) {
        super(mapper, serializer, props);
    }

    @Override
    protected HttpRequest buildRequest(AIObserverRuntimeSnapshot snapshot) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(Objects.requireNonNullElse(props.aiApiUrl(), DEFAULT_URL)))
                .header(HEADER_API_KEY,                              props.aiApiKey())
                .header(HEADER_VERSION,                              Objects.requireNonNullElse(props.aiApiVersion(), DEFAULT_VERSION))
                .header(AIObserverClientConstants.HEADER_CONTENT_TYPE.value(), AIObserverClientConstants.HEADER_JSON.value())
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(snapshot)))
                .build();
    }

    String buildRequestBody(AIObserverRuntimeSnapshot snapshot) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put(AIObserverClientConstants.REQ_MODEL.value(), Objects.requireNonNullElse(props.aiModel(), DEFAULT_MODEL));
        root.put(AIObserverClientConstants.REQ_MAX_TOKENS.value(), props.maxTokens());
        root.put(REQ_SYSTEM,                             buildSystemPrompt());

        ArrayNode  messages = root.putArray(AIObserverClientConstants.REQ_MESSAGES.value());
        ObjectNode userMsg  = messages.addObject();
        userMsg.put(AIObserverClientConstants.REQ_ROLE.value(), AIObserverClientConstants.REQ_ROLE_USER.value());
        userMsg.put(AIObserverClientConstants.REQ_CONTENT.value(), buildUserMessage(snapshot));

        return mapper.writeValueAsString(root);
    }

    @Override
    protected String extractText(String responseBody) throws Exception {
        return mapper.readTree(responseBody).path(RES_CONTENT).path(0).path(RES_TEXT).asText();
    }
}
