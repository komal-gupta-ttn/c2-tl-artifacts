package com.ttn.c2.artifact1.model;

/**
 * A single finding in the AI-generated code review summary.
 *
 * Mirrors the structured-output JSON schema used by the prompt template
 * (review_summary.prompt): category, severity, file, line, message, suggestion, grounded.
 */
public class ReviewFinding {

    private String category;          // security | performance | style | test-coverage
    private String severity;          // high | medium | low
    private String file;
    private Integer line;
    private String message;
    private String suggestion;
    private boolean grounded;         // true if finding references an actual diff line

    public ReviewFinding() {}

    public ReviewFinding(String category, String severity, String file,
                         Integer line, String message, String suggestion, boolean grounded) {
        this.category = category;
        this.severity = severity;
        this.file = file;
        this.line = line;
        this.message = message;
        this.suggestion = suggestion;
        this.grounded = grounded;
    }

    // Getters & setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
    public Integer getLine() { return line; }
    public void setLine(Integer line) { this.line = line; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
}
