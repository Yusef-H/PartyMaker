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

import android.content.Intent;

/** Utility methods for working with Intents and ExtrasMetadata. */
public class Common {
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
}
