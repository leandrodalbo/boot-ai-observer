package io.boot.ai.observer.request;

import io.boot.ai.observer.collector.LatencyTracker;
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
class RequestMonitoringFilterTest {

    @Mock
    private LatencyTracker latencyTracker;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestMonitoringFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestMonitoringFilter(latencyTracker);
    }

    @Test
    void doFilterInternal_regularPath_recordsLatency() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Long> latencyCaptor = ArgumentCaptor.forClass(Long.class);
        verify(latencyTracker).recordLatency(eq("/api/orders"), latencyCaptor.capture());
        assertThat(latencyCaptor.getValue()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void doFilterInternal_regularPath_continuesFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_actuatorPath_skipsLatencyRecording() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(latencyTracker);
    }

    @Test
    void doFilterInternal_actuatorPath_continuesFilterChain() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_exceptionThrown_stillRecordsLatency() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/orders");
        doThrow(new RuntimeException("downstream error")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(RuntimeException.class);

        verify(latencyTracker).recordLatency(eq("/api/orders"), anyLong());
    }
}
