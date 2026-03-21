package io.boot.ai.observer.aiclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.boot.ai.observer.config.AIObserverProperties;
import io.boot.ai.observer.model.AIObserverInsight;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;
import io.boot.ai.observer.utils.AIObserverPrompts;
import io.boot.ai.observer.utils.AIObserverClientConstants;
import io.boot.ai.observer.utils.AIObserverLog;
import io.boot.ai.observer.utils.AIObserverSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public abstract class AIObserverClient
{

    protected final Logger               log = LoggerFactory.getLogger(getClass());
    protected final HttpClient           httpClient;
    protected final ObjectMapper         mapper;
    protected final AIObserverSerializer serializer;
    protected final AIObserverProperties props;

    private static final String   FAILED_TEXT      = "AI analysis unavailable";
    protected static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    protected AIObserverClient(ObjectMapper mapper, AIObserverSerializer serializer, AIObserverProperties props) {
        this.mapper     = mapper;
        this.serializer = serializer;
        this.props      = props;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public CompletableFuture<AIObserverInsight> analyze(AIObserverRuntimeSnapshot snapshot) {
        if (props.aiApiKey() == null || props.aiApiKey().isBlank()) {
            AIObserverLog.STARTUP_NO_API_KEY.warn(log);
            return CompletableFuture.completedFuture(new AIObserverInsight(FAILED_TEXT, Instant.now()));
        }
        try {
            return httpClient.sendAsync(buildRequest(snapshot), HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::parseResponse)
                    .exceptionally(ex -> {
                        AIObserverLog.API_CALL_FAILED.warn(log, ex.getMessage());
                        return new AIObserverInsight(FAILED_TEXT, Instant.now());
                    });
        } catch (Exception ex) {
            AIObserverLog.API_REQUEST_BUILD_FAILED.warn(log, ex.getMessage());
            return CompletableFuture.completedFuture(new AIObserverInsight(FAILED_TEXT, Instant.now()));
        }
    }

    protected String buildSystemPrompt() {
        String intro = props.prompt().intro();
        if (intro != null && !intro.isBlank()) {
            return intro.strip() + "\n\n" + AIObserverPrompts.RESPONSE_FORMAT.text();
        }
        return AIObserverPrompts.RESPONSE_FORMAT.text();
    }

    protected String buildUserMessage(AIObserverRuntimeSnapshot snapshot) {
        String base  = AIObserverPrompts.SNAPSHOT_TEMPLATE.text()
                .replace(AIObserverClientConstants.SNAPSHOT_PLACEHOLDER.value(), serializer.serializeRuntimeSnapshot(snapshot));
        String extra = props.prompt().extraContext();
        if (extra != null && !extra.isBlank()) {
            return base + "\n\n" + extra.strip();
        }
        return base;
    }

    protected AIObserverInsight parseResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            AIObserverLog.API_HTTP_ERROR.warn(log, response.statusCode(), response.body());
            return new AIObserverInsight(FAILED_TEXT, Instant.now());
        }
        try {
            String text = extractText(response.body());
            if (text.isBlank()) return new AIObserverInsight(FAILED_TEXT, Instant.now());
            return new AIObserverInsight(text, Instant.now());
        } catch (Exception ex) {
            AIObserverLog.API_RESPONSE_PARSE_FAILED.warn(log, ex.getMessage());
            return new AIObserverInsight(FAILED_TEXT, Instant.now());
        }
    }

    protected abstract HttpRequest buildRequest(AIObserverRuntimeSnapshot snapshot) throws Exception;

    protected abstract String extractText(String responseBody) throws Exception;
}
