# Artifact 2 — RAG-Backed Assistant (Option A)

This artifact demonstrates the TL differentiator for C2: shared AI
infrastructure implemented as a Java Spring Boot RAG pipeline with grounded
answers and citation/evaluation support.

## What it does

Runs a deterministic RAG flow:
docs -> chunk -> embed -> store -> retrieve -> re-rank -> generate -> cite -> evaluate.

## Runtime stack

- Java 17+ (tested with Java 21)
- Spring Boot 3.2.5
- Thymeleaf UI
- Maven build/test

## Key paths

- `src/main/java/com/ttn/c2/artifact2/rag/controller/RagController.java`
- `src/main/java/com/ttn/c2/artifact2/rag/service/`
- `src/main/resources/templates/artifact2/`
- `src/main/resources/static/docs/` (sample corpus)
- `src/main/resources/eval/labeled_qa.jsonl`
- `src/test/java/com/ttn/c2/artifact2/rag/`

## Build and run (Java runtime mode)

```bash
cd artifact-2-ai-infra/rag-assistant
mvn clean test package
java -jar target/artifact-2-rag-assistant-1.0.0.jar
```

App URL: `http://localhost:8082/artifact2`

## Dev mode (optional)

```bash
cd artifact-2-ai-infra/rag-assistant
mvn spring-boot:run
```
