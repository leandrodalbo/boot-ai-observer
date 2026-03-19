package io.github.leandrodalbo.aiobserver.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leandrodalbo.aiobserver.config.AiObserverProperties;
import io.github.leandrodalbo.aiobserver.model.AiInsight;
import io.github.leandrodalbo.aiobserver.model.RuntimeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ClaudeApiClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeApiClient.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SnapshotSerializer snapshotSerializer;

    public ClaudeApiClient(ObjectMapper objectMapper, SnapshotSerializer snapshotSerializer) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.snapshotSerializer = snapshotSerializer;
    }

    public CompletableFuture<List<AiInsight>> analyze(RuntimeSnapshot snapshot,
                                                       AiObserverProperties props) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String snapshotJson = snapshotSerializer.serialize(snapshot);
                String userContent = buildUserContent(snapshotJson, props);
                String systemPrompt = buildSystemPrompt(props);
                String requestBody = buildRequestBody(props, systemPrompt, userContent);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("x-api-key", props.getAnthropicApiKey())
                        .header("anthropic-version", ANTHROPIC_VERSION)
                        .header("content-type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.warn("[AI Observer] Claude API returned status {}", response.statusCode());
                    return List.of();
                }

                return parseInsights(response.body());
            } catch (Exception e) {
                log.warn("[AI Observer] Failed to call Claude API: {}", e.getMessage());
                return List.of();
            }
        });
    }

    private String buildSystemPrompt(AiObserverProperties props) {
        String base = props.getPrompt().getSystemPrompt();
        String extra = props.getPrompt().getExtraContext();
        if (extra != null && !extra.isBlank()) {
            return base + "\n\nApplication context:\n" + extra;
        }
        return base;
    }

    private String buildUserContent(String snapshotJson, AiObserverProperties props) {
        String template = props.getPrompt().getUserTemplate();
        return template.replace("{snapshot}", snapshotJson);
    }

    private String buildRequestBody(AiObserverProperties props, String systemPrompt, String userContent) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", props.getModel());
            root.put("max_tokens", props.getMaxTokens());
            root.put("system", systemPrompt);
            var messages = objectMapper.createArrayNode();
            var userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userContent);
            messages.add(userMsg);
            root.set("messages", messages);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.warn("[AI Observer] Failed to build request body: {}", e.getMessage());
            return "{}";
        }
    }

    private List<AiInsight> parseInsights(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("content").path(0).path("text").asText();

            String jsonText = extractJsonArray(text);
            JsonNode insightsArray = objectMapper.readTree(jsonText);

            List<AiInsight> insights = new ArrayList<>();
            Instant now = Instant.now();
            for (JsonNode node : insightsArray) {
                insights.add(new AiInsight(
                        node.path("level").asText("INFO"),
                        node.path("message").asText(""),
                        node.path("recommendation").asText(""),
                        now
                ));
            }
            return insights;
        } catch (Exception e) {
            log.warn("[AI Observer] Failed to parse Claude response: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractJsonArray(String text) {
        if (text == null) return "[]";
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "[]";
    }
}
