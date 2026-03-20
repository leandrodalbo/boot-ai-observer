package io.boot.ai.observer.request;

import io.boot.ai.observer.collector.LatencyTracker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestMonitoringFilter extends OncePerRequestFilter {

    private static final String ACTUATOR_PREFIX = "/actuator";

    private final LatencyTracker latencyTracker;

    public RequestMonitoringFilter(LatencyTracker latencyTracker) {
        this.latencyTracker = latencyTracker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith(ACTUATOR_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            latencyTracker.recordLatency(path, System.currentTimeMillis() - start);
        }
    }
}
