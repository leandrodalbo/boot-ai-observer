package io.boot.ai.observer.autoconfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.boot.ai.observer.collector.error.AIObserverErrorCollector;
import io.boot.ai.observer.collector.jvm.AIObserverMicrometerJvmCollector;
import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import io.boot.ai.observer.config.AIObserverProperties;
import io.boot.ai.observer.snapshot.AIObserverSnapshotService;
import io.boot.ai.observer.utils.AIObserverLog;
import io.boot.ai.observer.utils.AIObserverSerializer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@AutoConfiguration
@EnableConfigurationProperties(AIObserverProperties.class)
@EnableScheduling
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class AIObserverAutoConfiguration
{

    private static final Logger log = LoggerFactory.getLogger(AIObserverAutoConfiguration.class);

    private final AIObserverProperties props;

    public AIObserverAutoConfiguration(AIObserverProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void validate() {
        if (props.aiApiKey() == null || props.aiApiKey().isBlank()) {
            AIObserverLog.STARTUP_NO_API_KEY.warn(log);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public AIObserverSerializer serializer(ObjectMapper mapper) {
        return new AIObserverSerializer(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AIObserverSnapshotService snapshotService(Optional<AIObserverMicrometerJvmCollector> jvmCollector,
                                                     Optional<AIObserverWebLatencyCollector> latencyCollector,
                                                     Optional<AIObserverErrorCollector> errorCollector) {
        return new AIObserverSnapshotService(jvmCollector, latencyCollector, errorCollector);
    }
}
