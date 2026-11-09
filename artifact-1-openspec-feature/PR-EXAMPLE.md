# PR Example — Filled Template (Artifact 1)

> This is a filled example showing traceability from spec → task → code → PR,
> for demonstration during Mentor review.

## Summary
Implemented "AI-Assisted Code Review Summary" feature: given a unified diff,
generates a structured review summary (security, performance, style, test
coverage) grounded in actual diff lines. Built entirely through OpenSpec-driven
flow.

## Spec
- Spec file: `artifact-1-openspec-feature/spec/feature-spec.md`
- All acceptance criteria linked to tasks: **Yes**

## Traceability

| Task ID | Commit(s) | PR section | Status |
|---------|-----------|------------|--------|
| T-01 | `a1b2c3d` | Scaffold API + route | ✅ |
| T-02 | `e4f5g6h` | Review summary generator | ✅ |
| T-03 | `i7j8k9l` | Error handling + rollback | ✅ |
| T-04 | `m0n1o2p` | Observability (duration log) | ✅ |
| T-05 | `q3r4s5t` | Self-review vs spec (self-critique) | ✅ |
| T-06 | `u6v7w8x` | PR opened with traceability | ✅ |

## AI usage
- Prompt templates used: **Yes** (`review_summary.prompt`, `self_critique.prompt`)
- Structured output enforced: **Yes** (JSON schema)
- Self-reviewed against spec acceptance criteria: **Yes** (T-05, self-critique pass)
- Self-critique applied: **Yes** (ungrounded findings filtered)

## Reviewer notes (TL pre-PR review)
[TL to complete]

- [x] Acceptance criteria are testable, not just descriptive
- [x] No missing edge cases in spec (empty diff, no files)
- [x] NFRs defined (performance < 2s, security no secrets echoed, observability)
- [x] AI-generated code self-reviewed before PR
- [x] Traceability visible (spec → task → code → PR)

## Definition of Done
- [x] All ACs have passing tests (AC-01…AC-04)
- [x] Spec artifact committed
- [x] Traceability visible
- [x] TL pre-PR review complete

## Demo narrative (for Mentor review)
1. Show spec file with ACs + NFRs + task breakdown.
2. Run `python -m src.main.review_summary` with a sample diff.
3. Show structured JSON output + markdown rendering.
4. Run `pytest` — all 12 tests pass, including grounded-finding assertions.
5. Show prompt templates used (few-shot + structured output + self-critique).
6. Explain how this shows up in real project work: PR reviews on the team repo
   now use this generator as a first-pass AI reviewer, with TL doing pre-PR
   review of teammates' AI-assisted output.
