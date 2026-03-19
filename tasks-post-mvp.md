# Post-MVP Roadmap — AI Observer for Spring Boot

> Work on these after v0.1.0 is live on Maven Central.
> Ordered by: user impact first, complexity second.

---

## Phase 1 — Make It Trustworthy (Week 1–2 post-launch)

These exist to stop people from uninstalling it after day one.

### 1.1 Smarter Prompt Engineering
- [ ] Include the last 3 snapshots in the AI payload (not just current) — enables trend detection
- [ ] Add a `snapshotHistory` ring buffer (last 3, in-memory) to `InsightCache`
- [ ] Update prompt: "Here are the last 3 snapshots taken 5 minutes apart. Identify trends."
- [ ] Force structured output: add a JSON schema description in the system prompt
- [ ] Add insight deduplication: don't repeat the same `message` two cycles in a row

### 1.2 Insight Severity Routing
- [ ] Add `@ConditionalOnClass` check for Spring Events
- [ ] Publish a `CriticalInsightEvent` when any CRITICAL insight is generated
- [ ] Users can subscribe to handle it however they want (log, alert, etc.)
- [ ] Document: "Listen to AI Observer events in your app"

### 1.3 Insight History Endpoint
- [ ] Keep last 10 analysis cycles in memory (ring buffer)
- [ ] Expose at `GET /actuator/ai-insights/history`
- [ ] Include: timestamp, trigger reason, snapshot summary, insights generated
- [ ] Useful for understanding why an insight appeared

### 1.4 `/actuator/ai-insights/snapshot` Endpoint
- [ ] Expose the raw current snapshot at `GET /actuator/ai-insights/snapshot`
- [ ] Lets users see exactly what gets sent to Claude
- [ ] Builds trust — "I can see what you're sending"
- [ ] Add `@WriteOperation` trigger: `POST /actuator/ai-insights/analyze` — force immediate analysis on demand

---

## Phase 2 — Make It Configurable (Week 3–4)

### 2.1 Endpoint Exclusion Patterns
- [ ] Add `ai.observer.exclude-paths` list to properties
  ```yaml
  ai:
    observer:
      exclude-paths:
        - /actuator/**
        - /internal/**
        - /health
  ```
- [ ] Apply in `RequestMonitoringFilter` using `AntPathMatcher`

### 2.2 Data Masking
- [ ] Add `ai.observer.mask-headers` list — strip header values from any future request context
- [ ] Never include query parameters in endpoint paths (already safe if using `getRequestURI()`)
- [ ] Add a `SensitiveDataFilter` interface + default implementation — users can plug in custom masking
- [ ] Document clearly what data is and is not sent to Claude

### 2.3 Multiple AI Provider Support
- [ ] Extract `AiClient` interface with `analyze(RuntimeSnapshot, Properties)` method
- [ ] Keep `ClaudeApiClient` as the default implementation
- [ ] Add `OpenAiClient` implementation (for users who already pay for OpenAI)
  ```yaml
  ai:
    observer:
      provider: openai   # or: claude (default)
      openai-api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
  ```
- [ ] Document: users can implement `AiClient` and expose it as a `@Bean` to replace the default

### 2.4 Configurable Serialization
- [ ] Add `ai.observer.max-errors` (default 20) — cap on recent errors in snapshot
- [ ] Add `ai.observer.max-endpoints` (default 50) — cap on endpoints tracked
- [ ] Add `ai.observer.min-request-count` (default 1) — skip endpoints with fewer requests than this
- [ ] Prevents token bloat on high-traffic services with many distinct paths

---

## Phase 3 — Make It Smarter (Month 2)

### 3.1 Persistent Snapshot History (Optional)
- [ ] Add optional dependency: `spring-data-jpa` + any JDBC driver
- [ ] If on classpath: automatically persist snapshots to `ai_observer_snapshots` table
- [ ] Schema auto-created via `schema.sql` (opt-in, not auto DDL)
- [ ] Pass last 24h of hourly snapshots to Claude for deeper trend analysis
- [ ] Property: `ai.observer.persistence.enabled: false` (opt-in only)

### 3.2 Alerting Integrations (as optional modules)
> Split into separate optional artifacts to keep the core dependency lean.

- [ ] `ai-observer-slack` — publish `ai-observer-spring-boot-starter-slack`
  ```yaml
  ai:
    observer:
      slack:
        webhook-url: ${SLACK_WEBHOOK_URL}
        min-level: WARNING   # only send WARNING and CRITICAL
  ```
- [ ] `ai-observer-webhook` — generic HTTP webhook for any alerting system
  ```yaml
  ai:
    observer:
      webhook:
        url: https://your.alerting.system/hook
        min-level: CRITICAL
  ```
- [ ] Both use the `CriticalInsightEvent` from Phase 1.2

### 3.3 Micrometer Metrics for AI Observer Itself
- [ ] Register custom Micrometer metrics:
  - `ai.observer.api.calls` counter (tagged: trigger=scheduled|threshold)
  - `ai.observer.api.latency` timer
  - `ai.observer.insights.generated` counter (tagged: level=INFO|WARNING|CRITICAL)
  - `ai.observer.api.errors` counter
- [ ] Now users can graph AI Observer behavior in their existing dashboards (Grafana, Datadog, etc.)

### 3.4 Correlation IDs
- [ ] Extract MDC `traceId` / `spanId` if available (Sleuth / Micrometer Tracing)
- [ ] Include in snapshot when present — helps AI say "this error correlates with trace X"
- [ ] Add to `RuntimeSnapshot`: `Map<String, String> activeTraceIds` (sampled, max 5)

---

## Phase 4 — Ecosystem (Month 3+)

### 4.1 Spring Boot DevTools Integration
- [ ] When DevTools is on classpath (dev profile), reduce interval to 60s and log more verbosely
- [ ] Dev mode banner: "AI Observer running in dev mode — analysis every 60s"

### 4.2 Gradle Plugin / Spring Initializr
- [ ] Submit PR to Spring Initializr to add AI Observer as a selectable dependency
- [ ] Write a blog post / demo video — this is the highest-leverage distribution action

### 4.3 GraalVM Native Image Support
- [ ] Add `RuntimeHintsRegistrar` for reflection hints on `RuntimeSnapshot`, `AiInsight`
- [ ] Add `native-image.properties` with necessary includes
- [ ] Test with `spring-boot:build-image -Pnative`
- [ ] Add badge to README: "Native Image Compatible"

### 4.4 Quarkus / Micronaut Port (stretch goal)
- [ ] Evaluate demand via GitHub issues before starting
- [ ] If demand exists: extract `ai-observer-core` as a framework-agnostic module
- [ ] Implement Quarkus extension or Micronaut integration module

---

## Backlog (No Priority Yet)

- Dashboard UI (Spring Boot Admin integration)
- Kubernetes-aware snapshots (pod name, node, namespace from env)
- Cost tracking: log estimated token usage per call and cumulative daily cost
- AI-generated `curl` commands or code snippets as recommendations (not just text)
- Rate limit detection: identify if your own app is being rate-limited by downstream services
- Dead letter queue monitoring (if RabbitMQ/Kafka on classpath)
