# Weekend MVP — AI Observer for Spring Boot

> Target: a publishable Spring Boot starter that devs can drop in, add an API key, and get
> AI-powered JVM insights within 5 minutes. Nothing else.

---

## Prerequisites (Friday night, ~30 min)

- [x] Create GitHub repo: `boot-ai-observer`
- [] Register on [Sonatype OSSRH](https://central.sonatype.com/) — Maven Central approval can take 1-2 business days, start now
- [ ] Have an Anthropic API key ready
- [x] Java 21+, Maven

---

## Day 1 — Saturday: Foundation + Data + AI

### Block 1 — Project Skeleton

- [ ] Create single-module Maven project: `ai-observer-spring-boot-starter`
  - `groupId`: your group (e.g. `io.github.yourname`)
  - `artifactId`: `ai-observer-spring-boot-starter`
  - Spring Boot parent: `3.2.x`
- [ ] Add dependencies to `pom.xml`:

  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-aop</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-configuration-processor</artifactId>
      <optional>true</optional>
  </dependency>
  ```

- [x] Create `AiObserverProperties.java` — the single config class:

  ```java
  @ConfigurationProperties(prefix = "ai.observer")
  public class AiObserverProperties {
      private boolean enabled = true;
      private String anthropicApiKey;          // required
      private String model = "claude-haiku-4-5-20251001"; // cheapest, fast enough
      private int maxTokens = 1024;
      private long intervalSeconds = 300;      // 5 min default — not spammy
      private Thresholds thresholds = new Thresholds();
      private Prompt prompt = new Prompt();

      public static class Thresholds {
          private double heapUsagePercent = 80.0;   // trigger early analysis
          private int threadCount = 200;
          private double errorRatePercent = 5.0;
      }

      public static class Prompt {
          // users can fully override these from their application.yml
          private String systemPrompt = """
              You are a JVM and Spring Boot performance expert.
              Analyze the provided runtime snapshot and return ONLY a JSON array of insights.
              Focus on correlations between metrics, not isolated thresholds.
              Each insight: {"level":"INFO|WARNING|CRITICAL","message":"...","recommendation":"..."}
              Be specific. Never say "consider increasing heap" without a concrete reason.
              Return [] if everything looks healthy.
              """;
          private String userTemplate =
              "Runtime snapshot from a Spring Boot application:\n{snapshot}\n\nReturn JSON insights only.";
          private String extraContext = ""; // injected between system and user — users describe their app
      }
  }
  ```

- [x] Create `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
  ```
  io.github.yourname.aiobserver.config.AiObserverAutoConfiguration
  ```

---

### Block 2 — Models

- [] Create `RuntimeSnapshot.java`:

  ```java
  public record RuntimeSnapshot(
      Instant capturedAt,
      double heapUsedPercent,
      long heapUsedMb,
      long heapMaxMb,
      int threadCount,
      int daemonThreadCount,
      double cpuUsagePercent,
      long gcPauseMs,          // total GC time in last interval
      long gcCollections,
      Map<String, EndpointStats> endpoints,  // path -> stats
      List<String> recentErrors,             // last N unique error types
      int errorCount
  ) {}

  public record EndpointStats(double avgLatencyMs, double maxLatencyMs, int requestCount) {}
  ```

- [ ] Create `AiInsight.java`:
  ```java
  public record AiInsight(
      String level,           // INFO | WARNING | CRITICAL
      String message,
      String recommendation,
      Instant generatedAt
  ) {}
  ```

---

### Block 3 — Metrics Collector

- [x] Create `JvmMetricsCollector.java` — use Micrometer's `MeterRegistry`, not raw MXBeans:

  ```java
  @Component
  @ConditionalOnBean(MeterRegistry.class)
  public class JvmMetricsCollector {
      // Read from MeterRegistry — all JVM metrics are already there via actuator
      // jvm.memory.used / jvm.memory.max → heap percent
      // jvm.threads.live → thread count
      // jvm.threads.daemon → daemon threads
      // process.cpu.usage → CPU
      // jvm.gc.pause → GC time (sum over interval)
      // jvm.gc.memory.allocated → GC collections proxy
  }
  ```

  > Why Micrometer: metrics are already collected, you're just reading them. Zero overhead.

- [x] Create `LatencyTracker.java` — in-memory concurrent store, cleaned on each snapshot:

  ```java
  @Component
  public class LatencyTracker {
      // ConcurrentHashMap<String, List<Long>> path -> latency samples
      // Exposed via: recordLatency(String path, long millis)
      //              getAndReset() -> Map<String, EndpointStats>
  }
  ```

- [x] Create `RequestMonitoringFilter.java`:

  ```java
  @Component
  @ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
  public class RequestMonitoringFilter extends OncePerRequestFilter {
      // Record latency per path pattern (use request.getRequestURI())
      // Skip actuator paths — don't pollute with internal noise
      // Skip if path matches exclude patterns (configurable later)
  }
  ```

- [x] Create `ErrorTracker.java` — AOP, not ControllerAdvice:
  ```java
  @Aspect
  @Component
  public class ErrorTracker {
      // @AfterThrowing on execution(* *(..)) within @RestController or @Controller
      // Store: error class name + message prefix (not full stack — too noisy for AI)
      // Keep last 20 unique errors in a bounded ConcurrentLinkedDeque
      // getAndReset() -> List<String>
  }
  ```

---

### Block 4 — Snapshot Builder (12:30–13:00)

- [ ] Create `SnapshotService.java`:
  ```java
  @Component
  public class SnapshotService {
      public RuntimeSnapshot build() {
          // Compose from JvmMetricsCollector + LatencyTracker + ErrorTracker
          // getAndReset() on trackers so each snapshot is a fresh window
      }
  }
  ```

---

### Block 5 — Claude API Client (14:00–15:30)

> Use `java.net.http.HttpClient` — zero extra dependencies.

- [ ] Create `ClaudeApiClient.java`:

  ```java
  @Component
  public class ClaudeApiClient {

      private static final String API_URL = "https://api.anthropic.com/v1/messages";

      // POST with:
      // Headers: x-api-key, anthropic-version: 2023-06-01, content-type: application/json
      // Body: { model, max_tokens, system, messages: [{role:user, content}] }

      public CompletableFuture<List<AiInsight>> analyze(RuntimeSnapshot snapshot,
                                                          AiObserverProperties props) {
          // 1. Build prompt: system (from props) + extraContext (if set) + user (snapshot JSON)
          // 2. POST async (don't block app threads)
          // 3. Parse response content[0].text as JSON array of AiInsight
          // 4. Return empty list on any error (fail silently — never crash the host app)
          // 5. Log warnings on failure, never throw
      }
  }
  ```

- [ ] Create `SnapshotSerializer.java` — compact JSON for the prompt (keep tokens low):
  ```java
  // Use Jackson ObjectMapper (already on classpath via Spring Boot)
  // Round doubles to 1 decimal place
  // Only include endpoints with requestCount > 0
  // Omit empty error lists
  ```

---

### Block 6 — Engine: Scheduler + Cache (15:30–17:00)

- [ ] Create `InsightCache.java`:

  ```java
  @Component
  public class InsightCache {
      private volatile List<AiInsight> insights = List.of();
      private volatile Instant lastUpdated;
      private volatile boolean analysisInFlight = false;  // prevent concurrent calls

      // Thread-safe read/write
      // Expose: get(), store(List<AiInsight>), isInFlight(), setInFlight(boolean)
      // Expose: getLastUpdated()
  }
  ```

- [ ] Create `AiObserverEngine.java` — the smart scheduler:

  ```java
  @Component
  public class AiObserverEngine {

      @Scheduled(fixedDelayString = "${ai.observer.interval-seconds:300}000")
      public void scheduledAnalysis() {
          runAnalysisIfAppropriate("scheduled");
      }

      // Called by threshold monitor — trigger early if something looks wrong
      public void triggerEarly(String reason) {
          runAnalysisIfAppropriate("threshold:" + reason);
      }

      private void runAnalysisIfAppropriate(String trigger) {
          if (!props.isEnabled()) return;
          if (cache.isInFlight()) return;  // previous call still running — skip

          cache.setInFlight(true);
          RuntimeSnapshot snapshot = snapshotService.build();

          claudeClient.analyze(snapshot, props)
              .whenComplete((insights, err) -> {
                  cache.setInFlight(false);
                  if (insights != null && !insights.isEmpty()) {
                      cache.store(insights);
                      log.info("[AI Observer] {} insights generated (trigger={})", insights.size(), trigger);
                  }
              });
      }
  }
  ```

- [ ] Create `ThresholdMonitor.java`:
  ```java
  @Component
  public class ThresholdMonitor {
      // @Scheduled(fixedDelay = 30000) — cheap local check, no AI call
      // If heap > threshold OR errors spiked → call engine.triggerEarly(reason)
      // Cooldown: don't trigger more than once per 60s regardless of metrics
  }
  ```

---

### Block 7 — Autoconfiguration (17:00–18:00)

- [ ] Create `AiObserverAutoConfiguration.java`:
  ```java
  @AutoConfiguration
  @ConditionalOnProperty(prefix = "ai.observer", name = "enabled", matchIfMissing = true)
  @ConditionalOnClass(MeterRegistry.class)
  @EnableConfigurationProperties(AiObserverProperties.class)
  @EnableScheduling
  @EnableAspectJAutoProxy
  @ComponentScan("io.github.yourname.aiobserver")
  public class AiObserverAutoConfiguration {
      // Validate API key on startup with a friendly error message:
      // "AI Observer: anthropic-api-key is not set. Add ai.observer.anthropic-api-key to your config."
      @PostConstruct
      public void validate() { ... }
  }
  ```

---

## Day 2 — Sunday: Endpoint + Prompts + Polish + Publish Prep

### Block 8 — Actuator Endpoint (9:00–10:00)

- [ ] Create `AiInsightsEndpoint.java`:

  ```java
  @Endpoint(id = "ai-insights")
  @Component
  public class AiInsightsEndpoint {

      @ReadOperation
      public InsightsResponse insights() {
          return new InsightsResponse(
              cache.get(),
              cache.getLastUpdated(),
              props.isEnabled()
          );
      }

      public record InsightsResponse(
          List<AiInsight> insights,
          Instant lastUpdated,
          boolean enabled
      ) {}
  }
  ```

- [ ] Add to README: how to expose the endpoint:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,info,ai-insights # don't expose *, be explicit
  ```

---

### Block 9 — Customizable Prompts (10:00–11:00)

> Users describe their app once in `application.yml` — all future insights are contextualized.

- [ ] Document prompt customization in README:

  ```yaml
  ai:
    observer:
      anthropic-api-key: ${ANTHROPIC_API_KEY}
      interval-seconds: 300
      model: claude-haiku-4-5-20251001

      prompt:
        # Describe your app — injected into every AI call
        extra-context: |
          This is a payment processing service handling ~500 req/min.
          It uses PostgreSQL and Redis. Peak traffic is 9am-6pm UTC.

        # Full system prompt override (optional — default is already good)
        system-prompt: |
          You are a JVM expert monitoring a fintech application.
          Prioritize latency and error rate insights over memory warnings.
          ...

        # User message template — {snapshot} is replaced with JSON
        user-template: |
          Here is the current state of our payment service:
          {snapshot}
          Focus on anything that could affect transaction success rates.

      thresholds:
        heap-usage-percent: 75.0
        thread-count: 150
        error-rate-percent: 2.0
  ```

- [ ] Implement template substitution in `ClaudeApiClient` — replace `{snapshot}` in `userTemplate` with serialized snapshot JSON
- [ ] Implement `extraContext` injection: insert between system prompt and user message as an additional system turn or prepended to user message

---

### Block 10 — Developer Experience (11:00–12:30)

- [ ] Startup banner log (INFO level):
  ```
  [AI Observer] Started. Model: claude-haiku-4-5-20251001 | Interval: 300s | Endpoint: /actuator/ai-insights
  ```
- [ ] Add `@ConditionalOnMissingBean` guards so users can replace any component
- [ ] Ensure all AI Observer logs use a single logger prefix `[AI Observer]` — easy to filter
- [ ] Never log the API key (even partially)
- [ ] Add `spring-configuration-metadata.json` hints (generated by `spring-boot-configuration-processor`) so users get IDE autocomplete for all `ai.observer.*` properties
- [ ] If `anthropic-api-key` is missing: log a clear warning and disable silently instead of crashing the host app

---

### Block 11 — Example App (13:30–14:30)

- [ ] Create `example-app/` as a standalone Spring Boot project in the repo (not a module — keeps the starter clean)
- [ ] Add a few demo endpoints:
  - `GET /hello` — healthy baseline
  - `GET /leak` — allocates memory to trigger heap warning
  - `GET /slow` — adds artificial latency
  - `GET /error` — throws an exception
- [ ] Add `application.yml` with all config options documented and commented
- [ ] Add a `README.md` in `example-app/` with: run instructions + expected output at `/actuator/ai-insights`

---

### Block 12 — README + Publish Prep (14:30–17:00)

- [ ] Write `README.md`:
  - [ ] One-sentence hook: what it does and why you'd want it
  - [ ] 30-second quickstart (dependency + API key + endpoint — nothing else required)
  - [ ] Full config reference (every `ai.observer.*` property with defaults)
  - [ ] Prompt customization section (copy-paste examples)
  - [ ] Cost FAQ: "How many Claude calls does this make?" (default: 12/hour, configurable)
  - [ ] Security note: always use `${ANTHROPIC_API_KEY}` env var, never hardcode
  - [ ] Compatibility: Spring Boot 3.x, Java 17+
- [ ] Set up GitHub Actions: build + test on push
- [ ] Configure Maven for Central publish:
  - `pom.xml`: add `<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`, `<scm>`
  - Add `maven-source-plugin`, `maven-javadoc-plugin`, `maven-gpg-plugin`
  - Configure Sonatype staging via `nexus-staging-maven-plugin`
- [ ] Tag `v0.1.0` and publish to Maven Central staging

---

## Definition of Done for Weekend MVP

- [ ] `mvn clean install` passes with at least one integration test
- [ ] Example app starts and produces insights at `/actuator/ai-insights` within 5 minutes
- [ ] Starter works with zero config beyond the API key
- [ ] All `ai.observer.*` properties have IDE autocomplete
- [ ] No AI call is made more often than once per minute regardless of config
- [ ] If Claude API fails, the host app is completely unaffected
- [ ] README answers: what is it, how to add it, what does it cost

---

## Hard Limits (keep MVP focused)

- No database, no file storage, no external dependencies beyond Spring Boot
- No multi-module Maven — single artifact is easier to publish and consume
- No UI — the actuator endpoint is the UI
- No Slack/email alerting — that's post-MVP
- No bytecode instrumentation — too complex for a weekend
- Insights are best-effort — a failure is a logged warning, never an exception
