# Artifact 1 — OpenSpec-Driven Feature Delivery

> SE/SSE-base C2 expectation: run feature delivery through a spec-first workflow
> (OpenSpec/Speckit), self-review AI-generated code/tests against acceptance
> criteria before PR, maintain traceability spec → task → code → PR.

## Feature

**AI-Assisted Code Review Summary** — given a unified diff, generates a
structured review summary (security, performance, style, test coverage) grounded
in actual diff lines.

## Directory layout

```
artifact-1-openspec-feature/
├── spec/
│   └── feature-spec.md          # OpenSpec spec (problem, ACs, NFRs, tasks)
├── tasks/
│   └── task-breakdown.md        # Atomic tasks T-01…T-06
├── traceability.md              # Spec → AC → task → commit → PR → tests
├── PR-TEMPLATE.md               # Blank PR template
├── PR-EXAMPLE.md                # Filled PR example (for demonstration)
├── src/
│   ├── main/
│   │   ├── review_summary.py    # Implementation (AI-generated, self-reviewed)
│   │   └── coding_standards.json  # Project coding standards (spec input)
│   ├── test/
│   │   ├── test_review_summary.py # Unit + integration tests (AC-01…AC-04)
│   │   └── conftest.py
│   └── prompts/
│       ├── review_summary.prompt   # few-shot + JSON schema structured output
│       └── self_critique.prompt    # self-critique prompting
└── README.md                    # this file
```

## Acceptance criteria (testable)

| ID | Criterion | Test |
|----|-----------|------|
| AC-01 | Review summary generated from PR diff (structured output) | `TestAC01_StructuredOutput` |
| AC-02 | Invalid/malformed diff returns error, never crashes | `TestAC02_InvalidInput` |
| AC-03 | Review completes within 2s for diffs < 500 lines | `TestAC03_Performance` |
| AC-04 | Findings grounded in actual diff lines (no hallucination) | `TestAC04_GroundedFindings` |

## Prompt techniques used

- **Structured output** — JSON schema enforced via `review_summary.prompt`
- **Few-shot examples** — input/output pair in prompt template
- **Self-critique** — `self_critique.prompt` reviews generated output before return
- **Chaining** — validate → generate → ground → render (fixed sequence, no agent loop)

## How to run

```bash
cd artifact-1-openspec-feature
python -m src.main.review_summary
python -m pytest src/test/ -v
```

## Traceability

Every commit message references a task ID:
```
feat: implement T-02 — review summary generator

Spec: spec/feature-spec.md
Task: T-02
AC: AC-01, AC-04
```

See `traceability.md` and `PR-EXAMPLE.md` for the full traceability matrix.
