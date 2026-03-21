package io.boot.ai.observer.webfilters;

import io.boot.ai.observer.collector.latency.WebLatencyCollector;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ServletRequestMonitoringFilter extends OncePerRequestFilter {

    private static final String ACTUATOR_PREFIX = "/actuator";

    private final WebLatencyCollector latencyCollector;

    public ServletRequestMonitoringFilter(WebLatencyCollector latencyCollector) {
        this.latencyCollector = latencyCollector;
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
            latencyCollector.record(path, System.currentTimeMillis() - start);
        }
    }
}
