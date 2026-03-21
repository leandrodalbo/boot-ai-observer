package io.boot.ai.observer.utils;


public enum AIObserverPrompts
{

    RESPONSE_FORMAT("""
            You are a JVM and Spring Boot performance expert.
            Analyze the provided runtime snapshot and give clear, actionable insights.
            Focus on correlations between metrics, not isolated thresholds.
            Be specific — never give generic advice without a concrete reason from the data.
            If everything looks healthy, say so briefly."""),

    SNAPSHOT_TEMPLATE("Runtime snapshot from a Spring Boot application:\n{snapshot}\n\nProvide your analysis.");

    private final String text;

    AIObserverPrompts(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}
