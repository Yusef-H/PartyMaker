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

  private String id;
  private String type;
  private String severity;
  private String title;
  private String description;
  private String recommendation;
  private String category;

  public SecurityIssue() {
    // Default constructor
  }

  public SecurityIssue(Severity severityEnum, String title, String description) {
    this.severity = severityEnum.name();
    this.title = title;
    this.description = description;
  }

  public SecurityIssue(
      Severity severityEnum, String title, String description, String recommendation) {
    this.severity = severityEnum.name();
    this.title = title;
    this.description = description;
    this.recommendation = recommendation;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSeverity() {
    return severity;
  }

  public Severity getSeverityEnum() {
    if (severity == null) return Severity.LOW;
    try {
      return Severity.valueOf(severity.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Severity.LOW;
    }
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
    return String.format(
        "[%s] %s: %s",
        severity != null ? severity : "UNKNOWN", title != null ? title : type, description);
  }
}
