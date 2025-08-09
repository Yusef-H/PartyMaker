package com.example.partymaker.utils.business.sharing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.example.partymaker.data.model.Group;
import java.io.ByteArrayOutputStream;

/**
 * Helper class for sharing party information with other apps. This class provides methods for
 * sharing party details via text, generating shareable images, and sharing to specific platforms.
 * All methods are static and handle proper error checking and user feedback.
 */
public class ContentSharingManager {
  private static final String TAG = "ContentSharingManager";

  // Share message constants
  private static final String SHARE_SUBJECT_PREFIX = "Join my party: ";
  private static final String SHARE_TITLE_GENERIC = "Share Party Details";
  private static final String SHARE_TITLE_EMAIL = "Send Email";
  private static final String SHARE_TITLE_IMAGE = "Share Party Image";

  // Error messages
  private static final String ERROR_SHARING_PARTY = "Error sharing party details";
  private static final String ERROR_WHATSAPP_NOT_AVAILABLE =
      "WhatsApp is not installed or an error occurred";
  private static final String ERROR_FACEBOOK_NOT_AVAILABLE =
      "Facebook is not installed or an error occurred";
  private static final String ERROR_SMS_FAILED = "Error sharing via SMS";
  private static final String ERROR_EMAIL_FAILED = "Error sharing via email";
  private static final String ERROR_IMAGE_SAVE_FAILED = "Error preparing image to share";
  private static final String ERROR_IMAGE_SHARE_FAILED = "Error sharing party image";

  // App package names
  private static final String WHATSAPP_PACKAGE = "com.whatsapp";
  private static final String FACEBOOK_PACKAGE = "com.facebook.katana";

  // Share message format constants
  private static final String PARTY_INVITATION_EMOJI = "🎉";
  private static final String DATE_EMOJI = "📅";
  private static final String TIME_EMOJI = "⏰";
  private static final String LOCATION_EMOJI = "📍";
  private static final String PRICE_EMOJI = "💰";
  private static final String DATE_SEPARATOR = "/";
  private static final String PRICE_FREE = "Free";
  private static final String LOCATION_FALLBACK = "Check the app for location details";
  private static final String APP_DOWNLOAD_URL =
      "https://play.google.com/store/apps/details?id=com.example.partymaker";
  private static final int IMAGE_COMPRESSION_QUALITY = 100;

