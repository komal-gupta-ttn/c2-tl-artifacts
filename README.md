# C2 TL Artifacts

This repository contains two standalone Java Spring Boot web applications and their supporting documents.

## Contents

| Directory | Application |
|-----------|-------------|
| `artifact-1-openspec-feature/` | Code review summary tool — accepts a unified diff and produces a structured review summary (security, performance, style, test coverage) grounded in the submitted diff |
| `artifact-2-ai-infra/rag-assistant/` | RAG-backed assistant — document ingestion pipeline (chunk → embed → store → retrieve → generate → cite → evaluate) over a sample docs corpus |

Both apps are built with Java 17+, Spring Boot 3.2.5, Thymeleaf, and Maven. Each is a standalone runnable JAR.

## Build and run

Prerequisite: Java 17+ and Maven.

From the repository root:

```bash
./run-artifacts-runtime.sh
```

This builds both apps with Maven (`clean test package`) and starts them:

- Artifact 1: `http://localhost:8081/artifact1`
- Artifact 2: `http://localhost:8082/artifact2`

Logs are written to `/tmp/artifact1.log` and `/tmp/artifact2.log`.

Stop both apps:

```bash
./stop-artifacts-runtime.sh
```

To build and run each app individually:

```bash
# Artifact 1
cd artifact-1-openspec-feature
mvn clean test package
java -jar target/artifact-1-openspec-feature-1.0.0.jar
```

```bash
# Artifact 2
cd artifact-2-ai-infra/rag-assistant
mvn clean test package
java -jar target/artifact-2-rag-assistant-1.0.0.jar
```

## Key paths

**Artifact 1** (`artifact-1-openspec-feature/`):
- `spec/feature-spec.md` — feature specification and acceptance criteria
- `tasks/task-breakdown.md` — task decomposition
- `traceability.md` — spec → task → code → test traceability matrix
- `src/main/java/com/ttn/c2/artifact1/` — application code
- `src/main/resources/templates/artifact1/` — Thymeleaf UI templates
- `src/test/java/com/ttn/c2/artifact1/` — unit tests
- `pom.xml` — Maven build

**Artifact 2** (`artifact-2-ai-infra/rag-assistant/`):
- `src/main/java/com/ttn/c2/artifact2/rag/` — application code (controllers, services, models)
- `src/main/resources/templates/artifact2/` — Thymeleaf UI templates
- `src/main/resources/static/docs/` — sample document corpus
- `src/test/java/com/ttn/c2/artifact2/rag/` — unit tests
- `pom.xml` — Maven build

## Scripts

- `run-artifacts-runtime.sh` — builds and starts both applications
- `stop-artifacts-runtime.sh` — stops both applications using their PID files
