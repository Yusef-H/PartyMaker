package com.example.partymaker.ui.features.auxiliary.security;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
  private TextView tvSecurityScore;
  private TextView tvSecurityGrade;
  private TextView tvIssuesFound;
  private TextView tvScanStatus;
  private Button btnRunScan;
  private Button btnExportReport;
  private Button btnShareReport;
  private ProgressBar progressBar;

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
    tvSecurityScore = findViewById(R.id.tv_security_score);
    tvSecurityGrade = findViewById(R.id.tv_security_grade);
    tvIssuesFound = findViewById(R.id.tv_issues_found);
    tvScanStatus = findViewById(R.id.tv_scan_status);
    btnRunScan = findViewById(R.id.btn_run_scan);
    btnExportReport = findViewById(R.id.btn_export_report);
    btnShareReport = findViewById(R.id.btn_share_report);
    progressBar = findViewById(R.id.progress_bar);

    // Initially hide export/share buttons
    btnExportReport.setVisibility(View.GONE);
    btnShareReport.setVisibility(View.GONE);
  }

  private void setupListeners() {
    btnRunScan.setOnClickListener(v -> runSecurityScan());
    btnExportReport.setOnClickListener(v -> exportReport());
    btnShareReport.setOnClickListener(v -> shareReport());
  }

  private void runSecurityScan() {
    // Show progress
    progressBar.setVisibility(View.VISIBLE);
    btnRunScan.setEnabled(false);
    tvScanStatus.setText(getString(R.string.running_security_scan));

    // Start scan
    long startTime = System.currentTimeMillis();

    securityAgent
        .performSecurityScan()
        .thenAccept(
            report ->
                runOnUiThread(
                    () -> {
                      long duration = System.currentTimeMillis() - startTime;
                      report.setScanDuration(duration + " ms");

                      currentReport = report;
                      displayResults(report);

                      // Hide progress and enable buttons
                      progressBar.setVisibility(View.GONE);
                      btnRunScan.setEnabled(true);
                      btnExportReport.setVisibility(View.VISIBLE);
                      btnShareReport.setVisibility(View.VISIBLE);

                      tvScanStatus.setText(getString(R.string.scan_completed_successfully));
                    }))
        .exceptionally(
            throwable -> {
              runOnUiThread(
                  () -> {
                    progressBar.setVisibility(View.GONE);
                    btnRunScan.setEnabled(true);
                    tvScanStatus.setText(getString(R.string.scan_failed, throwable.getMessage()));
                    Toast.makeText(this, "Security scan failed", Toast.LENGTH_LONG).show();
                  });
              return null;
            });
  }

  private void displayResults(SecurityReport report) {
    // Display score
    tvSecurityScore.setText(String.format(Locale.getDefault(), "%d/100", report.getOverallScore()));
    tvSecurityGrade.setText(getString(R.string.grade_with_value, report.getSecurityGrade()));

    // Display issues summary
    int totalIssues = report.getSecurityIssues().size();
    if (totalIssues == 0) {
      tvIssuesFound.setText(getString(R.string.no_security_issues_found));
      tvIssuesFound.setTextColor(getColor(android.R.color.holo_green_dark));
    } else {
      tvIssuesFound.setText(
          String.format(Locale.getDefault(), "%d security issues found", totalIssues));
      tvIssuesFound.setTextColor(getColor(android.R.color.holo_red_dark));
    }

    // TODO: Add a RecyclerView to display detailed issues
  }

  private void exportReport() {
    if (currentReport == null) {
      Toast.makeText(this, "No report to export", Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      // Create reports directory
      File reportsDir = new File(getExternalFilesDir(null), "security_reports");
      if (!reportsDir.exists()) {
        reportsDir.mkdirs();
      }

      // Generate filename with timestamp
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
      String timestamp = sdf.format(new Date());

      // Export as HTML
      File htmlFile = new File(reportsDir, "security_report_" + timestamp + ".html");
      FileWriter htmlWriter = new FileWriter(htmlFile);
      htmlWriter.write(currentReport.toHTML());
      htmlWriter.close();

      // Export as JSON
      File jsonFile = new File(reportsDir, "security_report_" + timestamp + ".json");
      FileWriter jsonWriter = new FileWriter(jsonFile);
      jsonWriter.write(currentReport.toJSON());
      jsonWriter.close();

      Toast.makeText(this, "Reports exported to:\n" + reportsDir.getPath(), Toast.LENGTH_LONG)
          .show();

      // Also upload to Firebase
      securityAgent.uploadReportToFirebase(currentReport);

    } catch (Exception e) {
      Toast.makeText(this, "Failed to export report: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void shareReport() {
    if (currentReport == null) {
      Toast.makeText(this, "No report to share", Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      // Create temporary file
      File tempDir = new File(getCacheDir(), "reports");
      if (!tempDir.exists()) {
        tempDir.mkdirs();
      }

      File tempFile = new File(tempDir, "security_report.html");
      FileWriter writer = new FileWriter(tempFile);
      writer.write(currentReport.toHTML());
      writer.close();

      // Share via intent
      Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/html");
      shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PartyMaker Security Report");
      shareIntent.putExtra(
          Intent.EXTRA_TEXT,
          "Security Score: "
              + currentReport.getOverallScore()
              + "/100\n"
              + "Grade: "
              + currentReport.getSecurityGrade());
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      startActivity(Intent.createChooser(shareIntent, "Share Security Report"));

    } catch (Exception e) {
      Toast.makeText(this, "Failed to share report: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }
}