  /**
   * Share party details as text to any app that can handle text sharing.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   */
  public static void sharePartyText(Context context, Group group) {
    if (validateInputs(context, group)) {
      return;
    }

    try {
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");

      String shareMessage = createShareMessage(group);
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT_PREFIX + group.getGroupName());
      shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(Intent.createChooser(shareIntent, SHARE_TITLE_GENERIC));
      Log.d(TAG, "Shared party as text: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing party text", e);
      Toast.makeText(context, ERROR_SHARING_PARTY, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details directly to WhatsApp.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   */
  public static void shareToWhatsApp(Context context, Group group) {
    if (validateInputs(context, group)) {
      return;
    }

    try {
      Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
      whatsappIntent.setType("text/plain");
      whatsappIntent.setPackage(WHATSAPP_PACKAGE);

      String shareMessage = createShareMessage(group);
      whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(whatsappIntent);
      Log.d(TAG, "Shared party to WhatsApp: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing to WhatsApp", e);
      Toast.makeText(context, ERROR_WHATSAPP_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details directly to Facebook.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   */
  public static void shareToFacebook(Context context, Group group) {
    if (validateInputs(context, group)) {
      return;
    }

    try {
      Intent facebookIntent = new Intent(Intent.ACTION_SEND);
      facebookIntent.setType("text/plain");
      facebookIntent.setPackage(FACEBOOK_PACKAGE);

      String shareMessage = createShareMessage(group);
      facebookIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(facebookIntent);
      Log.d(TAG, "Shared party to Facebook: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing to Facebook", e);
      Toast.makeText(context, ERROR_FACEBOOK_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details via SMS.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   */
  public static void shareViaSMS(Context context, Group group) {
    if (validateInputs(context, group)) {
      return;
    }

    try {
      Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
      smsIntent.setData(Uri.parse("smsto:"));

      String shareMessage = createShareMessage(group);
      smsIntent.putExtra("sms_body", shareMessage);

      context.startActivity(smsIntent);
      Log.d(TAG, "Shared party via SMS: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing via SMS", e);
      Toast.makeText(context, ERROR_SMS_FAILED, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details via email.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   */
  public static void shareViaEmail(Context context, Group group) {
    if (validateInputs(context, group)) {
      return;
    }

    try {
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
      emailIntent.setData(Uri.parse("mailto:"));

      String shareMessage = createShareMessage(group);
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT_PREFIX + group.getGroupName());
      emailIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(Intent.createChooser(emailIntent, SHARE_TITLE_EMAIL));
      Log.d(TAG, "Shared party via email: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing via email", e);
      Toast.makeText(context, ERROR_EMAIL_FAILED, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share an image of the party details.
   *
   * @param context The application context (cannot be null)
   * @param group The group/party to share (cannot be null)
   * @param image The image to share (cannot be null)
   */
  public static void sharePartyImage(Context context, Group group, Bitmap image) {
    if (validateInputs(context, group) || image == null) {
      if (image == null) {
        Log.e(TAG, "Image cannot be null");
        if (context != null) {
          Toast.makeText(context, ERROR_IMAGE_SHARE_FAILED, Toast.LENGTH_SHORT).show();
        }
      }
      return;
    }

    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, bytes);
      String path =
          MediaStore.Images.Media.insertImage(
              context.getContentResolver(), image, "Party: " + group.getGroupName(), null);

      if (path == null) {
        Log.e(TAG, "Failed to save image to MediaStore");
        Toast.makeText(context, ERROR_IMAGE_SAVE_FAILED, Toast.LENGTH_SHORT).show();
        return;
      }

      Uri imageUri = Uri.parse(path);
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("image/jpeg");
      shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
      shareIntent.putExtra(Intent.EXTRA_TEXT, SHARE_SUBJECT_PREFIX + group.getGroupName());

      context.startActivity(Intent.createChooser(shareIntent, SHARE_TITLE_IMAGE));
      Log.d(TAG, "Shared party as image: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing party image", e);
      Toast.makeText(context, ERROR_IMAGE_SHARE_FAILED, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Creates a formatted message with party details.
   *
   * @param group The group/party to create a message for
   * @return A formatted string with party details
   */
  private static String createShareMessage(Group group) {
    StringBuilder message = new StringBuilder();

    message
        .append(PARTY_INVITATION_EMOJI)
        .append(" You're invited to ")
        .append(group.getGroupName())
        .append("! ")
        .append(PARTY_INVITATION_EMOJI)
        .append("\n\n");

    appendDateTimeInfo(message, group);
    appendLocationInfo(message, group);
    appendPriceInfo(message, group);
    appendAppPromotion(message);

    return message.toString();
  }

  /**
   * Appends date and time information to the message.
   *
   * @param message The StringBuilder to append to
   * @param group The group containing date/time info
   */
  private static void appendDateTimeInfo(StringBuilder message, Group group) {
    message
        .append(DATE_EMOJI)
        .append(" Date: ")
        .append(group.getGroupDays())
        .append(DATE_SEPARATOR)
        .append(group.getGroupMonths())
        .append(DATE_SEPARATOR)
        .append(group.getGroupYears())
        .append("\n");

    message.append(TIME_EMOJI).append(" Time: ").append(group.getGroupHours()).append("\n\n");
  }

  /**
   * Appends location information to the message.
   *
   * @param message The StringBuilder to append to
   * @param group The group containing location info
   */
  private static void appendLocationInfo(StringBuilder message, Group group) {
    if (group.getGroupLocation() != null && !group.getGroupLocation().trim().isEmpty()) {
      message.append(LOCATION_EMOJI).append(" Location: ");

      if (isLocationCoordinates(group.getGroupLocation())) {
        message.append(LOCATION_FALLBACK);
      } else {
        message.append(group.getGroupLocation());
      }
      message.append("\n\n");
    }
  }

  /**
   * Appends price information to the message.
   *
   * @param message The StringBuilder to append to
   * @param group The group containing price info
   */
  private static void appendPriceInfo(StringBuilder message, Group group) {
    if (group.getGroupPrice() != null) {
      message.append(PRICE_EMOJI).append(" Entry: ");

      if ("0".equals(group.getGroupPrice().trim())) {
        message.append(PRICE_FREE);
      } else {
        message.append(group.getGroupPrice());
      }
      message.append("\n\n");
    }
  }

  /**
   * Appends app promotion to the message.
   *
   * @param message The StringBuilder to append to
   */
  private static void appendAppPromotion(StringBuilder message) {
    message
        .append("Download PartyMaker app to join this party and more!\n")
        .append(APP_DOWNLOAD_URL);
  }

  /**
   * Checks if a location string appears to be coordinates.
   *
   * @param location The location string to check
   * @return true if it appears to be coordinates, false otherwise
   */
  private static boolean isLocationCoordinates(String location) {
    return location.contains(",") && location.matches(".*\\d+.*");
  }

  /**
   * Validates that context and group are not null.
   *
   * @param context The context to validate
   * @param group The group to validate
   * @return true if both are valid, false otherwise
   */
  private static boolean validateInputs(Context context, Group group) {
    if (context == null) {
      Log.e(TAG, "Context cannot be null");
      return true;
    }
    if (group == null) {
      Log.e(TAG, "Group cannot be null");
      Toast.makeText(context, ERROR_SHARING_PARTY, Toast.LENGTH_SHORT).show();
      return true;
    }
    return false;
  }

  // Private constructor to prevent instantiation
  private ContentSharingManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
