package io.boot.ai.observer.collector;

import io.boot.ai.observer.utils.MicrometerMetric;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JvmMetricsCollectorTest
{
    private static final double MB             = 1024.0 * 1024.0;
    private static final double HEAP_USED_MB   = 512;
    private static final double HEAP_MAX_MB    = 1024;
    private static final double CPU_FRACTION   = 0.75;
    private static final int    THREAD_COUNT   = 42;
    private static final int    DAEMON_COUNT   = 10;
    private static final double GC_PAUSE_MS    = 175.0;
    private static final long   GC_COLLECTIONS = 3L;

    @Mock
    MeterRegistry registry;

    @InjectMocks
    JvmMetricsCollector collector;

    @Test
    void heapUsedPercent_returnsPercentageOfMaxHeap()
    {
        stubHeapUsed(HEAP_USED_MB * MB);
        stubHeapMax(HEAP_MAX_MB * MB);

        assertThat(collector.heapUsedPercent()).isCloseTo(50.0, within(0.01));
    }

    @Test
    void heapUsedMb_convertsFromBytes()
    {
        stubHeapUsed(HEAP_USED_MB * MB);

        assertThat(collector.heapUsedMb()).isEqualTo((long) HEAP_USED_MB);
    }

    @Test
    void heapMaxMb_convertsFromBytes()
    {
        stubHeapMax(HEAP_MAX_MB * MB);

        assertThat(collector.heapMaxMb()).isEqualTo((long) HEAP_MAX_MB);
    }

    @Test
    void threadCount_returnsLiveThreadCount()
    {
        stubGauge(MicrometerMetric.THREADS_LIVE, THREAD_COUNT);

        assertThat(collector.threadCount()).isEqualTo(THREAD_COUNT);
    }

    @Test
    void daemonThreadCount_returnsDaemonThreadCount()
    {
        stubGauge(MicrometerMetric.THREADS_DAEMON, DAEMON_COUNT);

        assertThat(collector.daemonThreadCount()).isEqualTo(DAEMON_COUNT);
    }

    @Test
    void cpuUsagePercent_convertsFractionToPercent()
    {
        stubGauge(MicrometerMetric.CPU_USAGE, CPU_FRACTION);

        assertThat(collector.cpuUsagePercent()).isCloseTo(75.0, within(0.01));
    }


    @Test
    void totalGcPauseMs_returnsTotalPauseTime()
    {
        stubGcPauseMs(GC_PAUSE_MS);

        assertThat(collector.totalGcPauseMs()).isEqualTo((long) GC_PAUSE_MS);
    }

    @Test
    void totalGcCollections_returnsPauseEventCount()
    {
        stubGcCollections(GC_COLLECTIONS);

        assertThat(collector.totalGcCollections()).isEqualTo(GC_COLLECTIONS);
    }


    private void stubHeapUsed(double bytes)
    {
        Search search = mock(Search.class);
        Gauge  gauge  = mock(Gauge.class);
        when(registry.find(MicrometerMetric.HEAP_USED.key)).thenReturn(search);
        when(search.tag(MicrometerMetric.TAG_AREA, MicrometerMetric.TAG_AREA_HEAP)).thenReturn(search);
        when(search.gauge()).thenReturn(gauge);
        when(gauge.value()).thenReturn(bytes);
    }

    private void stubHeapMax(double bytes)
    {
        Search search = mock(Search.class);
        Gauge  gauge  = mock(Gauge.class);
        when(registry.find(MicrometerMetric.HEAP_MAX.key)).thenReturn(search);
        when(search.tag(MicrometerMetric.TAG_AREA, MicrometerMetric.TAG_AREA_HEAP)).thenReturn(search);
        when(search.gauge()).thenReturn(gauge);
        when(gauge.value()).thenReturn(bytes);
    }

    private void stubGauge(MicrometerMetric metric, double value)
    {
        Search search = mock(Search.class);
        Gauge  gauge  = mock(Gauge.class);
        when(registry.find(metric.key)).thenReturn(search);
        when(search.gauge()).thenReturn(gauge);
        when(gauge.value()).thenReturn(value);
    }

    private void stubGcPauseMs(double totalMs)
    {
        Search search = mock(Search.class);
        Timer  timer  = mock(Timer.class);
        when(registry.find(MicrometerMetric.GC_PAUSE.key)).thenReturn(search);
        when(search.timer()).thenReturn(timer);
        when(timer.totalTime(TimeUnit.MILLISECONDS)).thenReturn(totalMs);
    }

    private void stubGcCollections(long count)
    {
        Search search = mock(Search.class);
        Timer  timer  = mock(Timer.class);
        when(registry.find(MicrometerMetric.GC_PAUSE.key)).thenReturn(search);
        when(search.timer()).thenReturn(timer);
        when(timer.count()).thenReturn(count);
    }
}
