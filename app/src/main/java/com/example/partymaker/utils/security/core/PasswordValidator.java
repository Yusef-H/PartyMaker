package com.example.partymaker.utils.security.core;

import com.example.partymaker.utils.core.AppConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Enhanced password validation utility with strong security requirements */
public final class PasswordValidator {

  // Private constructor to prevent instantiation
  private PasswordValidator() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // Minimum password length from constants
  private static final int MIN_LENGTH = AppConstants.Security.MIN_PASSWORD_LENGTH;

  // Maximum password length (to prevent DoS attacks)
  private static final int MAX_LENGTH = 128;

  // Regex patterns for password requirements
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
  private static final Pattern SPECIAL_CHAR_PATTERN =
      Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

  // Common weak passwords to reject
  private static final String[] COMMON_PASSWORDS = {
    "password",
    "12345678",
    "123456789",
    "qwerty",
    "abc123",
    "password123",
    "admin",
    "letmein",
    "welcome",
    "monkey",
    "dragon",
    "baseball",
    "iloveyou",
    "trustno1",
    "1234567",
    "sunshine",
    "master",
    "123123",
    "welcome123",
    "shadow",
    "ashley",
    "football",
    "jesus",
    "michael",
    "ninja",
    "mustang"
  };

  /** Validation result containing status and messages */
  public static class ValidationResult {
    public final boolean isValid;
    public final List<String> errors;
    public final int strengthScore; // 0-100

    public ValidationResult(boolean isValid, List<String> errors, int strengthScore) {
      this.isValid = isValid;
      this.errors = errors;
      this.strengthScore = strengthScore;
    }
  }

  /** Validate password with enhanced security requirements */
  public static ValidationResult validate(String password) {
    List<String> errors = new ArrayList<>();
    int strengthScore = 0;

    if (password == null || password.isEmpty()) {
      errors.add("Password is required");
      return new ValidationResult(false, errors, 0);
    }

    // Check length
    if (password.length() < MIN_LENGTH) {
      errors.add("Password must be at least " + MIN_LENGTH + " characters long");
    } else if (password.length() > MAX_LENGTH) {
      errors.add("Password must not exceed " + MAX_LENGTH + " characters");
    } else {
      // Add points for length
      strengthScore += Math.min(20, password.length() * 2);
    }

    // Check for required character types
    boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
    boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
    boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
    boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();

    if (!hasUppercase) {
      errors.add("Password must contain at least one uppercase letter");
    } else {
      strengthScore += 20;
    }

    if (!hasLowercase) {
      errors.add("Password must contain at least one lowercase letter");
    } else {
      strengthScore += 20;
    }

    if (!hasDigit) {
      errors.add("Password must contain at least one number");
    } else {
      strengthScore += 20;
    }

    if (!hasSpecialChar) {
      errors.add(
          "Password must contain at least one special character (!@#$%^&*()_+-=[]{};':\"\\|,.<>/?)");
    } else {
      strengthScore += 20;
    }

    // Check for common passwords
    String lowerPassword = password.toLowerCase();
    for (String commonPassword : COMMON_PASSWORDS) {
      if (lowerPassword.equals(commonPassword)) {
        errors.add("This password is too common. Please choose a more unique password");
        strengthScore = Math.max(0, strengthScore - 50);
        break;
      }
    }

    // Check for repeated characters
    if (hasRepeatedCharacters(password)) {
      errors.add("Password should not contain repeated characters (e.g., 'aaa' or '111')");
      strengthScore = Math.max(0, strengthScore - 10);
    }

    // Check for sequential characters
    if (hasSequentialCharacters(password)) {
      errors.add("Password should not contain sequential characters (e.g., 'abc' or '123')");
      strengthScore = Math.max(0, strengthScore - 10);
    }

    return new ValidationResult(errors.isEmpty(), errors, Math.min(100, strengthScore));
  }

  /** Check if password contains repeated characters */
  private static boolean hasRepeatedCharacters(String password) {
    for (int i = 0; i < password.length() - 2; i++) {
      if (password.charAt(i) == password.charAt(i + 1)
          && password.charAt(i) == password.charAt(i + 2)) {
        return true;
      }
    }
    return false;
  }

  /** Check if password contains sequential characters */
  private static boolean hasSequentialCharacters(String password) {
    String lowerPassword = password.toLowerCase();

    // Check for alphabetical sequences
    for (int i = 0; i < lowerPassword.length() - 2; i++) {
      char c1 = lowerPassword.charAt(i);
      char c2 = lowerPassword.charAt(i + 1);
      char c3 = lowerPassword.charAt(i + 2);

      if (Character.isLetter(c1) && Character.isLetter(c2) && Character.isLetter(c3)) {
        if (c2 == c1 + 1 && c3 == c2 + 1) {
          return true;
        }
      }
    }

    // Check for numerical sequences
    for (int i = 0; i < password.length() - 2; i++) {
      char c1 = password.charAt(i);
      char c2 = password.charAt(i + 1);
      char c3 = password.charAt(i + 2);

      if (Character.isDigit(c1) && Character.isDigit(c2) && Character.isDigit(c3)) {
        if (c2 == c1 + 1 && c3 == c2 + 1) {
          return true;
        }
      }
    }

    return false;
  }

  /** Get password strength as a string */
  public static String getStrengthText(int strengthScore) {
    if (strengthScore < 20) {
      return "Very Weak";
    } else if (strengthScore < 40) {
      return "Weak";
    } else if (strengthScore < 60) {
      return "Fair";
    } else if (strengthScore < 80) {
      return "Good";
    } else {
      return "Strong";
    }
  }

  /** Check if a password matches confirmation */
  public static boolean passwordsMatch(String password, String confirmPassword) {
    if (password == null || confirmPassword == null) {
      return false;
    }
    return password.equals(confirmPassword);
  }
}
