package io.github.leandrodalbo.aiobserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.observer")
public class AiObserverProperties {

    private boolean enabled = true;
    private String anthropicApiKey;
    private String model = "claude-haiku-4-5-20251001";
    private int maxTokens = 1024;
    private long intervalSeconds = 300;
    private Thresholds thresholds = new Thresholds();
    private Prompt prompt = new Prompt();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getAnthropicApiKey() { return anthropicApiKey; }
    public void setAnthropicApiKey(String anthropicApiKey) { this.anthropicApiKey = anthropicApiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public long getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(long intervalSeconds) { this.intervalSeconds = intervalSeconds; }

    public Thresholds getThresholds() { return thresholds; }
    public void setThresholds(Thresholds thresholds) { this.thresholds = thresholds; }

    public Prompt getPrompt() { return prompt; }
    public void setPrompt(Prompt prompt) { this.prompt = prompt; }

    public static class Thresholds {
        private double heapUsagePercent = 80.0;
        private int threadCount = 200;
        private double errorRatePercent = 5.0;

        public double getHeapUsagePercent() { return heapUsagePercent; }
        public void setHeapUsagePercent(double heapUsagePercent) { this.heapUsagePercent = heapUsagePercent; }

        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

        public double getErrorRatePercent() { return errorRatePercent; }
        public void setErrorRatePercent(double errorRatePercent) { this.errorRatePercent = errorRatePercent; }
    }

    public static class Prompt {
        private String systemPrompt = """
                You are a JVM and Spring Boot performance expert.
                Analyze the provided runtime snapshot and return ONLY a JSON array of insights.
                Focus on correlations between metrics, not isolated thresholds.
                Each insight: {"level":"INFO|WARNING|CRITICAL","message":"...","recommendation":"..."}
                Be specific. Never say "consider increasing heap" without a concrete reason.
                Return [] if everything looks healthy.
                """;
        private String userTemplate =
                "Runtime snapshot from a Spring Boot application:\n{snapshot}\n\nReturn JSON insights only.";
        private String extraContext = "";

        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

        public String getUserTemplate() { return userTemplate; }
        public void setUserTemplate(String userTemplate) { this.userTemplate = userTemplate; }

        public String getExtraContext() { return extraContext; }
        public void setExtraContext(String extraContext) { this.extraContext = extraContext; }
    }
}
