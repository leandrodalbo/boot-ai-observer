package io.boot.ai.observer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.boot.ai.observer.model.AIObserverEndpointStats;
import io.boot.ai.observer.model.AIObserverRuntimeSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class AIObserverSerializer
{

    private static final int DECIMAL_SCALE = 1;

    private final ObjectMapper mapper;

    public AIObserverSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String serializeRuntimeSnapshot(AIObserverRuntimeSnapshot snapshot) {
        try {
            ObjectNode root = mapper.createObjectNode();

            root.put(AIObserverClientConstants.SNAP_CAPTURED_AT.value(), snapshot.capturedAt().toString());
            root.put(AIObserverClientConstants.SNAP_HEAP_USED_PERCENT.value(), round(snapshot.heapUsedPercent()));
            root.put(AIObserverClientConstants.SNAP_HEAP_USED_MB.value(), snapshot.heapUsedMb());
            root.put(AIObserverClientConstants.SNAP_HEAP_MAX_MB.value(), snapshot.heapMaxMb());
            root.put(AIObserverClientConstants.SNAP_THREAD_COUNT.value(), snapshot.threadCount());
            root.put(AIObserverClientConstants.SNAP_DAEMON_THREADS.value(), snapshot.daemonThreadCount());
            root.put(AIObserverClientConstants.SNAP_CPU_USAGE.value(), round(snapshot.cpuUsagePercent()));
            root.put(AIObserverClientConstants.SNAP_GC_PAUSE_MS.value(), snapshot.gcPauseMs());
            root.put(AIObserverClientConstants.SNAP_GC_COLLECTIONS.value(), snapshot.gcCollections());
            root.put(AIObserverClientConstants.SNAP_ERROR_COUNT.value(), snapshot.errorCount());

            if (!snapshot.recentErrors().isEmpty()) {
                ArrayNode errorsNode = root.putArray(AIObserverClientConstants.SNAP_RECENT_ERRORS.value());
                snapshot.recentErrors().forEach(errorsNode::add);
            }

            if (!snapshot.endpoints().isEmpty()) {
                ObjectNode endpointsNode = root.putObject(AIObserverClientConstants.SNAP_ENDPOINTS.value());
                for (Map.Entry<String, AIObserverEndpointStats> entry : snapshot.endpoints().entrySet()) {
                    AIObserverEndpointStats stats    = entry.getValue();
                    ObjectNode    statsNode = endpointsNode.putObject(entry.getKey());
                    statsNode.put(AIObserverClientConstants.ENDPOINT_AVG_LATENCY.value(), round(stats.avgLatencyMs()));
                    statsNode.put(AIObserverClientConstants.ENDPOINT_MAX_LATENCY.value(), round(stats.maxLatencyMs()));
                    statsNode.put(AIObserverClientConstants.ENDPOINT_REQUEST_COUNT.value(), stats.requestCount());
                }
            }

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize RuntimeSnapshot", e);
        }
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
