package com.example.partymaker.utils.security.monitoring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Security report containing scan results */
public class SecurityReport {
  private long timestamp;
  private Map<String, String> deviceInfo;
  private Map<String, String> appInfo;
  private List<SecurityIssue> issues;
  private List<SecurityIssue> securityIssues;
  private int overallScore;
  private String scanDuration;
  private int totalIssues;
  private int criticalIssues;
  private int highIssues;
  private int mediumIssues;
  private int lowIssues;
  private List<String> recommendations;

  public SecurityReport() {
    this.deviceInfo = new HashMap<>();
    this.appInfo = new HashMap<>();
  }

  // Getters and Setters
  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public List<SecurityIssue> getIssues() {
    return issues;
  }

  public void setIssues(List<SecurityIssue> issues) {
    this.issues = issues;
    this.securityIssues = issues; // Keep both for compatibility
  }

  public int getTotalIssues() {
    return totalIssues;
  }

  public void setTotalIssues(int totalIssues) {
    this.totalIssues = totalIssues;
  }

  public int getCriticalIssues() {
    return criticalIssues;
  }

  public void setCriticalIssues(int criticalIssues) {
    this.criticalIssues = criticalIssues;
  }

  public int getHighIssues() {
    return highIssues;
  }

  public void setHighIssues(int highIssues) {
    this.highIssues = highIssues;
  }

  public int getMediumIssues() {
    return mediumIssues;
  }

  public void setMediumIssues(int mediumIssues) {
    this.mediumIssues = mediumIssues;
  }

  public int getLowIssues() {
    return lowIssues;
  }

  public void setLowIssues(int lowIssues) {
    this.lowIssues = lowIssues;
  }

  public List<String> getRecommendations() {
    return recommendations;
  }

  public void setRecommendations(List<String> recommendations) {
    this.recommendations = recommendations;
  }

  public Map<String, String> getDeviceInfo() {
    return deviceInfo;
  }

  public void setDeviceInfo(Map<String, String> deviceInfo) {
    this.deviceInfo = deviceInfo;
  }

  public Map<String, String> getAppInfo() {
    return appInfo;
  }

  public void setAppInfo(Map<String, String> appInfo) {
    this.appInfo = appInfo;
  }

  public List<SecurityIssue> getSecurityIssues() {
    return securityIssues;
  }

  public void setSecurityIssues(List<SecurityIssue> securityIssues) {
    this.securityIssues = securityIssues;
  }

  public int getOverallScore() {
    return overallScore;
  }

  public void setOverallScore(int overallScore) {
    this.overallScore = overallScore;
  }

  public String getScanDuration() {
    return scanDuration;
  }

  public void setScanDuration(String scanDuration) {
    this.scanDuration = scanDuration;
  }

  /** Get security grade based on score */
  public String getSecurityGrade() {
    if (overallScore >= 90) return "A";
    if (overallScore >= 80) return "B";
    if (overallScore >= 70) return "C";
    if (overallScore >= 60) return "D";
    return "F";
  }

  /** Count issues by severity */
  public Map<SecurityIssue.Severity, Integer> getIssueCountBySeverity() {
    Map<SecurityIssue.Severity, Integer> counts = new HashMap<>();
    for (SecurityIssue.Severity severity : SecurityIssue.Severity.values()) {
      counts.put(severity, 0);
    }

    for (SecurityIssue issue : securityIssues) {
        counts.compute(issue.getSeverityEnum(), (k, currentCount) -> (currentCount != null ? currentCount : 0) + 1);
    }

    return counts;
  }

  /** Convert report to JSON */
  public String toJSON() {
    Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    return gson.toJson(this);
  }

  /** Convert report to HTML */
  public String toHTML() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html>\n");
    html.append("<html>\n<head>\n");
    html.append("<title>Security Report - PartyMaker</title>\n");
    html.append("<style>\n");
    html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
    html.append("h1, h2, h3 { color: #333; }\n");
    html.append(".high { color: #d9534f; }\n");
    html.append(".medium { color: #f0ad4e; }\n");
    html.append(".low { color: #5bc0de; }\n");
    html.append(".score { font-size: 48px; font-weight: bold; }\n");
    html.append(".grade { font-size: 36px; margin-left: 20px; }\n");
    html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
    html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
    html.append("th { background-color: #f2f2f2; }\n");
    html.append("</style>\n</head>\n<body>\n");

    // Header
    html.append("<h1>Security Report - PartyMaker Application</h1>\n");
    html.append("<p>Generated on: ").append(sdf.format(timestamp)).append("</p>\n");

    // Overall Score
    html.append("<h2>Overall Security Score</h2>\n");
    html.append("<div class='score'>").append(overallScore).append("/100");
    html.append("<span class='grade'>Grade: ").append(getSecurityGrade()).append("</span></div>\n");

