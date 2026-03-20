package io.boot.ai.observer.collector;

import io.boot.ai.observer.model.EndpointStats;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class LatencyTracker {

    private final AtomicReference<ConcurrentHashMap<String, List<Long>>> store =
            new AtomicReference<>(new ConcurrentHashMap<>());

    public void recordLatency(String path, long millis) {
        store.get()
             .computeIfAbsent(path, k -> new CopyOnWriteArrayList<>())
             .add(millis);
    }

    public Map<String, EndpointStats> getAndReset() {
        Map<String, List<Long>> snapshot = store.getAndSet(new ConcurrentHashMap<>());

        return snapshot.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<Long> samples = e.getValue();
                            double avg = samples.stream().mapToLong(Long::longValue).average().orElse(0.0);
                            double max = samples.stream().mapToLong(Long::longValue).max().orElse(0);
                            return new EndpointStats(avg, max, samples.size());
                        }
                ));
    }
}
