package io.github.leandrodalbo.aiobserver.collector;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Aspect
@Component
public class ErrorTracker {

    private static final int MAX_ERRORS = 20;
    private final ConcurrentLinkedDeque<String> recentErrors = new ConcurrentLinkedDeque<>();

    @AfterThrowing(
            pointcut = "within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)",
            throwing = "ex"
    )
    public void trackError(JoinPoint joinPoint, Throwable ex) {
        String errorSummary = ex.getClass().getSimpleName() + ": " +
                truncate(ex.getMessage(), 100);
        recentErrors.addLast(errorSummary);
        while (recentErrors.size() > MAX_ERRORS) {
            recentErrors.pollFirst();
        }
    }

    public List<String> getAndReset() {
        List<String> errors = new ArrayList<>(recentErrors);
        recentErrors.clear();
        return errors;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
