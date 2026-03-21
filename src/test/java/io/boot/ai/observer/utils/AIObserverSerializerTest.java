package io.boot.ai.observer.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.boot.ai.observer.model.AIObserverEndpointStats;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AIObserverSerializerTest {

    private final AIObserverSerializer serializer = new AIObserverSerializer(new ObjectMapper());

    @Test
    void serialize_includesCoreJvmFields() throws Exception {
        AIObserverRuntimeSnapshot snapshot = snapshot(Map.of(), List.of());

        JsonNode json = parse(snapshot);

        assertThat(json.get("heapUsedPercent").doubleValue()).isEqualTo(65.5);
        assertThat(json.get("heapUsedMb").longValue()).isEqualTo(512L);
        assertThat(json.get("heapMaxMb").longValue()).isEqualTo(1024L);
        assertThat(json.get("threadCount").intValue()).isEqualTo(42);
        assertThat(json.get("daemonThreadCount").intValue()).isEqualTo(10);
        assertThat(json.get("cpuUsagePercent").doubleValue()).isEqualTo(33.3);
        assertThat(json.get("gcPauseMs").longValue()).isEqualTo(150L);
        assertThat(json.get("gcCollections").longValue()).isEqualTo(3L);
        assertThat(json.get("errorCount").intValue()).isEqualTo(0);
    }

    @Test
    void serialize_roundsDoublesToOneDecimal() throws Exception {
        AIObserverRuntimeSnapshot snapshot = new AIObserverRuntimeSnapshot(
                Instant.now(), 65.555, 512L, 1024L, 42, 10, 33.349, 150L, 3L,
                Map.of(), List.of(), 0
        );

        JsonNode json = parse(snapshot);

        assertThat(json.get("heapUsedPercent").doubleValue()).isEqualTo(65.6);
        assertThat(json.get("cpuUsagePercent").doubleValue()).isEqualTo(33.3);
    }

    @Test
    void serialize_omitsRecentErrors_whenEmpty() throws Exception {
        JsonNode json = parse(snapshot(Map.of(), List.of()));

        assertThat(json.has("recentErrors")).isFalse();
    }

    @Test
    void serialize_includesRecentErrors_whenPresent() throws Exception {
        JsonNode json = parse(snapshot(Map.of(), List.of("NullPointerException: oops")));

        assertThat(json.has("recentErrors")).isTrue();
        assertThat(json.get("recentErrors").get(0).asText()).isEqualTo("NullPointerException: oops");
    }

    @Test
    void serialize_omitsEndpoints_whenEmpty() throws Exception {
        JsonNode json = parse(snapshot(Map.of(), List.of()));

        assertThat(json.has("endpoints")).isFalse();
    }

    @Test
    void serialize_includesEndpoints_whenPresent() throws Exception {
        AIObserverEndpointStats stats = new AIObserverEndpointStats(120.0, 350.0, 5);
        JsonNode json = parse(snapshot(Map.of("/api/orders", stats), List.of()));

        assertThat(json.has("endpoints")).isTrue();
        JsonNode endpoint = json.get("endpoints").get("/api/orders");
        assertThat(endpoint.get("avgLatencyMs").doubleValue()).isEqualTo(120.0);
        assertThat(endpoint.get("maxLatencyMs").doubleValue()).isEqualTo(350.0);
        assertThat(endpoint.get("requestCount").intValue()).isEqualTo(5);
    }

    @Test
    void serialize_roundsEndpointLatencies() throws Exception {
        AIObserverEndpointStats stats = new AIObserverEndpointStats(120.456, 350.789, 5);
        JsonNode json = parse(snapshot(Map.of("/api/orders", stats), List.of()));

        JsonNode endpoint = json.get("endpoints").get("/api/orders");
        assertThat(endpoint.get("avgLatencyMs").doubleValue()).isEqualTo(120.5);
        assertThat(endpoint.get("maxLatencyMs").doubleValue()).isEqualTo(350.8);
    }

    @Test
    void serialize_includesCapturedAt() throws Exception {
        JsonNode json = parse(snapshot(Map.of(), List.of()));

        assertThat(json.has("capturedAt")).isTrue();
        assertThat(json.get("capturedAt").asText()).isNotBlank();
    }

    // --- helpers ---

    private AIObserverRuntimeSnapshot snapshot(Map<String, AIObserverEndpointStats> endpoints, List<String> errors) {
        return new AIObserverRuntimeSnapshot(
                Instant.now(), 65.5, 512L, 1024L, 42, 10, 33.3, 150L, 3L,
                endpoints, errors, errors.size()
        );
    }

    private JsonNode parse(AIObserverRuntimeSnapshot snapshot) throws Exception {
        return new ObjectMapper().readTree(serializer.serializeRuntimeSnapshot(snapshot));
    }
}
