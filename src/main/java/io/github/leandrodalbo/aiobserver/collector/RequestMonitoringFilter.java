package io.github.leandrodalbo.aiobserver.collector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
public class RequestMonitoringFilter extends OncePerRequestFilter {

    private final LatencyTracker latencyTracker;

    public RequestMonitoringFilter(LatencyTracker latencyTracker) {
        this.latencyTracker = latencyTracker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            latencyTracker.recordLatency(path, elapsed);
        }
    }
}
