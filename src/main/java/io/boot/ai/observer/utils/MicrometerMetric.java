package io.boot.ai.observer.utils;

public enum MicrometerMetric
{
    HEAP_USED("jvm.memory.used"),
    HEAP_MAX("jvm.memory.max"),
    THREADS_LIVE("jvm.threads.live"),
    THREADS_DAEMON("jvm.threads.daemon"),
    CPU_USAGE("process.cpu.usage"),
    GC_PAUSE("jvm.gc.pause");

    public static final String TAG_AREA      = "area";
    public static final String TAG_AREA_HEAP = "heap";

    public final String key;

    MicrometerMetric(String key)
    {
        this.key = key;
    }
}
