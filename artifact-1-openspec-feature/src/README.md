# Source — Artifact 1 Feature Implementation

> AI-generated code, self-reviewed against `spec/feature-spec.md` before PR.

## Structure

```
src/
  ├── main/          # implementation (AI-generated, self-reviewed)
  ├── test/          # unit + integration tests for each AC
  └── prompts/       # prompt templates used (chaining, structured output, few-shot)
```

## Prompt templates used

| Template | Purpose | Technique |
|----------|---------|-----------|
| prompts/create-record.prompt | Generate record creation service | few-shot, JSON schema output |
| prompts/validate-input.prompt | Input validation | structured output |
| prompts/error-handling.prompt | Error + rollback logic | chaining |
| prompts/self-critique.prompt | Review generated code vs spec | self-critique |

## Convention

Commit messages reference task IDs:
```
feat: implement T-02 — business logic + validation

Spec: spec/feature-spec.md
Task: T-02
AC: AC-01, AC-02
```
