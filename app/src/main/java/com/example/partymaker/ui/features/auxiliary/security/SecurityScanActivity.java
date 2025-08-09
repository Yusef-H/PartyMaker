package com.example.partymaker.ui.features.auxiliary.security;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.partymaker.R;
import com.example.partymaker.utils.security.monitoring.SecurityAgent;
import com.example.partymaker.utils.security.monitoring.SecurityReport;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Activity for running security scans */
public class SecurityScanActivity extends AppCompatActivity {

  // Constants
  private static final String TAG = "SecurityScanActivity";
  private static final String FILE_PROVIDER_AUTHORITY = ".fileprovider";
  private static final String REPORTS_DIRECTORY = "security_reports";
  private static final String TEMP_REPORTS_DIRECTORY = "reports";
  private static final String HTML_FILE_PREFIX = "security_report_";
  private static final String JSON_FILE_PREFIX = "security_report_";
  private static final String HTML_EXTENSION = ".html";
  private static final String JSON_EXTENSION = ".json";
  private static final String TEMP_HTML_FILE = "security_report.html";
  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd_HHmmss";

  // UI text constants
  private static final String RUNNING_SCAN_TEXT = "Running security scan...";
  private static final String SCAN_COMPLETED_TEXT = "Scan completed successfully";
  private static final String SCAN_FAILED_TEXT = "Scan failed: %s";
  private static final String NO_SECURITY_ISSUES_TEXT = "No security issues found";
  private static final String SECURITY_ISSUES_FORMAT = "%d security issues found";
  private static final String GRADE_FORMAT = "Grade: %s";
  private static final String SCORE_FORMAT = "%d/100";
  private static final String NO_REPORT_TEXT = "No report to export";
  private static final String NO_REPORT_SHARE_TEXT = "No report to share";
  private static final String EXPORT_SUCCESS_FORMAT = "Reports exported to:\n%s";
  private static final String EXPORT_FAILED_FORMAT = "Failed to export report: %s";
  private static final String SHARE_FAILED_FORMAT = "Failed to share report: %s";
  private static final String SECURITY_SCORE_TEXT = "Security Score: %d/100\nGrade: %s";
  private static final String SHARE_SUBJECT = "PartyMaker Security Report";
  private static final String SHARE_CHOOSER_TITLE = "Share Security Report";

  // UI Components
  private TextView securityScoreText;
  private TextView securityGradeText;
  private TextView issuesFoundText;
  private TextView scanStatusText;
  private Button runScanButton;
  private Button exportReportButton;
  private Button shareReportButton;
  private ProgressBar progressBar;

