package io.github.leandrodalbo.aiobserver.collector;

import io.github.leandrodalbo.aiobserver.model.EndpointStats;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LatencyTracker {

    private final ConcurrentHashMap<String, List<Long>> latencySamples = new ConcurrentHashMap<>();

    public void recordLatency(String path, long millis) {
        latencySamples.computeIfAbsent(path, k -> new CopyOnWriteArrayList<>()).add(millis);
    }

    public Map<String, EndpointStats> getAndReset() {
        Map<String, List<Long>> snapshot = new HashMap<>();
        latencySamples.forEach((key, value) -> snapshot.put(key, latencySamples.remove(key)));

        Map<String, EndpointStats> result = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : snapshot.entrySet()) {
            List<Long> samples = entry.getValue();
            if (samples.isEmpty()) continue;
            double avg = samples.stream().mapToLong(Long::longValue).average().orElse(0.0);
            long max = samples.stream().mapToLong(Long::longValue).max().orElse(0L);
            result.put(entry.getKey(), new EndpointStats(avg, max, samples.size()));
        }
        return result;
    }
}
