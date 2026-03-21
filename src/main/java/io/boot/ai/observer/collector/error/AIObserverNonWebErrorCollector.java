package io.boot.ai.observer.collector.error;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AIObserverNonWebErrorCollector extends AIObserverErrorCollector
{

    @Pointcut("within(@org.springframework.stereotype.Service *)" +
              " || within(@org.springframework.stereotype.Component *)" +
              " || within(@org.springframework.stereotype.Repository *)")
    void springComponents() {}

    @AfterThrowing(pointcut = "springComponents()", throwing = "ex")
    public void afterThrowing(Throwable ex) {
        track(ex);
    }
}
