package com.example.partymaker.utils.security;

import static org.junit.Assert.*;

import com.example.partymaker.utils.security.core.PasswordValidator;

import org.junit.Test;

/**
 * Unit tests for PasswordValidator.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Password length requirements
 *   <li>Character type requirements (upper, lower, digit, special)
 *   <li>Common password detection
 *   <li>Sequential and repeated character detection
 *   <li>Password strength scoring
 * </ul>
 */
public class PasswordValidatorTest {

  @Test
  public void testValidate_ValidPassword_ReturnsValid() {
    // Arrange
    String validPassword = "MySecure123!";

    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate(validPassword);

    // Assert
    assertTrue("Password should be valid", result.isValid);
    assertTrue("Password should have good strength", result.strengthScore >= 60);
    assertTrue("Error list should be empty", result.errors.isEmpty());
  }

  @Test
  public void testValidate_NullPassword_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate(null);

    // Assert
    assertFalse("Null password should be invalid", result.isValid);
    assertEquals("Strength score should be 0", 0, result.strengthScore);
    assertFalse("Error list should not be empty", result.errors.isEmpty());
    assertTrue("Should contain required error", result.errors.contains("Password is required"));
  }

  @Test
  public void testValidate_EmptyPassword_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("");

    // Assert
    assertFalse("Empty password should be invalid", result.isValid);
    assertEquals("Strength score should be 0", 0, result.strengthScore);
    assertTrue("Should contain required error", result.errors.contains("Password is required"));
  }

  @Test
  public void testValidate_TooShort_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("Abc1!");

    // Assert
    assertFalse("Short password should be invalid", result.isValid);
    assertTrue(
        "Should contain length error",
        result.errors.stream().anyMatch(error -> error.contains("at least")));
  }

  @Test
  public void testValidate_NoUppercase_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("mypassword123!");

    // Assert
    assertFalse("Password without uppercase should be invalid", result.isValid);
    assertTrue(
        "Should contain uppercase error",
        result.errors.stream().anyMatch(error -> error.contains("uppercase")));
  }

  @Test
  public void testValidate_NoLowercase_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("MYPASSWORD123!");

    // Assert
    assertFalse("Password without lowercase should be invalid", result.isValid);
    assertTrue(
        "Should contain lowercase error",
        result.errors.stream().anyMatch(error -> error.contains("lowercase")));
  }

  @Test
  public void testValidate_NoDigit_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("MyPassword!");

    // Assert
    assertFalse("Password without digit should be invalid", result.isValid);
    assertTrue(
        "Should contain digit error",
        result.errors.stream().anyMatch(error -> error.contains("number")));
  }

  @Test
  public void testValidate_NoSpecialChar_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("MyPassword123");

    // Assert
    assertFalse("Password without special char should be invalid", result.isValid);
    assertTrue(
        "Should contain special character error",
        result.errors.stream().anyMatch(error -> error.contains("special character")));
  }

  @Test
  public void testValidate_CommonPassword_ReturnsInvalid() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("Password123!");

    // Assert
    assertFalse("Common password should be invalid", result.isValid);
    assertTrue(
        "Should contain common password error",
        result.errors.stream().anyMatch(error -> error.contains("too common")));
    assertTrue("Strength should be reduced", result.strengthScore < 100);
  }

  @Test
  public void testValidate_RepeatedCharacters_ReducesStrength() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("Myyyy123!");

    // Assert
    assertFalse("Password with repeated chars should be invalid", result.isValid);
    assertTrue(
        "Should contain repeated character error",
        result.errors.stream().anyMatch(error -> error.contains("repeated characters")));
  }

  @Test
  public void testValidate_SequentialCharacters_ReducesStrength() {
    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate("Mabc123!");

    // Assert
    assertFalse("Password with sequential chars should be invalid", result.isValid);
    assertTrue(
        "Should contain sequential character error",
        result.errors.stream().anyMatch(error -> error.contains("sequential characters")));
  }

  @Test
  public void testGetStrengthText_VariousScores() {
    // Test different strength levels
    assertEquals("Very Weak", PasswordValidator.getStrengthText(10));
    assertEquals("Weak", PasswordValidator.getStrengthText(30));
    assertEquals("Fair", PasswordValidator.getStrengthText(50));
    assertEquals("Good", PasswordValidator.getStrengthText(70));
    assertEquals("Strong", PasswordValidator.getStrengthText(90));
  }

  @Test
  public void testPasswordsMatch_ValidMatching_ReturnsTrue() {
    // Act & Assert
    assertTrue(
        "Matching passwords should return true",
        PasswordValidator.passwordsMatch("Password123!", "Password123!"));
  }

  @Test
  public void testPasswordsMatch_NonMatching_ReturnsFalse() {
    // Act & Assert
    assertFalse(
        "Non-matching passwords should return false",
        PasswordValidator.passwordsMatch("Password123!", "DifferentPassword!"));
  }

  @Test
  public void testPasswordsMatch_NullPasswords_ReturnsFalse() {
    // Act & Assert
    assertFalse("Null passwords should return false", PasswordValidator.passwordsMatch(null, null));
    assertFalse(
        "One null password should return false",
        PasswordValidator.passwordsMatch("Password123!", null));
    assertFalse(
        "One null password should return false",
        PasswordValidator.passwordsMatch(null, "Password123!"));
  }

  @Test
  public void testValidate_MaxLength_IsEnforced() {
    // Create a password that's too long (over 128 characters)
    StringBuilder longPassword = new StringBuilder();
    for (int i = 0; i < 130; i++) {
      longPassword.append("a");
    }
    longPassword.append("A1!");

    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate(longPassword.toString());

    // Assert
    assertFalse("Overly long password should be invalid", result.isValid);
    assertTrue(
        "Should contain max length error",
        result.errors.stream().anyMatch(error -> error.contains("must not exceed")));
  }

  @Test
  public void testValidate_StrongPassword_HighScore() {
    // A strong password with all requirements
    String strongPassword = "MyVerySecure!Password2024";

    // Act
    PasswordValidator.ValidationResult result = PasswordValidator.validate(strongPassword);

    // Assert
    assertTrue("Strong password should be valid", result.isValid);
    assertTrue("Strong password should have high score", result.strengthScore >= 80);
    assertEquals("Strong", PasswordValidator.getStrengthText(result.strengthScore));
  }
}
