package io.github.leandrodalbo.aiobserver;

import io.github.leandrodalbo.aiobserver.config.AiObserverProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AiObserverPropertiesTest.TestConfig.class,
        properties = {
                "ai.observer.anthropic-api-key=test-key",
                "spring.main.web-application-type=none"
        })
public class AiObserverPropertiesTest {

    @Autowired
    private AiObserverProperties props;

    @Test
    void defaultValues() {
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getModel()).isEqualTo("claude-haiku-4-5-20251001");
        assertThat(props.getMaxTokens()).isEqualTo(1024);
        assertThat(props.getIntervalSeconds()).isEqualTo(300);
        assertThat(props.getThresholds().getHeapUsagePercent()).isEqualTo(80.0);
        assertThat(props.getThresholds().getThreadCount()).isEqualTo(200);
        assertThat(props.getThresholds().getErrorRatePercent()).isEqualTo(5.0);
    }

    @Test
    void apiKeyIsSet() {
        assertThat(props.getAnthropicApiKey()).isEqualTo("test-key");
    }

    @EnableConfigurationProperties(AiObserverProperties.class)
    static class TestConfig {}
}
