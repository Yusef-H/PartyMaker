package com.example.partymaker.utils.security.monitoring;

/** Represents a security issue found during scanning */
public class SecurityIssue {
  public enum Severity {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String displayName;

    Severity(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  private final Severity severity;
  private final String title;
  private final String description;
  private String recommendation;
  private String category;

  public SecurityIssue(Severity severity, String title, String description) {
    this.severity = severity;
    this.title = title;
    this.description = description;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getRecommendation() {
    return recommendation;
  }

  public void setRecommendation(String recommendation) {
    this.recommendation = recommendation;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public String toString() {
    return String.format("[%s] %s: %s", severity.getDisplayName(), title, description);
  }
}
