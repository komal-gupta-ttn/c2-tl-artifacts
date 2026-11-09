# Artifact 1 — OpenSpec-Driven Feature Delivery

This artifact demonstrates the SE/SSE base C2 expectation using a Java Spring
Boot implementation: spec-first delivery, AI-assisted implementation, and
traceability from spec to working software.

## Feature

**AI-Assisted Code Review Summary** — accepts a unified diff and produces a
structured summary (security, performance, style, test coverage) grounded in
the submitted diff.

## Runtime stack

- Java 17+ (tested with Java 21)
- Spring Boot 3.2.5
- Thymeleaf UI
- Maven build/test

## Key paths

- `spec/feature-spec.md` — OpenSpec spec
- `tasks/task-breakdown.md` — task decomposition
- `traceability.md` — AC/task/code/test traceability
- `src/main/java/com/ttn/c2/artifact1/` — application code
- `src/main/resources/templates/artifact1/` — UI flow pages
- `src/test/java/com/ttn/c2/artifact1/` — unit tests

## Build and run (Java runtime mode)

```bash
cd artifact-1-openspec-feature
mvn clean test package
java -jar target/artifact-1-openspec-feature-1.0.0.jar
```

App URL: `http://localhost:8081/artifact1`

## Dev mode (optional)

```bash
cd artifact-1-openspec-feature
mvn spring-boot:run
```
