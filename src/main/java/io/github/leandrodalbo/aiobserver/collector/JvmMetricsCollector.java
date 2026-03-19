package io.github.leandrodalbo.aiobserver.collector;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnBean(MeterRegistry.class)
public class JvmMetricsCollector {

    private final MeterRegistry registry;

    public JvmMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    public double getHeapUsedPercent() {
        double used = getGaugeValue("jvm.memory.used", "heap");
        double max = getGaugeValue("jvm.memory.max", "heap");
        if (max <= 0) return 0.0;
        return (used / max) * 100.0;
    }

    public long getHeapUsedMb() {
        return (long) (getGaugeValue("jvm.memory.used", "heap") / (1024 * 1024));
    }

    public long getHeapMaxMb() {
        return (long) (getGaugeValue("jvm.memory.max", "heap") / (1024 * 1024));
    }

    public int getThreadCount() {
        return (int) getGaugeValue("jvm.threads.live", null);
    }

    public int getDaemonThreadCount() {
        return (int) getGaugeValue("jvm.threads.daemon", null);
    }

    public double getCpuUsage() {
        return getGaugeValue("process.cpu.usage", null) * 100.0;
    }

    public long getGcPauseMs() {
        var search = registry.find("jvm.gc.pause");
        if (search == null) return 0L;
        return search.timers().stream()
                .mapToLong(t -> (long) t.totalTime(TimeUnit.MILLISECONDS))
                .sum();
    }

    public long getGcCollections() {
        var search = registry.find("jvm.gc.pause");
        if (search == null) return 0L;
        return search.timers().stream()
                .mapToLong(t -> t.count())
                .sum();
    }

    private double getGaugeValue(String name, String areaTag) {
        var search = registry.find(name);
        if (search == null) return 0.0;
        if (areaTag != null) {
            search = search.tag("area", areaTag);
        }
        var gauges = search.gauges();
        return gauges.stream().mapToDouble(Gauge::value).sum();
    }
}
