package com.example.partymaker.utils.security.monitoring;

import android.content.Context;
import android.util.Log;

/** Example usage of the Security Agent */
public class SecurityAgentExample {
  private static final String TAG = "SecurityAgentExample";

  /** Example: Run a basic security scan */
  public static void runBasicScan(Context context) {
    SecurityAgent agent = SecurityAgent.getInstance(context);

    agent
        .performSecurityScan()
        .thenAccept(
            report -> {
              Log.d(TAG, "Security scan completed");
              Log.d(TAG, "Overall score: " + report.getOverallScore() + "/100");
              Log.d(TAG, "Security grade: " + report.getSecurityGrade());
              Log.d(TAG, "Issues found: " + report.getSecurityIssues().size());

              // Process each issue
              report
                  .getSecurityIssues()
                  .forEach(
                      issue -> Log.w(TAG, issue.toString()));
            })
        .exceptionally(
            throwable -> {
              Log.e(TAG, "Security scan failed", throwable);
              return null;
            });
  }

  /** Example: Export report to file */
  public static void exportReportExample(Context context) {
    SecurityAgent agent = SecurityAgent.getInstance(context);

    agent
        .performSecurityScan()
        .thenAccept(
            report -> {
              // Export as JSON
              String jsonReport = agent.exportReportAsJSON(report);
              Log.d(TAG, "JSON Report: " + jsonReport);

              // Export as HTML
              String htmlReport = agent.exportReportAsHTML(report);
              // Save to file or display in WebView

              // Export as Markdown
              String markdownReport = agent.exportReportAsMarkdown(report);
              // Use for documentation or sharing
            });
  }

  /** Example: Upload report to Firebase */
  public static void uploadReportExample(Context context) {
    SecurityAgent agent = SecurityAgent.getInstance(context);

    agent
        .performSecurityScan()
        .thenCompose(agent::uploadReportToFirebase)
        .thenRun(
            () -> Log.d(TAG, "Report uploaded to Firebase successfully"))
        .exceptionally(
            throwable -> {
              Log.e(TAG, "Failed to upload report", throwable);
              return null;
            });
  }

  /** Example: Schedule periodic security scans */
  public static void schedulePeriodicScans(Context context) {
    // This would typically be done with WorkManager
    // Example:
    /*
    PeriodicWorkRequest scanWork = new PeriodicWorkRequest.Builder(
        SecurityScanWorker.class,
        24, TimeUnit.HOURS)
        .build();

    WorkManager.getInstance(context).enqueue(scanWork);
    */
  }

  /** Example: Integration with existing app features */
  public static void integrateWithApp(Context context) {
    // 1. Add to settings menu
    // 2. Run on app startup (with user consent)
    // 3. Run before sensitive operations
    // 4. Include in crash reports
    // 5. Show security badge in app based on score

    SecurityAgent agent = SecurityAgent.getInstance(context);
    agent
        .performSecurityScan()
        .thenAccept(
            report -> {
              if (report.getOverallScore() < 60) {
                // Show security warning to user
                Log.w(TAG, "Low security score detected!");
              }
            });
  }
}
