package com.example.partymaker.utilities;

import static com.example.partymaker.utilities.Constants.ADMIN_KEY;
import static com.example.partymaker.utilities.Constants.CAN_ADD;
import static com.example.partymaker.utilities.Constants.COMING_KEYS;
import static com.example.partymaker.utilities.Constants.CREATED_AT;
import static com.example.partymaker.utilities.Constants.FRIEND_KEYS;
import static com.example.partymaker.utilities.Constants.GROUP_DAYS;
import static com.example.partymaker.utilities.Constants.GROUP_HOURS;
import static com.example.partymaker.utilities.Constants.GROUP_KEY;
import static com.example.partymaker.utilities.Constants.GROUP_LOCATION;
import static com.example.partymaker.utilities.Constants.GROUP_MONTHS;
import static com.example.partymaker.utilities.Constants.GROUP_NAME;
import static com.example.partymaker.utilities.Constants.GROUP_PRICE;
import static com.example.partymaker.utilities.Constants.GROUP_TYPE;
import static com.example.partymaker.utilities.Constants.GROUP_YEARS;
import static com.example.partymaker.utilities.Constants.MESSAGE_KEYS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;


/** Utility methods for working with Intents and ExtrasMetadata. */
public class Common {
  private static float downX, downY, dX, dY;
  private static int touchSlop;


  /** Packs all fields from ExtrasMetadata into the given Intent. */
  public static void addExtrasToIntent(Intent intent, ExtrasMetadata extras) {
    intent.putExtra(GROUP_NAME, extras.getGroupName());
    intent.putExtra(GROUP_KEY, extras.getGroupKey());
    intent.putExtra(GROUP_DAYS, extras.getGroupDays());
    intent.putExtra(GROUP_MONTHS, extras.getGroupMonths());
    intent.putExtra(GROUP_YEARS, extras.getGroupYears());
    intent.putExtra(GROUP_HOURS, extras.getGroupHours());
    intent.putExtra(GROUP_LOCATION, extras.getGroupLocation());
    intent.putExtra(ADMIN_KEY, extras.getAdminKey());
    intent.putExtra(CREATED_AT, extras.getCreatedAt());
    intent.putExtra(GROUP_PRICE, extras.getGroupPrice());
    intent.putExtra(GROUP_TYPE, extras.getGroupType());
    intent.putExtra(CAN_ADD, extras.isCanAdd());
    intent.putExtra(FRIEND_KEYS, extras.getFriendKeys());
    intent.putExtra(COMING_KEYS, extras.getComingKeys());
    intent.putExtra(MESSAGE_KEYS, extras.getMessageKeys());
  }

  @SuppressLint("ClickableViewAccessibility")
  public static boolean dragChatButtonOnTouch(View view, MotionEvent event) {
    // lazy‐init slop
    if (touchSlop == 0) {
      touchSlop = ViewConfiguration.get(view.getContext())
              .getScaledTouchSlop();
    }

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        downX = event.getRawX();
        downY = event.getRawY();
        dX    = view.getX() - downX;
        dY    = view.getY() - downY;
        return true;

      case MotionEvent.ACTION_MOVE:
        float newX = event.getRawX() + dX;
        float newY = event.getRawY() + dY;
        View parent = (View) view.getParent();
        // clamp inside parent bounds
        newX = Math.max(0, Math.min(newX, parent.getWidth()  - view.getWidth()));
        newY = Math.max(0, Math.min(newY, parent.getHeight() - view.getHeight()));
        view.setX(newX);
        view.setY(newY);
        return true;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        float upX = event.getRawX();
        float upY = event.getRawY();
        // if finger didn’t move more than slop → it’s a tap
        if (Math.hypot(upX - downX, upY - downY) < touchSlop) {
          view.performClick();
        }
        return true;

      default:
        return false;
    }
  }

  public static String getApiKey(Context ctx, String key) {
    try {
      java.util.Properties properties = new java.util.Properties();
      java.io.InputStream inputStream = ctx.getAssets().open("local.properties");
      properties.load(inputStream);
      return properties.getProperty(key);
    } catch (java.io.IOException e) {
      return "";
    }
  }
}
