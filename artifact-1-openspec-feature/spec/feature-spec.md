# Feature Spec — OpenSpec-Driven Delivery (Artifact 1)

> **Purpose:** Demonstrates SE/SSE-base C2 expectation — run feature/bug delivery through a spec-first workflow (OpenSpec/Speckit), self-review AI-generated code/tests against acceptance criteria, and maintain traceability from spec → task → code → PR.

## 1. Problem statement

[TODO: Describe the user/business problem this feature solves. Keep it to 2–3 sentences.]

## 2. Acceptance criteria (testable)

Each criterion must be independently verifiable. Link each to a task ID below.

| ID | Acceptance criterion | Verification method | Linked task(s) |
|----|----------------------|---------------------|-----------------|
| AC-01 | [e.g. User can create a record via API] | Automated integration test, HTTP 201 | T-01, T-02 |
| AC-02 | [e.g. Invalid input returns 400 with error message] | Unit test + contract test | T-03 |
| AC-03 | [e.g. Record appears in list view within 2s] | Performance test | T-04 |
| AC-04 | [e.g. AI-generated summary is grounded in source text, no hallucinated entities] | Eval harness: precision/recall on labeled set | T-05 |

## 3. Non-functional requirements (NFRs)

- **Performance:** [e.g. p95 < 500ms for read path]
- **Reliability:** [e.g. 99.9% uptime, rollback path defined]
- **Security:** [e.g. input validation, authz check, no secrets in code]
- **Observability:** [e.g. structured logs, trace ID propagation, key metrics]
- **Maintainability:** [e.g. spec and code traceable via commit convention]

## 4. Task decomposition (atomic, independently verifiable)

| Task ID | Description | Acceptance (Definition of Done) | Status |
|---------|-------------|----------------------------------|--------|
| T-01 | Scaffold API endpoint + route | Route returns 200/201, wired in router | ☐ |
| T-02 | Implement business logic + validation | Unit tests pass, coverage > 80% | ☐ |
| T-03 | Add error handling + rollback path | Failure returns 4xx/5xx gracefully | ☐ |
| T-04 | Add observability (logs, metrics, traces) | Logs emitted, metrics registered | ☐ |
| T-05 | Self-review AI-generated code/tests vs spec | Review notes attached, no open spec gaps | ☐ |
| T-06 | Raise PR with spec artifact + traceability | PR links spec, all tasks closed | ☐ |

## 5. Traceability

Every code change must reference a spec task ID via commit convention:

```
feat: implement T-02 — business logic + validation

- adds service layer for record creation
- includes unit tests covering AC-02
- self-reviewed against spec acceptance criteria

Spec: artifact-1-openspec-feature/spec/feature-spec.md
Task: T-02
```

PR template: `artifact-1-openspec-feature/PR-TEMPLATE.md`

## 6. Prompt-engineering approach (non-trivial tasks)

- Use **prompt templates with variables** (not one-off ad hoc prompts).
- Apply **few-shot examples** for structured output.
- Enforce **JSON schema / structured output** for AI-generated responses.
- Use **prompt chaining** (no full agent loop) for multi-step generation.
- Apply **self-critique prompting** before finalizing AI-generated code.

## 7. Self-review checklist (before PR)

- [ ] All acceptance criteria (AC-01…AC-04) have linked tests.
- [ ] AI-generated code reviewed against spec, not just "looks reasonable."
- [ ] NFRs addressed (performance, security, observability).
- [ ] Rollback/error path defined and tested.
- [ ] Spec artifact and traceability visible in PR description.
- [ ] Commit messages reference task IDs.

---

*Status: Draft — to be completed as the feature is built.*
