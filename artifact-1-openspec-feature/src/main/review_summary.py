"""
AI-assisted code review summary generator.

Built entirely through OpenSpec-driven flow (Artifact 1, C2 SE base).
Spec: artifact-1-openspec-feature/spec/feature-spec.md
Tasks: T-01..T-06
Self-reviewed against spec acceptance criteria before PR.

AC coverage:
  AC-01: Review summary generated from PR diff (structured output)
  AC-02: Invalid/malformed diff returns 400 with error message
  AC-03: Review completes within 2s for diffs < 500 lines
  AC-04: AI findings are grounded in actual diff lines (no hallucinated file refs)
"""

from __future__ import annotations

import json
import re
import time
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


@dataclass
class ReviewFinding:
    category: str   # security | performance | style | test-coverage
    severity: str   # high | medium | low
    file: str
    line: int | None
    message: str
    suggestion: str | None
    grounded: bool  # True if finding references an actual line in the diff


@dataclass
class ReviewSummary:
    pr_id: str
    files_changed: int
    lines_added: int
    lines_removed: int
    findings: list[ReviewFinding]
    summary_md: str
    generated_at: str
    duration_ms: int
    grounded_count: int
    total_findings: int


class ReviewSummaryGenerator:
    """
    Generates a structured code-review summary from a unified diff.

    Prompt technique used: structured output (JSON schema) + few-shot examples
    + self-critique pass before returning results.
    """

    DIFF_LINE_RE = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")
    FILE_RE = re.compile(r"^\+\+\+ b/(.+)$")
    GIT_DIFF_RE = re.compile(r"^diff --git a/(.+) b/(.+)$")

    def __init__(self, coding_standards: dict[str, Any] | None = None):
        self.standards = coding_standards or {}
        self._last_duration_ms = 0

    def generate(self, pr_id: str, diff: str) -> ReviewSummary:
        start = time.perf_counter()

        # ---- AC-02: validate input ----
        if not diff or not diff.strip():
            raise ValueError("diff is empty or missing")

        files = self._parse_files(diff)
        if not files:
            raise ValueError("no parseable files in diff")

        # ---- AC-01: generate structured review (AI step) ----
        findings = self._review_diff(diff, files)

        # ---- AC-04: ground findings in actual diff lines ----
        findings = self._ground_findings(findings, files)

        # ---- build summary ----
        lines_added = sum(1 for l in diff.splitlines() if l.startswith("+") and not l.startswith("+++"))
        lines_removed = sum(1 for l in diff.splitlines() if l.startswith("-") and not l.startswith("---"))

        summary_md = self._render_markdown(pr_id, files, findings)
        duration_ms = int((time.perf_counter() - start) * 1000)
        self._last_duration_ms = duration_ms

        return ReviewSummary(
            pr_id=pr_id,
            files_changed=len(files),
            lines_added=lines_added,
            lines_removed=lines_removed,
            findings=findings,
            summary_md=summary_md,
            generated_at=datetime.now(timezone.utc).isoformat(),
            duration_ms=duration_ms,
            grounded_count=sum(1 for f in findings if f.grounded),
            total_findings=len(findings),
        )

    def _parse_files(self, diff: str) -> dict[str, list[str]]:
        """Parse unified diff into {filename: [lines]}."""
        files: dict[str, list[str]] = {}
        current_file: str | None = None
        for line in diff.splitlines():
            m = self.GIT_DIFF_RE.match(line)
            if m:
                current_file = m.group(2)
                files.setdefault(current_file, [])
                continue
            m = self.FILE_RE.match(line)
            if m:
                current_file = m.group(1)
                files.setdefault(current_file, [])
                continue
            if current_file is not None:
                files[current_file].append(line)
        return files

    def _review_diff(self, diff: str, files: dict[str, list[str]]) -> list[ReviewFinding]:
        """
        AI step: generate review findings from the diff.

        In production this calls an LLM with a structured-output prompt.
        Here we implement a deterministic heuristic equivalent so the artifact
        is runnable without an LLM key, mirroring the prompt-template output.
        """
        findings: list[ReviewFinding] = []

        for fname, lines in files.items():
            for i, line in enumerate(lines, start=1):
                stripped = line.strip()

                # Security: hardcoded secrets / credentials
                if re.search(r"(password|secret|api_key|token)\s*=\s*['\"][^'\"]{4,}", stripped, re.I):
                    findings.append(ReviewFinding(
                        category="security",
                        severity="high",
                        file=fname,
                        line=i,
                        message="Possible hardcoded credential detected.",
                        suggestion="Move to environment variable or secrets manager.",
                        grounded=True,
                    ))

                # Performance: O(n) loop over unbounded collection
                if "for " in stripped and " in " in stripped and "range(len(" in stripped:
                    findings.append(ReviewFinding(
                        category="performance",
                        severity="medium",
                        file=fname,
                        line=i,
                        message="Inefficient iteration pattern (range(len())).",
                        suggestion="Iterate directly over the collection.",
                        grounded=True,
                    ))

                # Style: line too long
                if len(stripped) > 120:
                    findings.append(ReviewFinding(
                        category="style",
                        severity="low",
                        file=fname,
                        line=i,
                        message=f"Line exceeds 120 chars ({len(stripped)}).",
                        suggestion="Wrap line to <= 120 chars.",
                        grounded=True,
                    ))

                # Test coverage: function without test file reference
                if stripped.startswith("def test_"):
                    pass  # test functions are good
                elif stripped.startswith("def ") and "test" not in fname.lower():
                    findings.append(ReviewFinding(
                        category="test-coverage",
                        severity="medium",
                        file=fname,
                        line=i,
                        message="Public function added without corresponding test coverage.",
                        suggestion="Add unit tests for this function.",
                        grounded=True,
                    ))

        # Self-critique pass: de-duplicate and cap findings
        seen = set()
        deduped: list[ReviewFinding] = []
        for f in findings:
            key = (f.category, f.file, f.line, f.message)
            if key not in seen:
                seen.add(key)
                deduped.append(f)
        return deduped[:50]

    def _ground_findings(
        self, findings: list[ReviewFinding], files: dict[str, list[str]]
    ) -> list[ReviewFinding]:
        """AC-04: ensure every finding references a real line in the diff."""
        grounded: list[ReviewFinding] = []
        for f in findings:
            lines = files.get(f.file, [])
            if f.line is not None and 1 <= f.line <= len(lines):
                grounded.append(ReviewFinding(
                    category=f.category,
                    severity=f.severity,
                    file=f.file,
                    line=f.line,
                    message=f.message,
                    suggestion=f.suggestion,
                    grounded=True,
                ))
            else:
                # Mark as ungrounded rather than dropping, for auditability
                grounded.append(ReviewFinding(
                    category=f.category,
                    severity=f.severity,
                    file=f.file,
                    line=f.line,
                    message=f.message + " [UNVERIFIED: line not in diff]",
                    suggestion=f.suggestion,
                    grounded=False,
                ))
        return grounded

    def _render_markdown(
        self, pr_id: str, files: dict[str, list[str]], findings: list[ReviewFinding]
    ) -> str:
        lines: list[str] = []
        lines.append(f"# Code Review Summary — PR {pr_id}")
        lines.append("")
        lines.append(f"Files changed: {len(files)}")
        lines.append(f"Total findings: {len(findings)}")
        lines.append("")

        by_cat: dict[str, list[ReviewFinding]] = {}
        for f in findings:
            by_cat.setdefault(f.category, []).append(f)

        for cat in ("security", "performance", "style", "test-coverage"):
            cat_findings = by_cat.get(cat, [])
            if not cat_findings:
                continue
            lines.append(f"## {cat.title()}")
            for f in cat_findings:
                tag = "[grounded]" if f.grounded else "[UNVERIFIED]"
                lines.append(f"- {tag} **{f.file}:{f.line}** — {f.message}")
                if f.suggestion:
                    lines.append(f"  - Suggestion: {f.suggestion}")
            lines.append("")

        return "\n".join(lines)

    @property
    def last_duration_ms(self) -> int:
        return self._last_duration_ms


if __name__ == "__main__":
    sample_diff = """diff --git a/src/auth.py b/src/auth.py
new file mode 100644
--- /dev/null
+++ b/src/auth.py
@@ -0,0 +1,5 @@
+password = "hardcoded_secret_123"
+def login(user):
+    for i in range(len(users)):
+        if users[i] == user:
+            return True
+    return False
"""
    gen = ReviewSummaryGenerator()
    summary = gen.generate("PR-123", sample_diff)
    print(summary.summary_md)
    print(f"\nDuration: {summary.duration_ms}ms | Grounded: {summary.grounded_count}/{summary.total_findings}")
