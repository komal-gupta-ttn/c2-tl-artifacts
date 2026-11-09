"""
Unit + integration tests for ReviewSummaryGenerator.

Maps to acceptance criteria:
  AC-01: Review summary generated from PR diff (structured output)
  AC-02: Invalid/malformed diff returns error
  AC-03: Review completes within 2s for diffs < 500 lines
  AC-04: Findings grounded in actual diff lines
"""

import time
import pytest

from src.main.review_summary import ReviewSummaryGenerator, ReviewFinding


class TestAC01_StructuredOutput:
    def test_generates_review_summary_with_findings(self):
        gen = ReviewSummaryGenerator()
        diff = """diff --git a/src/auth.py b/src/auth.py
new file mode 100644
--- /dev/null
+++ b/src/auth.py
@@ -0,0 +1,3 @@
+password = "hardcoded_secret"
+def login(user):
+    return True
"""
        summary = gen.generate("PR-001", diff)

        assert summary.pr_id == "PR-001"
        assert summary.files_changed == 1
        assert isinstance(summary.findings, list)
        assert len(summary.findings) > 0
        assert summary.summary_md
        assert isinstance(summary.duration_ms, int)
        assert "+00:00" in summary.generated_at or summary.generated_at.endswith("Z")

    def test_finding_has_required_fields(self):
        gen = ReviewSummaryGenerator()
        diff = """diff --git a/src/auth.py b/src/auth.py
@@ -0,0 +1,2 @@
+password = "sk_live_1234567890"
"""
        summary = gen.generate("PR-002", diff)
        f = summary.findings[0]

        assert f.category in ("security", "performance", "style", "test-coverage")
        assert f.severity in ("high", "medium", "low")
        assert isinstance(f.file, str)
        assert isinstance(f.message, str)
        assert isinstance(f.grounded, bool)


class TestAC02_InvalidInput:
    def test_empty_diff_raises(self):
        gen = ReviewSummaryGenerator()
        with pytest.raises(ValueError):
            gen.generate("PR-003", "")

    def test_whitespace_only_diff_raises(self):
        gen = ReviewSummaryGenerator()
        with pytest.raises(ValueError):
            gen.generate("PR-004", "   \n  \n")

    def test_no_files_raises(self):
        gen = ReviewSummaryGenerator()
        with pytest.raises(ValueError):
            gen.generate("PR-005", "no diff content here")


class TestAC03_Performance:
    def test_completes_within_2s_for_small_diff(self):
        gen = ReviewSummaryGenerator()
        # Build a diff < 500 lines
        lines = ["diff --git a/src/x.py b/src/x.py", "new file mode 100644"]
        for i in range(100):
            lines.append(f"+def func_{i}():\n+    pass\n")
        diff = "\n".join(lines)

        start = time.perf_counter()
        summary = gen.generate("PR-006", diff)
        elapsed = time.perf_counter() - start

        assert elapsed < 2.0, f"Took {elapsed:.2f}s, expected < 2.0s"
        assert summary.duration_ms < 2000


class TestAC04_GroundedFindings:
    def test_findings_reference_real_lines(self):
        gen = ReviewSummaryGenerator()
        diff = """diff --git a/src/auth.py b/src/auth.py
@@ -0,0 +1,3 @@
+password = "sk_live_1234567890"
+def login(user):
+    return True
"""
        summary = gen.generate("PR-007", diff)

        # Every finding must be grounded (reference a real diff line)
        for f in summary.findings:
            assert f.grounded is True, f"Ungrounded finding: {f.file}:{f.line} — {f.message}"

    def test_grounded_count_matches(self):
        gen = ReviewSummaryGenerator()
        diff = """diff --git a/src/auth.py b/src/auth.py
@@ -0,0 +1,2 @@
+password = "secret"
"""
        summary = gen.generate("PR-008", diff)
        assert summary.grounded_count == summary.total_findings


class TestSelfReview:
    def test_no_hallucinated_file_paths(self):
        gen = ReviewSummaryGenerator()
        diff = """diff --git a/src/auth.py b/src/auth.py
@@ -0,0 +1,2 @@
+password = "secret"
"""
        summary = gen.generate("PR-009", diff)
        for f in summary.findings:
            assert f.file == "src/auth.py", f"Unexpected file: {f.file}"

    def test_dedupes_findings(self):
        gen = ReviewSummaryGenerator()
        # Repeated identical lines should not produce duplicate findings
        diff = "\n".join([f"+password = \"secret_{i}\"" for i in range(20)])
        diff = "diff --git a/src/a.py b/src/a.py\n@@ -0,0 +1,20 @@\n" + diff
        summary = gen.generate("PR-010", diff)
        # All should be unique category+file+line+message combos
        keys = [(f.category, f.file, f.line, f.message) for f in summary.findings]
        assert len(keys) == len(set(keys)), "Duplicate findings found"
