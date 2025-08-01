package com.example.partymaker.utils.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Security Agent for PartyMaker Application Performs security scans, vulnerability checks, and
 * generates security reports
 */
public class SecurityAgent {
  private static final String TAG = "SecurityAgent";
  private static SecurityAgent instance;
  private final Context context;
  private final List<SecurityIssue> securityIssues;
  private final Map<String, SecurityCheck> securityChecks;

  private SecurityAgent(Context context) {
    this.context = context.getApplicationContext();
    this.securityIssues = new ArrayList<>();
    this.securityChecks = new HashMap<>();
    initializeSecurityChecks();
  }

  public static synchronized SecurityAgent getInstance(Context context) {
    if (instance == null) {
      instance = new SecurityAgent(context);
    }
    return instance;
  }

  /** Initialize all security checks */
  private void initializeSecurityChecks() {
    // App integrity checks
    securityChecks.put("app_signature", this::checkAppSignature);
    securityChecks.put("debug_mode", this::checkDebugMode);
    securityChecks.put("root_detection", this::checkRootDetection);

    // Network security checks
    securityChecks.put("ssl_pinning", this::checkSSLPinning);
    securityChecks.put("network_security", this::checkNetworkSecurity);

    // Data protection checks
    securityChecks.put("encryption", this::checkDataEncryption);
    securityChecks.put("secure_storage", this::checkSecureStorage);

    // Permission checks
    securityChecks.put("permissions", this::checkPermissions);

    // Firebase security checks
    securityChecks.put("firebase_rules", this::checkFirebaseRules);

    // Code obfuscation checks
    securityChecks.put("obfuscation", this::checkCodeObfuscation);
  }

  /** Perform comprehensive security scan */
  public CompletableFuture<SecurityReport> performSecurityScan() {
    return CompletableFuture.supplyAsync(
        () -> {
          securityIssues.clear();

          Log.d(TAG, "Starting security scan...");

          // Run all security checks
          for (Map.Entry<String, SecurityCheck> entry : securityChecks.entrySet()) {
            try {
              entry.getValue().check();
            } catch (Exception e) {
              Log.e(TAG, "Error during security check: " + entry.getKey(), e);
              addSecurityIssue(
                  new SecurityIssue(
                      SecurityIssue.Severity.MEDIUM,
                      "Security Check Failed",
                      "Failed to complete " + entry.getKey() + " check: " + e.getMessage()));
            }
          }

          Log.d(TAG, "Security scan completed. Found " + securityIssues.size() + " issues");

          return generateSecurityReport();
        });
  }

  /** Check app signature integrity */
  private void checkAppSignature() {
    try {
      PackageInfo packageInfo =
          context
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

      if (packageInfo.signatures == null || packageInfo.signatures.length == 0) {
        addSecurityIssue(
            new SecurityIssue(
                SecurityIssue.Severity.HIGH,
                "Missing App Signature",
                "Application signature not found. This could indicate tampering."));
      }

      // TODO: Compare with known good signature hash
    } catch (Exception e) {
      Log.e(TAG, "Error checking app signature", e);
    }
  }