  // Data
  private SecurityAgent securityAgent;
  private SecurityReport currentReport;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_security_scan);

    initializeViews();
    setupListeners();

    securityAgent = SecurityAgent.getInstance(this);
  }

  private void initializeViews() {
    securityScoreText = findViewById(R.id.tv_security_score);
    securityGradeText = findViewById(R.id.tv_security_grade);
    issuesFoundText = findViewById(R.id.tv_issues_found);
    scanStatusText = findViewById(R.id.tv_scan_status);
    runScanButton = findViewById(R.id.btn_run_scan);
    exportReportButton = findViewById(R.id.btn_export_report);
    shareReportButton = findViewById(R.id.btn_share_report);
    progressBar = findViewById(R.id.progress_bar);

    // Initially hide export/share buttons
    exportReportButton.setVisibility(View.GONE);
    shareReportButton.setVisibility(View.GONE);
  }

  private void setupListeners() {
    runScanButton.setOnClickListener(v -> runSecurityScan());
    exportReportButton.setOnClickListener(v -> exportReport());
    shareReportButton.setOnClickListener(v -> shareReport());
  }

  private void runSecurityScan() {
    showScanInProgress();

    long startTime = System.currentTimeMillis();

    securityAgent
        .performSecurityScan()
        .thenAccept(report -> runOnUiThread(() -> handleScanSuccess(report, startTime)))
        .exceptionally(
            throwable -> {
              runOnUiThread(() -> handleScanFailure(throwable));
              return null;
            });
  }

  private void showScanInProgress() {
    progressBar.setVisibility(View.VISIBLE);
    runScanButton.setEnabled(false);
    scanStatusText.setText(RUNNING_SCAN_TEXT);
  }

  private void handleScanSuccess(SecurityReport report, long startTime) {
    long duration = System.currentTimeMillis() - startTime;
    report.setScanDuration(duration + " ms");

    currentReport = report;
    displayResults(report);
    showScanCompleted();
  }

  private void handleScanFailure(Throwable throwable) {
    progressBar.setVisibility(View.GONE);
    runScanButton.setEnabled(true);
    scanStatusText.setText(String.format(SCAN_FAILED_TEXT, throwable.getMessage()));
    Toast.makeText(this, "Security scan failed", Toast.LENGTH_LONG).show();
  }

  private void showScanCompleted() {
    progressBar.setVisibility(View.GONE);
    runScanButton.setEnabled(true);
    exportReportButton.setVisibility(View.VISIBLE);
    shareReportButton.setVisibility(View.VISIBLE);
    scanStatusText.setText(SCAN_COMPLETED_TEXT);
  }

  private void displayResults(SecurityReport report) {
    displaySecurityScore(report);
    displaySecurityGrade(report);
    displayIssuesSummary(report);
    displayDetailedIssues(report);
  }

  private void displaySecurityScore(SecurityReport report) {
    securityScoreText.setText(
        String.format(Locale.getDefault(), SCORE_FORMAT, report.getOverallScore()));
  }

  private void displaySecurityGrade(SecurityReport report) {
    securityGradeText.setText(String.format(GRADE_FORMAT, report.getSecurityGrade()));
  }

  private void displayIssuesSummary(SecurityReport report) {
    int totalIssues = report.getSecurityIssues().size();
    if (totalIssues == 0) {
      issuesFoundText.setText(NO_SECURITY_ISSUES_TEXT);
      issuesFoundText.setTextColor(getColor(android.R.color.holo_green_dark));
    } else {
      issuesFoundText.setText(
          String.format(Locale.getDefault(), SECURITY_ISSUES_FORMAT, totalIssues));
      issuesFoundText.setTextColor(getColor(android.R.color.holo_red_dark));
    }
  }

  private void displayDetailedIssues(SecurityReport report) {
    // For now, detailed issues are only shown in exported reports
    // UI only shows the summary count
  }

  private void exportReport() {
    if (currentReport == null) {
      Toast.makeText(this, NO_REPORT_TEXT, Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      File reportsDir = createReportsDirectory();
      String timestamp = generateTimestamp();

      exportHtmlReport(reportsDir, timestamp);
      exportJsonReport(reportsDir, timestamp);

      Toast.makeText(
              this, String.format(EXPORT_SUCCESS_FORMAT, reportsDir.getPath()), Toast.LENGTH_LONG)
          .show();

      // Also upload to Firebase
      securityAgent.uploadReportToFirebase(currentReport);

    } catch (Exception e) {
      Toast.makeText(this, String.format(EXPORT_FAILED_FORMAT, e.getMessage()), Toast.LENGTH_LONG)
          .show();
    }
  }

  private File createReportsDirectory() {
    File reportsDir = new File(getExternalFilesDir(null), REPORTS_DIRECTORY);
    if (!reportsDir.exists()) {
      if (!reportsDir.mkdirs()) {
        Log.e(TAG, "Failed to create reports directory: " + reportsDir.getAbsolutePath());
      }
    }
    return reportsDir;
  }

  private String generateTimestamp() {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
    return sdf.format(new Date());
  }

  private void exportHtmlReport(File reportsDir, String timestamp) throws Exception {
    File htmlFile = new File(reportsDir, HTML_FILE_PREFIX + timestamp + HTML_EXTENSION);
    try (FileWriter htmlWriter = new FileWriter(htmlFile)) {
      htmlWriter.write(currentReport.toHTML());
    }
  }

  private void exportJsonReport(File reportsDir, String timestamp) throws Exception {
    File jsonFile = new File(reportsDir, JSON_FILE_PREFIX + timestamp + JSON_EXTENSION);
    try (FileWriter jsonWriter = new FileWriter(jsonFile)) {
      jsonWriter.write(currentReport.toJSON());
    }
  }

  private void shareReport() {
    if (currentReport == null) {
      Toast.makeText(this, NO_REPORT_SHARE_TEXT, Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      File tempFile = createTempReportFile();
      Uri fileUri = createFileUri(tempFile);
      Intent shareIntent = createShareIntent(fileUri);
      startActivity(Intent.createChooser(shareIntent, SHARE_CHOOSER_TITLE));

    } catch (Exception e) {
      Toast.makeText(this, String.format(SHARE_FAILED_FORMAT, e.getMessage()), Toast.LENGTH_LONG)
          .show();
    }
  }

  private File createTempReportFile() throws Exception {
    File tempDir = new File(getCacheDir(), TEMP_REPORTS_DIRECTORY);
    if (!tempDir.exists()) {
      if (!tempDir.mkdirs()) {
        Log.e(TAG, "Failed to create temp directory: " + tempDir.getAbsolutePath());
      }
    }

    File tempFile = new File(tempDir, TEMP_HTML_FILE);
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(currentReport.toHTML());
    }
    return tempFile;
  }

  private Uri createFileUri(File tempFile) {
    return FileProvider.getUriForFile(this, getPackageName() + FILE_PROVIDER_AUTHORITY, tempFile);
  }

  private Intent createShareIntent(Uri fileUri) {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/html");
    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
    shareIntent.putExtra(Intent.EXTRA_TEXT, createShareText());
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    return shareIntent;
  }

  private String createShareText() {
    return String.format(
        SECURITY_SCORE_TEXT, currentReport.getOverallScore(), currentReport.getSecurityGrade());
  }
}
