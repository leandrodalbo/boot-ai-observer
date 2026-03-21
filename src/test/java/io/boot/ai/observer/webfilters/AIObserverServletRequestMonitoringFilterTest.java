package io.boot.ai.observer.webfilters;

import io.boot.ai.observer.collector.latency.AIObserverWebLatencyCollector;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIObserverServletRequestMonitoringFilterTest {

    @Mock
    AIObserverWebLatencyCollector latencyCollector;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain         filterChain;

    private AIObserverServletRequestMonitoringFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AIObserverServletRequestMonitoringFilter(latencyCollector);
    }

    @Test
    void doFilter_regularPath_recordsLatency() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Long> latencyCaptor = ArgumentCaptor.forClass(Long.class);
        verify(latencyCollector).record(eq("/api/orders"), latencyCaptor.capture());
        assertThat(latencyCaptor.getValue()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void doFilter_regularPath_continuesFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_actuatorPath_skipsLatencyRecording() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(latencyCollector);
    }

    @Test
    void doFilter_actuatorPath_continuesFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_exceptionThrown_stillRecordsLatency() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");
        doThrow(new RuntimeException("downstream error")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class);

        verify(latencyCollector).record(eq("/api/orders"), anyLong());
    }
}
