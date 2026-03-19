package io.github.leandrodalbo.aiobserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leandrodalbo.aiobserver.client.SnapshotSerializer;
import io.github.leandrodalbo.aiobserver.model.EndpointStats;
import io.github.leandrodalbo.aiobserver.model.RuntimeSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotSerializerTest {

    private final SnapshotSerializer serializer = new SnapshotSerializer(new ObjectMapper());

    @Test
    void serializesBasicSnapshot() {
        RuntimeSnapshot snapshot = new RuntimeSnapshot(
                Instant.now(), 50.0, 512L, 1024L, 100, 80,
                25.0, 10L, 5L, Map.of(), List.of(), 0
        );

        String json = serializer.serialize(snapshot);
        assertThat(json).contains("heapUsedPercent");
        assertThat(json).contains("threadCount");
        assertThat(json).doesNotContain("endpoints");
        assertThat(json).doesNotContain("recentErrors");
    }

    @Test
    void includesEndpointsWhenPresent() {
        RuntimeSnapshot snapshot = new RuntimeSnapshot(
                Instant.now(), 50.0, 512L, 1024L, 100, 80,
                25.0, 10L, 5L,
                Map.of("/api/test", new EndpointStats(100.0, 200.0, 5)),
                List.of(), 0
        );

        String json = serializer.serialize(snapshot);
        assertThat(json).contains("endpoints");
        assertThat(json).contains("/api/test");
    }
}
