package com.example.partymaker.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.group.PartyMainActivity;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for handling notifications in the app. This class provides methods for creating
 * notification channels and displaying notifications.
 */
public class NotificationHelper {
  private static final String TAG = "NotificationHelper";

  // Notification channel IDs
  public static final String CHANNEL_PARTIES = "channel_parties";
  public static final String CHANNEL_MESSAGES = "channel_messages";
  public static final String CHANNEL_UPDATES = "channel_updates";

  // Store notification subscriptions locally
  private static final Map<String, Boolean> groupSubscriptions = new HashMap<>();
  private static boolean globalSubscription = false;

  /**
   * Creates notification channels for Android O and above
   *
   * @param context The application context
   */
  public static void createNotificationChannels(Context context) {
    // Only needed for Android O and above
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    // Party updates channel
    NotificationChannel partyChannel =
        new NotificationChannel(
            CHANNEL_PARTIES, "Party Updates", NotificationManager.IMPORTANCE_HIGH);
    partyChannel.setDescription("Notifications about party updates");
    partyChannel.enableLights(true);
    partyChannel.setLightColor(Color.BLUE);
    partyChannel.enableVibration(true);
    notificationManager.createNotificationChannel(partyChannel);

    // Messages channel
    NotificationChannel messagesChannel =
        new NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH);
    messagesChannel.setDescription("Notifications about new messages");
    messagesChannel.enableLights(true);
    messagesChannel.setLightColor(Color.GREEN);
    messagesChannel.enableVibration(true);
    notificationManager.createNotificationChannel(messagesChannel);

    // General updates channel
    NotificationChannel updatesChannel =
        new NotificationChannel(
            CHANNEL_UPDATES, "General Updates", NotificationManager.IMPORTANCE_DEFAULT);
    updatesChannel.setDescription("General app updates and announcements");
    notificationManager.createNotificationChannel(updatesChannel);

    Log.d(TAG, "Notification channels created");
  }

  /**
   * Subscribe to a group's notification topic
   *
   * @param groupId The ID of the group to subscribe to
   */
  public static void subscribeToGroup(String groupId) {
    groupSubscriptions.put(groupId, true);
    Log.d(TAG, "Subscribed to group: " + groupId);
  }

  /**
   * Unsubscribe from a group's notification topic
   *
   * @param groupId The ID of the group to unsubscribe from
   */
  public static void unsubscribeFromGroup(String groupId) {
    groupSubscriptions.put(groupId, false);
    Log.d(TAG, "Unsubscribed from group: " + groupId);
  }

  /**
   * Check if subscribed to a group
   *
   * @param groupId The ID of the group to check
   * @return true if subscribed, false otherwise
   */
  public static boolean isSubscribedToGroup(String groupId) {
    Boolean isSubscribed = groupSubscriptions.get(groupId);
    return isSubscribed != null && isSubscribed;
  }

  /** Subscribe to global announcements */
  public static void subscribeToGlobalAnnouncements() {
    globalSubscription = true;
    Log.d(TAG, "Subscribed to global announcements");
  }

  /** Unsubscribe from global announcements */
  public static void unsubscribeFromGlobalAnnouncements() {
    globalSubscription = false;
    Log.d(TAG, "Unsubscribed from global announcements");
  }

  /**
   * Check if subscribed to global announcements
   *
   * @return true if subscribed, false otherwise
   */
  public static boolean isSubscribedToGlobalAnnouncements() {
    return globalSubscription;
  }

  /**
   * Display a party update notification
   *
   * @param context The application context
   * @param title The notification title
   * @param message The notification message
   * @param group The group related to the notification
   */
  public static void showPartyNotification(
      Context context, String title, String message, Group group) {
    // Only show notification if subscribed to this group
    if (!isSubscribedToGroup(group.getGroupKey())) {
      Log.d(TAG, "Not showing notification for group " + group.getGroupKey() + " - not subscribed");
      return;
    }

    Intent intent = new Intent(context, PartyMainActivity.class);
    intent.putExtra("GroupKey", group.getGroupKey());

    showNotification(context, title, message, intent, CHANNEL_PARTIES, 1);
  }

  /**
   * Display a new message notification
   *
   * @param context The application context
   * @param title The notification title
   * @param message The notification message
   * @param groupId The ID of the group the message is from
   */
  public static void showMessageNotification(
      Context context, String title, String message, String groupId) {
    // Only show notification if subscribed to this group
    if (!isSubscribedToGroup(groupId)) {
      Log.d(TAG, "Not showing message notification for group " + groupId + " - not subscribed");
      return;
    }

    Intent intent = new Intent(context, PartyMainActivity.class);
    intent.putExtra("GroupKey", groupId);

    showNotification(context, title, message, intent, CHANNEL_MESSAGES, 2);
  }

  /**
   * Display a general update notification
   *
   * @param context The application context
   * @param title The notification title
   * @param message The notification message
   */
  public static void showUpdateNotification(Context context, String title, String message) {
    // Only show notification if subscribed to global announcements
    if (!isSubscribedToGlobalAnnouncements()) {
      Log.d(TAG, "Not showing global notification - not subscribed");
      return;
    }

    Intent intent = new Intent(context, PartyMainActivity.class);

    showNotification(context, title, message, intent, CHANNEL_UPDATES, 3);
  }

  /** Generic method to show a notification */
  private static void showNotification(
      Context context,
      String title,
      String message,
      Intent intent,
      String channelId,
      int notificationId) {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_party_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH);

    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(notificationId, notificationBuilder.build());

    Log.d(TAG, "Notification displayed: " + title);
  }
}
