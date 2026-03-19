# AI Observer for Spring Boot

> Drop-in Spring Boot starter that gives you AI-powered JVM insights in 5 minutes. Powered by Claude (Anthropic).

## What It Does

AI Observer collects JVM metrics, endpoint latency, and error data from your Spring Boot app, then periodically sends a compact snapshot to Claude for analysis. Insights are served at `/actuator/ai-insights`.

## Quickstart (30 seconds)

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.leandrodalbo</groupId>
    <artifactId>ai-observer-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Add your API key

```yaml
ai:
  observer:
    anthropic-api-key: ${ANTHROPIC_API_KEY}
```

### 3. Expose the endpoint

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,ai-insights
```

### 4. Get insights

```bash
curl http://localhost:8080/actuator/ai-insights
```

## Configuration Reference

```yaml
ai:
  observer:
    anthropic-api-key: ${ANTHROPIC_API_KEY}
    model: claude-haiku-4-5-20251001
    interval-seconds: 300
    max-tokens: 1024
    enabled: true
    thresholds:
      heap-usage-percent: 80.0
      thread-count: 200
      error-rate-percent: 5.0
    prompt:
      extra-context: |
        This is a payment processing service handling ~500 req/min.
```

## Compatibility

- Spring Boot 3.x
- Java 17+
- Requires Spring Boot Actuator on the classpath

## Example App

See [example-app/](example-app/) for a complete demo application.
AI Observer for Spring Boot
