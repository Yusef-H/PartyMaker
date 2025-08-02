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
 */
public class ContentSharingManager {
  private static final String TAG = "ShareHelper";

  /**
   * Share party details as text to any app that can handle text sharing
   *
   * @param context The application context
   * @param group The group/party to share
   */
  public static void sharePartyText(Context context, Group group) {
    try {
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");

      // Create a formatted message with party details
      String shareMessage = createShareMessage(group);

      shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my party: " + group.getGroupName());
      shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(Intent.createChooser(shareIntent, "Share Party Details"));

      Log.d(TAG, "Shared party as text: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing party text", e);
      Toast.makeText(context, "Error sharing party details", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details directly to WhatsApp
   *
   * @param context The application context
   * @param group The group/party to share
   */
  public static void shareToWhatsApp(Context context, Group group) {
    try {
      Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
      whatsappIntent.setType("text/plain");
      whatsappIntent.setPackage("com.whatsapp");

      // Create a formatted message with party details
      String shareMessage = createShareMessage(group);

      whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(whatsappIntent);

      Log.d(TAG, "Shared party to WhatsApp: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing to WhatsApp", e);
      Toast.makeText(context, "WhatsApp is not installed or an error occurred", Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * Share party details directly to Facebook
   *
   * @param context The application context
   * @param group The group/party to share
   */
  public static void shareToFacebook(Context context, Group group) {
    try {
      Intent facebookIntent = new Intent(Intent.ACTION_SEND);
      facebookIntent.setType("text/plain");
      facebookIntent.setPackage("com.facebook.katana");

      // Create a formatted message with party details
      String shareMessage = createShareMessage(group);

      facebookIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(facebookIntent);

      Log.d(TAG, "Shared party to Facebook: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing to Facebook", e);
      Toast.makeText(context, "Facebook is not installed or an error occurred", Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * Share party details via SMS
   *
   * @param context The application context
   * @param group The group/party to share
   */
  public static void shareViaSMS(Context context, Group group) {
    try {
      Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
      smsIntent.setData(Uri.parse("smsto:"));

      // Create a formatted message with party details
      String shareMessage = createShareMessage(group);

      smsIntent.putExtra("sms_body", shareMessage);

      context.startActivity(smsIntent);

      Log.d(TAG, "Shared party via SMS: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing via SMS", e);
      Toast.makeText(context, "Error sharing via SMS", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share party details via email
   *
   * @param context The application context
   * @param group The group/party to share
   */
  public static void shareViaEmail(Context context, Group group) {
    try {
      Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
      emailIntent.setData(Uri.parse("mailto:"));

      // Create a formatted message with party details
      String shareMessage = createShareMessage(group);

      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my party: " + group.getGroupName());
      emailIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(Intent.createChooser(emailIntent, "Send Email"));

      Log.d(TAG, "Shared party via email: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing via email", e);
      Toast.makeText(context, "Error sharing via email", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Share an image of the party details
   *
   * @param context The application context
   * @param group The group/party to share
   * @param image The image to share (e.g., screenshot of party details)
   */
  public static void sharePartyImage(Context context, Group group, Bitmap image) {
    try {
      // Save bitmap to the MediaStore
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
      String path =
          MediaStore.Images.Media.insertImage(
              context.getContentResolver(), image, "Party: " + group.getGroupName(), null);

      if (path == null) {
        Log.e(TAG, "Failed to save image to MediaStore");
        Toast.makeText(context, "Error preparing image to share", Toast.LENGTH_SHORT).show();
        return;
      }

      Uri imageUri = Uri.parse(path);

      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("image/jpeg");
      shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

      // Add text caption
      String shareMessage = "Join my party: " + group.getGroupName();
      shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

      context.startActivity(Intent.createChooser(shareIntent, "Share Party Image"));

      Log.d(TAG, "Shared party as image: " + group.getGroupName());
    } catch (Exception e) {
      Log.e(TAG, "Error sharing party image", e);
      Toast.makeText(context, "Error sharing party image", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Create a formatted message with party details
   *
   * @param group The group/party to create a message for
   * @return A formatted string with party details
   */
  private static String createShareMessage(Group group) {
    StringBuilder message = new StringBuilder();

    message.append("🎉 You're invited to ").append(group.getGroupName()).append("! 🎉\n\n");

    // Date and time
    message
        .append("📅 Date: ")
        .append(group.getGroupDays())
        .append("/")
        .append(group.getGroupMonths())
        .append("/")
        .append(group.getGroupYears())
        .append("\n");

    message.append("⏰ Time: ").append(group.getGroupHours()).append("\n\n");

    // Location
    if (group.getGroupLocation() != null && !group.getGroupLocation().isEmpty()) {
      if (group.getGroupLocation().contains(",") && group.getGroupLocation().matches(".*\\d+.*")) {
        message.append("📍 Location: Check the app for location details\n\n");
      } else {
        message.append("📍 Location: ").append(group.getGroupLocation()).append("\n\n");
      }
    }

    // Entry price
    if (group.getGroupPrice() != null) {
      if (group.getGroupPrice().equals("0")) {
        message.append("💰 Entry: Free\n\n");
      } else {
        message.append("💰 Entry: ").append(group.getGroupPrice()).append("\n\n");
      }
    }

    // App promo
    message.append("Download PartyMaker app to join this party and more!\n");
    message.append("https://play.google.com/store/apps/details?id=com.example.partymaker");

    return message.toString();
  }
}
