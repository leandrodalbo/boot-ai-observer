# Weekend MVP — AI Observer for Spring Boot

> Target: a Spring Boot starter that devs can drop in, add an API key, and get
> AI-powered JVM insights within 5 minutes. Nothing else.

---

## Prerequisites ✅

- [x] Create GitHub repo: `boot-ai-observer`
- [x] Have an Anthropic API key ready
- [x] Java 21, Maven 3.9+, IDE ready

---

## Day 1 — Saturday: Foundation + Data + AI

### Block 1 — Project Skeleton ✅

- [x] Single-module Maven project with Spring Boot 4.0.x parent
- [x] Dependencies: `spring-boot-starter-actuator`, `aspectjweaver`, `spring-boot-configuration-processor` (optional), `spring-web` (optional), `spring-webflux` (optional)
- [x] Package structure:
  ```
  io.boot.ai.observer
  ├── autoconfiguration/   (Spring config classes)
  ├── collector/           (jvm / latency / error)
  ├── config/              (properties)
  ├── model/               (RuntimeSnapshot, AiInsight)
  ├── snapshot/            (SnapshotService)
  ├── utils/               (MicrometerMetric)
  └── webfilters/          (servlet + reactive filters)
  ```
- [x] `AiObserverProperties` — record with `enabled`, `anthropicApiKey`, `model`, `maxTokens`, `intervalSeconds`, `thresholds`, `prompt`, `collectors`
- [x] `ThresholdsProperties` — `heapUsagePercent`, `threadCount`, `errorRatePercent`
- [x] `PromptProperties` — `systemPrompt`, `userTemplate`, `extraContext`
- [x] `CollectorsProperties` — `jvmEnabled`, `latencyEnabled`, `errorsEnabled` (all default `true`)
- [x] `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

---

### Block 2 — Models ✅

- [x] `RuntimeSnapshot` record — `capturedAt`, heap, threads, CPU, GC, endpoints, errors, errorCount
- [x] `EndpointStats` record — `avgLatencyMs`, `maxLatencyMs`, `requestCount`
- [x] `AiInsight` record — `level`, `message`, `recommendation`, `generatedAt`

---

### Block 3 — Metrics Collector ✅

- [x] `MicrometerJvmCollector` — reads heap, threads, CPU, GC from `MeterRegistry`
- [x] `WebLatencyCollector` — `ConcurrentHashMap` + `AtomicReference` swap on `getAndReset()`
- [x] `ServletRequestMonitoringFilter` — `OncePerRequestFilter`, skips actuator paths
- [x] `ReactiveRequestMonitoringFilter` — `WebFilter` equivalent for reactive stack
- [x] `ErrorCollector` (abstract) — bounded `ConcurrentLinkedDeque`, max 20 entries, truncates messages
- [x] `ServletErrorCollector` — AOP `@AfterThrowing` on `@RestController` / `@Controller`
- [x] `ReactiveErrorCollector` — AOP on reactive controller methods
- [x] `NonWebErrorCollector` — AOP fallback for `@Service` / `@Component` / `@Repository`
- [x] `CollectorConfiguration` — wires all collectors with `@ConditionalOnWebApplication` + `@ConditionalOnProperty(collectors.*-enabled)`
- [x] `WebFiltersConfiguration` — wires filters, gated on `collectors.latency-enabled`

---

### Block 4 — Snapshot Builder ✅

- [x] `SnapshotService` (`@Service`) — composes `RuntimeSnapshot` from all three collectors
  - All collectors injected as `Optional<>` — any can be absent (disabled via config or non-web app)
  - `getAndReset()` on each tracker — each snapshot is a fresh window
  - `errorCount` derived from `errors.size()` — no duplicate state

---

### Block 5 — Claude API Client (next)

- [ ] `ClaudeApiClient` — `java.net.http.HttpClient`, async, fail-silent
  - POST to `https://api.anthropic.com/v1/messages`
  - Headers: `x-api-key`, `anthropic-version: 2023-06-01`, `content-type: application/json`
  - Body: `{ model, max_tokens, system, messages: [{role:user, content}] }`
  - Returns `CompletableFuture<List<AiInsight>>` — empty list on any error, never throws
- [ ] `SnapshotSerializer` — compact JSON via Jackson, rounds doubles to 1 decimal, omits empty collections
- [ ] Template substitution: replace `{snapshot}` in `userTemplate`, inject `extraContext`

---

### Block 6 — Engine: Scheduler + Cache

- [ ] `InsightCache` — `volatile` fields, thread-safe read/write, in-flight guard
- [ ] `AiObserverEngine` — `@Scheduled`, calls `snapshotService.build()` + `claudeClient.analyze()`, respects `props.enabled()` and in-flight flag
- [ ] `ThresholdMonitor` — `@Scheduled` every 30s, triggers early analysis with 60s cooldown

---

### Block 7 — Autoconfiguration

- [ ] `AiObserverAutoConfiguration` — `@AutoConfiguration`, `@ConditionalOnProperty(enabled)`, `@EnableConfigurationProperties`, `@EnableScheduling`, `@EnableAspectJAutoProxy`
- [ ] Startup validation: warn and disable if `anthropic-api-key` is missing — never crash the host app

---

## Day 2 — Sunday: Endpoint + Polish + Launch Prep

### Block 8 — Actuator Endpoint

- [ ] `AiInsightsEndpoint` — `@Endpoint(id = "ai-insights")`, `@ReadOperation` returns insights + lastUpdated + enabled
- [ ] Document in README: expose via `management.endpoints.web.exposure.include`

---

### Block 9 — Customizable Prompts

- [ ] `extraContext` injected into every Claude call — users describe their app once in `application.yml`
- [ ] `systemPrompt` and `userTemplate` fully overridable

---

### Block 10 — Developer Experience

- [ ] Startup banner: `[AI Observer] Started. Model: X | Interval: Xs | Endpoint: /actuator/ai-insights`
- [ ] All logs use `[AI Observer]` prefix — easy to grep
- [ ] Never log the API key
- [ ] `@ConditionalOnMissingBean` guards on all beans — users can replace any component
- [ ] IDE autocomplete for all `ai.observer.*` properties via configuration processor

---

### Block 11 — Example App

- [ ] Standalone Spring Boot project in `example-app/` (not a Maven module)
- [ ] Demo endpoints: `GET /hello`, `GET /leak`, `GET /slow`, `GET /error`
- [ ] Fully commented `application.yml`

---

## Definition of Done

- [ ] `mvn clean install` passes
- [ ] Example app produces insights at `/actuator/ai-insights` within 5 minutes of start
- [ ] Starter works with zero config beyond the API key
- [ ] All `ai.observer.*` properties have IDE autocomplete
- [ ] No AI call more than once per minute regardless of config
- [ ] Claude API failure has zero impact on the host app

---
