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

public class AIObserverOpenAiClient extends AIObserverClient
{

    private static final String DEFAULT_URL   = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private static final String HEADER_AUTH = "Authorization";
    private static final String AUTH_PREFIX = "Bearer ";
    private static final String ROLE_SYSTEM = "system";
    private static final String RES_CHOICES = "choices";
    private static final String RES_MESSAGE = "message";
    private static final String RES_CONTENT = "content";

    public AIObserverOpenAiClient(ObjectMapper mapper, AIObserverSerializer serializer, AIObserverProperties props) {
        super(mapper, serializer, props);
    }

    @Override
    protected HttpRequest buildRequest(AIObserverRuntimeSnapshot snapshot) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(Objects.requireNonNullElse(props.aiApiUrl(), DEFAULT_URL)))
                .timeout(REQUEST_TIMEOUT)
                .header(HEADER_AUTH,                                 AUTH_PREFIX + props.aiApiKey())
                .header(AIObserverClientConstants.HEADER_CONTENT_TYPE.value(), AIObserverClientConstants.HEADER_JSON.value())
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(snapshot)))
                .build();
    }

    String buildRequestBody(AIObserverRuntimeSnapshot snapshot) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put(AIObserverClientConstants.REQ_MODEL.value(), Objects.requireNonNullElse(props.aiModel(), DEFAULT_MODEL));
        root.put(AIObserverClientConstants.REQ_MAX_TOKENS.value(), props.maxTokens());

        ArrayNode messages = root.putArray(AIObserverClientConstants.REQ_MESSAGES.value());

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put(AIObserverClientConstants.REQ_ROLE.value(), ROLE_SYSTEM);
        systemMsg.put(AIObserverClientConstants.REQ_CONTENT.value(), buildSystemPrompt());

        ObjectNode userMsg = messages.addObject();
        userMsg.put(AIObserverClientConstants.REQ_ROLE.value(), AIObserverClientConstants.REQ_ROLE_USER.value());
        userMsg.put(AIObserverClientConstants.REQ_CONTENT.value(), buildUserMessage(snapshot));

        return mapper.writeValueAsString(root);
    }

    @Override
    protected String extractText(String responseBody) throws Exception {
        return mapper.readTree(responseBody).path(RES_CHOICES).path(0).path(RES_MESSAGE).path(RES_CONTENT).asText();
    }
}
