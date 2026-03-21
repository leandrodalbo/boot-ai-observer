package io.boot.ai.observer.utils;

public enum AIObserverClientConstants
{

    HEADER_CONTENT_TYPE ("content-type"),
    HEADER_JSON         ("application/json"),

    REQ_MODEL      ("model"),
    REQ_MAX_TOKENS ("max_tokens"),
    REQ_MESSAGES   ("messages"),
    REQ_ROLE       ("role"),
    REQ_ROLE_USER  ("user"),
    REQ_CONTENT    ("content"),

    SNAPSHOT_PLACEHOLDER ("{snapshot}"),

    SNAP_CAPTURED_AT       ("capturedAt"),
    SNAP_HEAP_USED_PERCENT ("heapUsedPercent"),
    SNAP_HEAP_USED_MB      ("heapUsedMb"),
    SNAP_HEAP_MAX_MB       ("heapMaxMb"),
    SNAP_THREAD_COUNT      ("threadCount"),
    SNAP_DAEMON_THREADS    ("daemonThreadCount"),
    SNAP_CPU_USAGE         ("cpuUsagePercent"),
    SNAP_GC_PAUSE_MS       ("gcPauseMs"),
    SNAP_GC_COLLECTIONS    ("gcCollections"),
    SNAP_ERROR_COUNT       ("errorCount"),
    SNAP_RECENT_ERRORS     ("recentErrors"),
    SNAP_ENDPOINTS         ("endpoints"),

    ENDPOINT_AVG_LATENCY   ("avgLatencyMs"),
    ENDPOINT_MAX_LATENCY   ("maxLatencyMs"),
    ENDPOINT_REQUEST_COUNT ("requestCount");

    private final String value;

    AIObserverClientConstants(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
