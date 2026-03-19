package io.github.leandrodalbo.aiobserver.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.leandrodalbo.aiobserver.model.EndpointStats;
import io.github.leandrodalbo.aiobserver.model.RuntimeSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class SnapshotSerializer {

    private final ObjectMapper objectMapper;

    public SnapshotSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(RuntimeSnapshot snapshot) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("capturedAt", snapshot.capturedAt().toString());
            node.put("heapUsedPercent", round(snapshot.heapUsedPercent()));
            node.put("heapUsedMb", snapshot.heapUsedMb());
            node.put("heapMaxMb", snapshot.heapMaxMb());
            node.put("threadCount", snapshot.threadCount());
            node.put("daemonThreadCount", snapshot.daemonThreadCount());
            node.put("cpuUsagePercent", round(snapshot.cpuUsagePercent()));
            node.put("gcPauseMs", snapshot.gcPauseMs());
            node.put("gcCollections", snapshot.gcCollections());

            if (!snapshot.endpoints().isEmpty()) {
                ObjectNode endpointsNode = objectMapper.createObjectNode();
                for (Map.Entry<String, EndpointStats> entry : snapshot.endpoints().entrySet()) {
                    EndpointStats stats = entry.getValue();
                    if (stats.requestCount() > 0) {
                        ObjectNode statsNode = objectMapper.createObjectNode();
                        statsNode.put("avgLatencyMs", round(stats.avgLatencyMs()));
                        statsNode.put("maxLatencyMs", round(stats.maxLatencyMs()));
                        statsNode.put("requestCount", stats.requestCount());
                        endpointsNode.set(entry.getKey(), statsNode);
                    }
                }
                if (endpointsNode.size() > 0) {
                    node.set("endpoints", endpointsNode);
                }
            }

            if (!snapshot.recentErrors().isEmpty()) {
                ArrayNode errorsNode = objectMapper.createArrayNode();
                snapshot.recentErrors().forEach(errorsNode::add);
                node.set("recentErrors", errorsNode);
                node.put("errorCount", snapshot.errorCount());
            }

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\":\"serialization failed\"}";
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
