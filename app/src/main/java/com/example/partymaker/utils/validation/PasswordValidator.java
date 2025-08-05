package com.example.partymaker.utils.validation;

import java.util.regex.Pattern;

/**
 * Utility class for password validation. Provides comprehensive password strength validation with
 * customizable rules.
 */
public class PasswordValidator {

  private static final int MIN_LENGTH = 6;
  private static final int RECOMMENDED_LENGTH = 8;

  // Password patterns
  private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
  private static final Pattern SPECIAL_CHAR_PATTERN =
      Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

  /**
   * Validates a password and returns detailed results.
   *
   * @param password The password to validate
   * @return ValidationResult containing validation details
   */
  public static ValidationResult validatePassword(String password) {
    if (password == null) {
      return new ValidationResult(false, "Password cannot be null");
    }

    if (password.isEmpty()) {
      return new ValidationResult(false, "Password cannot be empty");
    }

    if (password.length() < MIN_LENGTH) {
      return new ValidationResult(
          false, "Password must be at least " + MIN_LENGTH + " characters long");
    }

    // Check for whitespace
    if (password.contains(" ")) {
      return new ValidationResult(false, "Password cannot contain spaces");
    }

    // Basic validation passed
    if (password.length() >= MIN_LENGTH) {
      return new ValidationResult(true, "Password is valid");
    }

    return new ValidationResult(false, "Password does not meet requirements");
  }

  /**
   * Calculates password strength score (0-100).
   *
   * @param password The password to evaluate
   * @return Strength score from 0 to 100
   */
  public static int calculateStrength(String password) {
    if (password == null || password.isEmpty()) {
      return 0;
    }

    int score = 0;

    // Length scoring
    if (password.length() >= 6) score += 20;
    if (password.length() >= 8) score += 10;
    if (password.length() >= 12) score += 10;

    // Character type scoring
    if (LOWERCASE_PATTERN.matcher(password).find()) score += 10;
    if (UPPERCASE_PATTERN.matcher(password).find()) score += 15;
    if (DIGIT_PATTERN.matcher(password).find()) score += 15;
    if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 20;

    return Math.min(score, 100);
  }

  /**
   * Gets a human-readable strength description.
   *
   * @param password The password to evaluate
   * @return Strength description
   */
  public static String getStrengthDescription(String password) {
    int strength = calculateStrength(password);

    if (strength < 25) return "Weak";
    if (strength < 50) return "Fair";
    if (strength < 75) return "Good";
    return "Strong";
  }

  /**
   * Checks if password meets recommended criteria.
   *
   * @param password The password to check
   * @return true if password meets recommended criteria
   */
  public static boolean isRecommendedStrength(String password) {
    return calculateStrength(password) >= 60;
  }

  /** Result of password validation. */
  public static class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;

    public ValidationResult(boolean isValid, String errorMessage) {
      this.isValid = isValid;
      this.errorMessage = errorMessage;
    }

    public boolean isValid() {
      return isValid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public String toString() {
      return "ValidationResult{"
          + "isValid="
          + isValid
          + ", errorMessage='"
          + errorMessage
          + '\''
          + '}';
    }
  }
}
