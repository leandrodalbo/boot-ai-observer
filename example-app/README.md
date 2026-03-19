# AI Observer Example App

This is a standalone Spring Boot application that demonstrates the `ai-observer-spring-boot-starter`.

## Prerequisites

- Java 17+
- Maven 3.9+
- An Anthropic API key

## Running

1. First, install the starter locally:
   ```bash
   cd ..
   mvn clean install
   cd example-app
   ```

2. Start the app with your API key:
   ```bash
   ANTHROPIC_API_KEY=your_key_here mvn spring-boot:run
   ```

## Demo Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /hello` | Healthy baseline response |
| `GET /leak` | Allocates 1MB of memory each call |
| `GET /slow` | Waits 2 seconds |
| `GET /error` | Throws a RuntimeException |

## Viewing Insights

```
http://localhost:8080/actuator/ai-insights
```
