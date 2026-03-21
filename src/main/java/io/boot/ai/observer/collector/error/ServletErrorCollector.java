package io.boot.ai.observer.collector.error;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ServletErrorCollector extends ErrorCollector {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)" +
              " || within(@org.springframework.stereotype.Controller *)")
    void webControllerMethods() {}

    @AfterThrowing(pointcut = "webControllerMethods()", throwing = "ex")
    public void afterThrowing(Throwable ex) {
        track(ex);
    }
}
