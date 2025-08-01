package com.example.partymaker.utils.security;

import android.util.Log;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Enhanced SSL Certificate Pinning Manager for secure network connections. Implements certificate
 * pinning to prevent man-in-the-middle attacks.
 */
public class SSLPinningManager {
  private static final String TAG = "SSLPinningManager";

  // Production certificate pins for partymaker.onrender.com
  private static final String[] RENDER_PINS = {
    // Render.com actual certificate pins (extracted from logs)
    "sha256/dkMGMmGeNRiqRXCzd53WyllMgWte1onHKx0WPImZncY=", // CN=onrender.com
    "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // CN=WE1,O=Google Trust Services,C=US
    "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=", // CN=GTS Root R4,O=Google Trust Services
    // LLC,C=US
  };

  // Development/localhost pins
  private static final String[] DEV_PINS = {
    "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=", // Development certificate
  };

  private static SSLPinningManager instance;
  private final Set<String> pinnedCertificates;
  private final boolean isProduction;

  private SSLPinningManager(boolean isProduction) {
    this.isProduction = isProduction;
    this.pinnedCertificates = new HashSet<>();

    if (isProduction) {
      pinnedCertificates.addAll(Arrays.asList(RENDER_PINS));
    } else {
      pinnedCertificates.addAll(Arrays.asList(DEV_PINS));
    }
  }

  public static synchronized SSLPinningManager getInstance(boolean isProduction) {
    if (instance == null) {
      instance = new SSLPinningManager(isProduction);
    }
    return instance;
  }

  /** Creates an OkHttpClient with SSL pinning enabled */
  public OkHttpClient createSecureClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    if (isProduction) {
      // Enable certificate pinning for production
      CertificatePinner certificatePinner =
          new CertificatePinner.Builder().add("partymaker.onrender.com", RENDER_PINS).build();

      builder.certificatePinner(certificatePinner);

      // Custom hostname verifier
      builder.hostnameVerifier(new CustomHostnameVerifier());
    } else {
      Log.w(TAG, "SSL Pinning disabled for development environment");
    }

    return builder.build();
  }

  /** Validates a certificate against pinned certificates */
  public boolean validateCertificate(X509Certificate certificate) {
    try {
      String pin = getPinFromCertificate(certificate);
      boolean isValid = pinnedCertificates.contains(pin);

      if (!isValid) {
        Log.w(TAG, "Certificate validation failed. Pin: " + pin);
        SecurityEvent.logSecurityViolation(
            "SSL_PINNING_FAILURE", "Certificate pin mismatch: " + certificate.getSubjectDN());
      }

      return isValid;
    } catch (Exception e) {
      Log.e(TAG, "Error validating certificate", e);
      return false;
    }
  }

  /** Extracts SHA256 pin from certificate */
  private String getPinFromCertificate(X509Certificate certificate) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] publicKey = certificate.getPublicKey().getEncoded();
    byte[] hash = digest.digest(publicKey);
    return "sha256/" + android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
  }

  /** Custom Trust Manager for additional certificate validation */
  private class CustomTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      // Client certificate validation (if needed)
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      if (chain == null || chain.length == 0) {
        throw new CertificateException("No certificate chain provided");
      }

      // Validate each certificate in the chain
      for (X509Certificate cert : chain) {
        if (!validateCertificate(cert)) {
          throw new CertificateException("Certificate pinning validation failed");
        }
      }

      // Additional security checks
      performAdditionalSecurityChecks(chain[0]);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    private void performAdditionalSecurityChecks(X509Certificate certificate)
        throws CertificateException {
      // Check certificate validity period
      try {
        certificate.checkValidity();
      } catch (Exception e) {
        throw new CertificateException("Certificate validity check failed", e);
      }

      // Check key usage
      boolean[] keyUsage = certificate.getKeyUsage();
      if (keyUsage != null && keyUsage.length > 0) {
        // Ensure digital signature is allowed
        if (!keyUsage[0]) {
          Log.w(TAG, "Certificate does not allow digital signatures");
        }
      }
    }
  }

  /** Custom Hostname Verifier for additional hostname validation */
  private class CustomHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      // Allow our known hosts
      Set<String> allowedHosts = new HashSet<>();
      allowedHosts.add("partymaker.onrender.com");
      allowedHosts.add("localhost");
      allowedHosts.add("10.0.2.2"); // Android Emulator localhost

      boolean isAllowed = allowedHosts.contains(hostname);

      if (!isAllowed) {
        Log.w(TAG, "Hostname verification failed for: " + hostname);
        SecurityEvent.logSecurityViolation(
            "HOSTNAME_VERIFICATION_FAILURE", "Unexpected hostname: " + hostname);
      }

      return isAllowed;
    }
  }

  /** Tests SSL connection to verify pinning is working */
  public boolean testSSLConnection(String url) {
    try {
      OkHttpClient client = createSecureClient();
      Request request = new Request.Builder().url(url).build();

      try (Response response = client.newCall(request).execute()) {
        boolean isSuccessful = response.isSuccessful();
        Log.d(TAG, "SSL connection test " + (isSuccessful ? "passed" : "failed") + " for: " + url);
        return isSuccessful;
      }
    } catch (Exception e) {
      Log.e(TAG, "SSL connection test failed for: " + url, e);
      return false;
    }
  }

  /** Updates certificate pins (for certificate rotation) */
  public void updateCertificatePins(String[] newPins) {
    if (newPins != null && newPins.length > 0) {
      pinnedCertificates.clear();
      pinnedCertificates.addAll(Arrays.asList(newPins));
      Log.i(TAG, "Certificate pins updated. Count: " + newPins.length);
    }
  }

  /** Security event logging helper */
  private static class SecurityEvent {
    static void logSecurityViolation(String eventType, String details) {
      Log.w(TAG, "SECURITY VIOLATION - " + eventType + ": " + details);
      // In production, send to security monitoring system
    }
  }
}
