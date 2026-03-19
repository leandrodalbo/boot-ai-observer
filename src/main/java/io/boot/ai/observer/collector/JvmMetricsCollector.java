package io.boot.ai.observer.collector;

import io.boot.ai.observer.utils.MicrometerMetric;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static io.boot.ai.observer.utils.MicrometerMetric.*;

@Component
@ConditionalOnBean(MeterRegistry.class)
public class JvmMetricsCollector
{
    private static final double BYTES_PER_MB        = 1024.0 * 1024.0;
    private static final double FRACTION_TO_PERCENT = 100.0;

    private final MeterRegistry registry;

    public JvmMetricsCollector(MeterRegistry registry)
    {
        this.registry = registry;
    }

    public double heapUsedPercent()
    {
        double used = heapUsedBytes();
        double max  = heapMaxBytes();
        if (max <= 0) return 0.0;
        return (used / max) * FRACTION_TO_PERCENT;
    }

    public long heapUsedMb()
    {
        return (long) (heapUsedBytes() / BYTES_PER_MB);
    }

    public long heapMaxMb()
    {
        return (long) (heapMaxBytes() / BYTES_PER_MB);
    }

    public int threadCount()
    {
        return (int) gauge(THREADS_LIVE);
    }

    public int daemonThreadCount()
    {
        return (int) gauge(THREADS_DAEMON);
    }

    public double cpuUsagePercent()
    {
        return gauge(CPU_USAGE) * FRACTION_TO_PERCENT;
    }

    public long totalGcPauseMs()
    {
        Timer gcTimer = registry.find(GC_PAUSE.key).timer();
        if (gcTimer == null) return 0L;
        return (long) gcTimer.totalTime(TimeUnit.MILLISECONDS);
    }

    public long totalGcCollections()
    {
        Timer gcTimer = registry.find(GC_PAUSE.key).timer();
        if (gcTimer == null) return 0L;
        return gcTimer.count();
    }

    double heapUsedBytes()
    {
        var gauge = registry.find(HEAP_USED.key).tag(TAG_AREA, TAG_AREA_HEAP).gauge();
        return gauge == null ? 0.0 : gauge.value();
    }

    double heapMaxBytes()
    {
        var gauge = registry.find(HEAP_MAX.key).tag(TAG_AREA, TAG_AREA_HEAP).gauge();
        return gauge == null ? 0.0 : gauge.value();
    }

    double gauge(MicrometerMetric metric)
    {
        var gauge = registry.find(metric.key).gauge();
        return gauge == null ? 0.0 : gauge.value();
    }
}
