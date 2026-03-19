package io.boot.ai.observer.config;

import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Metric thresholds that trigger an early AI analysis before the next scheduled interval.
 * Crossing any single threshold is enough to trigger analysis.
 *
 * ai:
 *   observer:
 *     thresholds:
 *       heap-usage-percent: 75.0
 *       thread-count: 150
 *       error-rate-percent: 2.0
 */
public record ThresholdsProperties(

        /** Heap used as a percentage of max heap. Default: 80.0 */
        @DefaultValue("80.0") double heapUsagePercent,

        /** Live thread count. Default: 200 */
        @DefaultValue("200") int threadCount,

        /** Errors as a percentage of total requests in the current window. Default: 5.0 */
        @DefaultValue("5.0") double errorRatePercent

) {}