    // Summary
    html.append("<h2>Summary</h2>\n");
    Map<SecurityIssue.Severity, Integer> counts = getIssueCountBySeverity();
    html.append("<ul>\n");
    for (Map.Entry<SecurityIssue.Severity, Integer> entry : counts.entrySet()) {
      html.append("<li class='").append(entry.getKey().toString().toLowerCase()).append("'>");
      html.append(entry.getKey().getDisplayName()).append(": ").append(entry.getValue());
      html.append(" issues</li>\n");
    }
    html.append("</ul>\n");

    // Device Info
    html.append("<h2>Device Information</h2>\n");
    html.append("<table>\n");
    for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
      html.append("<tr><td>").append(entry.getKey()).append("</td>");
      html.append("<td>").append(entry.getValue()).append("</td></tr>\n");
    }
    html.append("</table>\n");

    // App Info
    html.append("<h2>Application Information</h2>\n");
    html.append("<table>\n");
    for (Map.Entry<String, String> entry : appInfo.entrySet()) {
      html.append("<tr><td>").append(entry.getKey()).append("</td>");
      html.append("<td>").append(entry.getValue()).append("</td></tr>\n");
    }
    html.append("</table>\n");

    // Security Issues
    html.append("<h2>Security Issues</h2>\n");
    if (securityIssues.isEmpty()) {
      html.append("<p>No security issues found!</p>\n");
    } else {
      html.append("<table>\n");
      html.append("<tr><th>Severity</th><th>Title</th><th>Description</th></tr>\n");
      for (SecurityIssue issue : securityIssues) {
        html.append("<tr class='")
            .append(issue.getSeverityEnum().toString().toLowerCase())
            .append("'>");
        html.append("<td>").append(issue.getSeverityEnum().getDisplayName()).append("</td>");
        html.append("<td>").append(issue.getTitle()).append("</td>");
        html.append("<td>").append(issue.getDescription()).append("</td>");
        html.append("</tr>\n");
      }
      html.append("</table>\n");
    }

    html.append("</body>\n</html>");
    return html.toString();
  }

  /** Convert report to Markdown */
  public String toMarkdown() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    StringBuilder md = new StringBuilder();

    // Header
    md.append("# Security Report - PartyMaker Application\n\n");
    md.append("**Generated on:** ").append(sdf.format(timestamp)).append("\n\n");

    // Overall Score
    md.append("## Overall Security Score\n\n");
    md.append("**Score:** ").append(overallScore).append("/100\n");
    md.append("**Grade:** ").append(getSecurityGrade()).append("\n\n");

    // Summary
    md.append("## Summary\n\n");
    Map<SecurityIssue.Severity, Integer> counts = getIssueCountBySeverity();
    for (Map.Entry<SecurityIssue.Severity, Integer> entry : counts.entrySet()) {
      md.append("- **").append(entry.getKey().getDisplayName()).append(":** ");
      md.append(entry.getValue()).append(" issues\n");
    }
    md.append("\n");

    // Device Info
    md.append("## Device Information\n\n");
    md.append("| Property | Value |\n");
    md.append("|----------|-------|\n");
    for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
      md.append("| ").append(entry.getKey()).append(" | ");
      md.append(entry.getValue()).append(" |\n");
    }
    md.append("\n");

    // App Info
    md.append("## Application Information\n\n");
    md.append("| Property | Value |\n");
    md.append("|----------|-------|\n");
    for (Map.Entry<String, String> entry : appInfo.entrySet()) {
      md.append("| ").append(entry.getKey()).append(" | ");
      md.append(entry.getValue()).append(" |\n");
    }
    md.append("\n");

    // Security Issues
    md.append("## Security Issues\n\n");
    if (securityIssues.isEmpty()) {
      md.append("No security issues found!\n");
    } else {
      md.append("| Severity | Title | Description |\n");
      md.append("|----------|-------|-------------|\n");
      for (SecurityIssue issue : securityIssues) {
        md.append("| ").append(issue.getSeverityEnum().getDisplayName());
        md.append(" | ").append(issue.getTitle());
        md.append(" | ").append(issue.getDescription()).append(" |\n");
      }
    }

    return md.toString();
  }

  /** Convert report to Map for Firebase */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("timestamp", timestamp);
    map.put("deviceInfo", deviceInfo);
    map.put("appInfo", appInfo);
    map.put("overallScore", overallScore);
    map.put("securityGrade", getSecurityGrade());
    map.put("scanDuration", scanDuration);

    // Convert issues to list of maps
    List<Map<String, Object>> issuesList = new java.util.ArrayList<>();
    for (SecurityIssue issue : securityIssues) {
      Map<String, Object> issueMap = new HashMap<>();
      issueMap.put("severity", issue.getSeverityEnum().toString());
      issueMap.put("title", issue.getTitle());
      issueMap.put("description", issue.getDescription());
      issueMap.put("recommendation", issue.getRecommendation());
      issueMap.put("category", issue.getCategory());
      issuesList.add(issueMap);
    }
    map.put("securityIssues", issuesList);

    return map;
  }
}
