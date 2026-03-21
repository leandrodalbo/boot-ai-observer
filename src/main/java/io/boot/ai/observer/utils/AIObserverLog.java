package io.boot.ai.observer.utils;

import org.slf4j.Logger;

public enum AIObserverLog
{

    STARTUP_NO_API_KEY        ("'ai.observer.ai-api-key' is not set — AI analysis will not run"),
    API_CALL_FAILED           ("AI API call failed: {}"),
    API_REQUEST_BUILD_FAILED  ("Failed to build AI API request: {}"),
    API_RESPONSE_PARSE_FAILED ("Failed to parse AI API response: {}"),
    API_HTTP_ERROR            ("AI API returned HTTP {}: {}");

    private static final String PREFIX = "[AI Observer] ";

    private final String template;

    AIObserverLog(String template) {
        this.template = template;
    }

    public void warn(Logger log, Object... args) {
        log.warn(PREFIX + template, args);
    }

    public void info(Logger log, Object... args) {
        log.info(PREFIX + template, args);
    }
}
