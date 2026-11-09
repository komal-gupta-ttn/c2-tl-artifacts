# Task Breakdown — Atomic, Independently Verifiable

> Linked to spec: `artifact-1-openspec-feature/spec/feature-spec.md`

## T-01 — Scaffold API endpoint + route
- **Input:** feature spec section 4
- **Output:** route registered, returns 200/201
- **Verification:** integration test hits endpoint
- **Prompt technique:** template with variables, structured output for response schema

## T-02 — Implement business logic + validation
- **Input:** AC-01, AC-02
- **Output:** service layer + input validation
- **Verification:** unit tests, >80% coverage
- **Prompt technique:** few-shot examples, JSON schema output, self-critique

## T-03 — Add error handling + rollback path
- **Input:** NFR reliability
- **Output:** graceful 4xx/5xx, rollback on failure
- **Verification:** failure-path test
- **Prompt technique:** chaining (validation → processing → error handling)

## T-04 — Add observability
- **Input:** NFR observability
- **Output:** structured logs, metrics, trace IDs
- **Verification:** log/metric assertions in tests

## T-05 — Self-review AI-generated code/tests vs spec
- **Input:** all ACs, all tasks
- **Output:** review notes, spec gaps closed
- **Verification:** review checklist in PR

## T-06 — Raise PR with spec artifact + traceability
- **Input:** completed T-01…T-05
- **Output:** PR opened, all tasks closed
- **Verification:** PR template filled, traceability table complete

---

*Update statuses in `spec/feature-spec.md` as work progresses.*
