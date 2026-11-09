# Source — Artifact 1 Feature Implementation

> AI-generated code, self-reviewed against `spec/feature-spec.md` before PR.

## Structure

```
src/
  ├── main/
  │   ├── review_summary.py        # Implementation (AI-generated, self-reviewed)
  │   └── coding_standards.json    # Project coding standards (spec input)
  ├── test/
  │   ├── test_review_summary.py   # Unit + integration tests for AC-01…AC-04
  │   └── conftest.py
  └── prompts/
      ├── review_summary.prompt    # Prompt template (few-shot, JSON schema, structured output)
      └── self_critique.prompt     # Self-critique prompt (T-05 self-review)
```

## How to run

```bash
# From artifact-1-openspec-feature/
python -m src.main.review_summary

# Run tests
python -m pytest src/test/ -v
```

## Convention

Commit messages reference task IDs:
```
feat: implement T-02 — review summary generator

Spec: spec/feature-spec.md
Task: T-02
AC: AC-01, AC-04
```

## Prompt techniques used

| Template | Technique | Used for |
|---|---|---|
| `review_summary.prompt` | few-shot + JSON schema structured output | T-01, T-02 |
| `self_critique.prompt` | self-critique | T-05 |
