package io.boot.ai.observer.collector.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AIObserverNonWebErrorCollectorTest {

    private AIObserverNonWebErrorCollector collector;

    @BeforeEach
    void setUp() {
        collector = new AIObserverNonWebErrorCollector();
    }

    @Test
    void track_storesErrorClassAndMessagePrefix() {
        collector.track(new IllegalArgumentException("bad input value"));

        List<String> errors = collector.getAndReset();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).startsWith("IllegalArgumentException: bad input value");
    }

    @Test
    void track_nullMessage_doesNotThrow() {
        collector.track(new NullPointerException(null));

        List<String> errors = collector.getAndReset();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).startsWith("NullPointerException:");
    }

    @Test
    void track_longMessage_truncatesTo120Chars() {
        collector.track(new RuntimeException("x".repeat(200)));

        String entry       = collector.getAndReset().get(0);
        String messagePart = entry.substring(entry.indexOf(": ") + 2);

        assertThat(messagePart.length()).isLessThanOrEqualTo(121);
    }

    @Test
    void getAndReset_clearsBuffer_subsequentCallReturnsEmpty() {
        collector.track(new RuntimeException("err"));

        collector.getAndReset();

        assertThat(collector.getAndReset()).isEmpty();
    }

    @Test
    void track_beyondMaxCapacity_dropsOldestEntry() {
        for (int i = 0; i < 21; i++) {
            collector.track(new RuntimeException("error-" + i));
        }

        List<String> errors = collector.getAndReset();

        assertThat(errors).hasSize(20);
        assertThat(errors.get(0)).contains("error-1");
        assertThat(errors.get(19)).contains("error-20");
    }
}
