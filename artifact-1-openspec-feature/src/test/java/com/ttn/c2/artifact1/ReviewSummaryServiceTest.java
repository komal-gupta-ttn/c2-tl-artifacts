package com.ttn.c2.artifact1;

import com.ttn.c2.artifact1.model.DiffInput;
import com.ttn.c2.artifact1.model.ReviewFinding;
import com.ttn.c2.artifact1.model.ReviewSummary;
import com.ttn.c2.artifact1.service.ReviewSummaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReviewSummaryServiceTest {

    private final ReviewSummaryService service = new ReviewSummaryService();

    private String smallDiff() {
        return "diff --git a/src/auth.py b/src/auth.py\n" +
               "new file mode 100644\n" +
               "--- /dev/null\n" +
               "+++ b/src/auth.py\n" +
               "@@ -0,0 +1,3 @@\n" +
               "+password = \"hardcoded_secret\"\n" +
               "+def login(user):\n" +
               "+    return True";
    }

    @Test
    @DisplayName("AC-01: generates structured review summary with findings")
    void ac01_structuredOutput() {
        ReviewSummary s = service.generate(new DiffInput("PR-001", smallDiff()));
        assertThat(s.getPrId()).isEqualTo("PR-001");
        assertThat(s.getFilesChanged()).isEqualTo(1);
        assertThat(s.getFindings()).isNotEmpty();
        assertThat(s.getSummaryMarkdown()).isNotBlank();
        assertThat(s.getGeneratedAt()).isNotNull();
    }

    @Test
    @DisplayName("AC-01: finding has required fields")
    void ac01_findingFields() {
        ReviewSummary s = service.generate(new DiffInput("PR-002", smallDiff()));
        ReviewFinding f = s.getFindings().get(0);
        assertThat(f.getCategory()).isIn("security", "performance", "style", "test-coverage");
        assertThat(f.getSeverity()).isIn("high", "medium", "low");
        assertThat(f.getFile()).isNotBlank();
        assertThat(f.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("AC-02: empty diff raises error")
    void ac02_emptyDiff() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.generate(new DiffInput("PR-003", "")));
    }

    @Test
    @DisplayName("AC-02: whitespace-only diff raises error")
    void ac02_whitespaceDiff() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.generate(new DiffInput("PR-004", "   \n")));
    }

    @Test
    @DisplayName("AC-03: completes within 2s for small diff")
    void ac03_performance() {
        long start = System.nanoTime();
        service.generate(new DiffInput("PR-006", smallDiff()));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(elapsedMs).isLessThan(2000);
    }

    @Test
    @DisplayName("AC-04: all findings grounded in real diff lines")
    void ac04_grounded() {
        ReviewSummary s = service.generate(new DiffInput("PR-007", smallDiff()));
        for (ReviewFinding f : s.getFindings()) {
            assertThat(f.isGrounded()).as("ungrounded: " + f.getMessage()).isTrue();
        }
        assertThat(s.getGroundedCount()).isEqualTo(s.getTotalFindings());
    }

    @Test
    @DisplayName("No hallucinated file paths")
    void noHallucinatedFiles() {
        ReviewSummary s = service.generate(new DiffInput("PR-009", smallDiff()));
        for (ReviewFinding f : s.getFindings()) {
            assertThat(f.getFile()).isEqualTo("src/auth.py");
        }
    }

    @Test
    @DisplayName("Spec tasks and traceability present")
    void specTraceability() {
        assertThat(service.getSpecTasks()).hasSize(6);
        assertThat(service.getTraceability()).hasSize(4);
    }
}