  /** Check if app is running in debug mode */
  private void checkDebugMode() {
    if ((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)
        != 0) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.HIGH,
              "Debug Mode Enabled",
              "Application is running in debug mode. This should be disabled in production."));
    }
  }

  /** Check for root detection */
  private void checkRootDetection() {
    if (isDeviceRooted()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.HIGH,
              "Rooted Device Detected",
              "Device appears to be rooted. This poses significant security risks."));
    }
  }

  /** Check SSL pinning implementation */
  private void checkSSLPinning() {
    // TODO: Implement actual SSL pinning verification
    if (!isSSLPinningEnabled()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.HIGH,
              "SSL Pinning Not Implemented",
              "SSL certificate pinning is not enabled. This makes the app vulnerable to MITM attacks."));
    }
  }

  /** Check network security configuration */
  private void checkNetworkSecurity() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // Check if cleartext traffic is allowed
      if (isCleartextTrafficAllowed()) {
        addSecurityIssue(
            new SecurityIssue(
                SecurityIssue.Severity.MEDIUM,
                "Cleartext Traffic Allowed",
                "Application allows cleartext (non-HTTPS) network traffic."));
      }
    }
  }

  /** Check data encryption practices */
  private void checkDataEncryption() {
    // Check if sensitive data is encrypted
    if (!isDataEncrypted()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.HIGH,
              "Unencrypted Sensitive Data",
              "Sensitive data may not be properly encrypted in storage."));
    }
  }

  /** Check secure storage implementation */
  private void checkSecureStorage() {
    // Check SharedPreferences encryption
    if (!isSharedPreferencesEncrypted()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.MEDIUM,
              "Unencrypted SharedPreferences",
              "SharedPreferences are not encrypted. Consider using EncryptedSharedPreferences."));
    }
  }

  /** Check app permissions */
  private void checkPermissions() {
    try {
      PackageInfo packageInfo =
          context
              .getPackageManager()
              .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

      String[] requestedPermissions = packageInfo.requestedPermissions;
      if (requestedPermissions != null) {
        for (String permission : requestedPermissions) {
          if (isDangerousPermission(permission)) {
            // Check if permission is justified
            if (!isPermissionJustified(permission)) {
              addSecurityIssue(
                  new SecurityIssue(
                      SecurityIssue.Severity.MEDIUM,
                      "Excessive Permission",
                      "App requests permission: " + permission + " which may not be necessary."));
            }
          }
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Error checking permissions", e);
    }
  }

  /** Check Firebase security rules */
  private void checkFirebaseRules() {
    // This would typically require backend verification
    // For now, we'll check basic configuration
    if (!areFirebaseRulesSecure()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.HIGH,
              "Insecure Firebase Rules",
              "Firebase security rules may be too permissive. Review and tighten access controls."));
    }
  }

  /** Check code obfuscation */
  private void checkCodeObfuscation() {
    if (!isCodeObfuscated()) {
      addSecurityIssue(
          new SecurityIssue(
              SecurityIssue.Severity.LOW,
              "No Code Obfuscation",
              "Code is not obfuscated. Consider using ProGuard/R8 for release builds."));
    }
  }

  /** Helper methods for security checks */
  private boolean isDeviceRooted() {
    String[] paths = {
      "/system/app/Superuser.apk",
      "/sbin/su",
      "/system/bin/su",
      "/system/xbin/su",
      "/data/local/xbin/su",
      "/data/local/bin/su",
      "/system/sd/xbin/su",
      "/system/bin/failsafe/su",
      "/data/local/su"
    };

    for (String path : paths) {
      if (new File(path).exists()) {
        return true;
      }
    }

    return false;
  }

  private boolean isSSLPinningEnabled() {
    // TODO: Implement actual SSL pinning check
    return false;
  }

  private boolean isCleartextTrafficAllowed() {
    // TODO: Check network security config
    return true;
  }

  private boolean isDataEncrypted() {
    // TODO: Check encryption implementation
    return false;
  }

  private boolean isSharedPreferencesEncrypted() {
    // TODO: Check if using EncryptedSharedPreferences
    return false;
  }

  private boolean isDangerousPermission(String permission) {
    return permission.contains("android.permission.READ_CONTACTS")
        || permission.contains("android.permission.CAMERA")
        || permission.contains("android.permission.RECORD_AUDIO")
        || permission.contains("android.permission.ACCESS_FINE_LOCATION")
        || permission.contains("android.permission.READ_SMS")
        || permission.contains("android.permission.READ_PHONE_STATE");
  }

  private boolean isPermissionJustified(String permission) {
    // TODO: Implement logic to check if permission is justified based on app features
    return true;
  }

  private boolean areFirebaseRulesSecure() {
    // TODO: Implement Firebase rules security check
    return false;
  }

  private boolean isCodeObfuscated() {
    // Simple check - in reality would need more sophisticated analysis
    try {
      Class.forName("a.a.a");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /** Add security issue to the list */
  private void addSecurityIssue(SecurityIssue issue) {
    securityIssues.add(issue);
    Log.w(TAG, "Security Issue: " + issue.getTitle() + " - " + issue.getDescription());
  }

  /** Generate comprehensive security report */
  private SecurityReport generateSecurityReport() {
    SecurityReport report = new SecurityReport();
    report.setTimestamp(new Date());
    report.setDeviceInfo(getDeviceInfo());
    report.setAppInfo(getAppInfo());
    report.setSecurityIssues(new ArrayList<>(securityIssues));
    report.setOverallScore(calculateSecurityScore());

    return report;
  }

  /** Calculate overall security score */
  private int calculateSecurityScore() {
    if (securityIssues.isEmpty()) {
      return 100;
    }

    int totalDeductions = 0;
    for (SecurityIssue issue : securityIssues) {
      switch (issue.getSeverity()) {
        case HIGH:
          totalDeductions += 20;
          break;
        case MEDIUM:
          totalDeductions += 10;
          break;
        case LOW:
          totalDeductions += 5;
          break;
      }
    }

    return Math.max(0, 100 - totalDeductions);
  }

  /** Get device information */
  private Map<String, String> getDeviceInfo() {
    Map<String, String> deviceInfo = new HashMap<>();
    deviceInfo.put("manufacturer", Build.MANUFACTURER);
    deviceInfo.put("model", Build.MODEL);
    deviceInfo.put("android_version", Build.VERSION.RELEASE);
    deviceInfo.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
    deviceInfo.put("security_patch", Build.VERSION.SECURITY_PATCH);
    return deviceInfo;
  }

  /** Get app information */
  private Map<String, String> getAppInfo() {
    Map<String, String> appInfo = new HashMap<>();
    try {
      PackageInfo packageInfo =
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      appInfo.put("package_name", packageInfo.packageName);
      appInfo.put("version_name", packageInfo.versionName);
      appInfo.put("version_code", String.valueOf(packageInfo.versionCode));
    } catch (Exception e) {
      Log.e(TAG, "Error getting app info", e);
    }
    return appInfo;
  }

  /** Export security report to various formats */
  public String exportReportAsJSON(SecurityReport report) {
    // TODO: Implement JSON export
    return report.toJSON();
  }

  public String exportReportAsHTML(SecurityReport report) {
    // TODO: Implement HTML export
    return report.toHTML();
  }

  public String exportReportAsMarkdown(SecurityReport report) {
    // TODO: Implement Markdown export
    return report.toMarkdown();
  }

  /** Save report to file */
  public void saveReportToFile(SecurityReport report, File outputFile) {
    // TODO: Implement file saving
  }

  /** Upload report to Firebase */
  public CompletableFuture<Void> uploadReportToFirebase(SecurityReport report) {
    return CompletableFuture.runAsync(
        () -> {
          FirebaseFirestore db = FirebaseFirestore.getInstance();
          db.collection("security_reports")
              .add(report.toMap())
              .addOnSuccessListener(
                  documentReference ->
                      Log.d(TAG, "Report uploaded with ID: " + documentReference.getId()))
              .addOnFailureListener(e -> Log.e(TAG, "Error uploading report", e));
        });
  }

  /** Functional interface for security checks */
  private interface SecurityCheck {
    void check();
  }
}
