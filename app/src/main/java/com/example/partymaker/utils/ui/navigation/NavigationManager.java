package com.example.partymaker.utils.ui.navigation;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.partymaker.R;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.group.CreateGroupActivity;
import com.example.partymaker.ui.group.PublicGroupsActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;

public class NavigationManager {

  /**
   * Sets up the bottom navigation with the current page highlighted
   *
   * @param activity The activity
   * @param currentPage The current page identifier
   */
  public static void setupBottomNavigation(Activity activity, String currentPage) {
    LinearLayout navProfile = activity.findViewById(R.id.navProfile);
    LinearLayout navMyParties = activity.findViewById(R.id.navMyParties);
    LinearLayout navPublicParties = activity.findViewById(R.id.navPublicParties);
    LinearLayout navCreateGroup = activity.findViewById(R.id.navCreateGroup);

    // Set current page highlighting
    highlightCurrentPage(activity, currentPage);

    // Set click listeners
    if (navProfile != null) {
      navProfile.setOnClickListener(v -> navigateToProfile(activity));
    }

    if (navMyParties != null) {
      navMyParties.setOnClickListener(v -> navigateToMyParties(activity));
    }

    if (navPublicParties != null) {
      navPublicParties.setOnClickListener(v -> navigateToPublicParties(activity));
    }

    if (navCreateGroup != null) {
      navCreateGroup.setOnClickListener(v -> navigateToCreateGroup(activity));
    }
  }

  /**
   * Sets up the bottom navigation without highlighting any page
   *
   * @param activity The activity
   */
  public static void setupBottomNavigation(Activity activity) {
    setupBottomNavigation(activity, "none");
  }

  private static void highlightCurrentPage(Activity activity, String currentPage) {
    // Reset all to default colors
    resetNavColors(activity);

    // Highlight current page
    switch (currentPage) {
      case "profile":
        highlightNavItem(activity, R.id.navProfile);
        break;
      case "myparties":
        highlightNavItem(activity, R.id.navMyParties);
        break;
      case "publicparties":
        highlightNavItem(activity, R.id.navPublicParties);
        break;
      case "creategroup":
        highlightNavItem(activity, R.id.navCreateGroup);
        break;
      case "none":
      default:
        // Don't highlight any item
        break;
    }
  }

  private static void resetNavColors(Activity activity) {
    resetNavItem(activity, R.id.navProfile);
    resetNavItem(activity, R.id.navMyParties);
    resetNavItem(activity, R.id.navPublicParties);
    resetNavItem(activity, R.id.navCreateGroup);
  }

  private static void resetNavItem(Activity activity, int navItemId) {
    LinearLayout navItem = activity.findViewById(navItemId);
    if (navItem != null) {
      ImageView icon = (ImageView) navItem.getChildAt(0);
      TextView text = (TextView) navItem.getChildAt(1);

      if (icon != null) {
        icon.setColorFilter(activity.getResources().getColor(R.color.gray_medium));
      }
      if (text != null) {
        text.setTextColor(activity.getResources().getColor(R.color.gray_medium));
      }
    }
  }

  private static void highlightNavItem(Activity activity, int navItemId) {
    LinearLayout navItem = activity.findViewById(navItemId);
    if (navItem != null) {
      ImageView icon = (ImageView) navItem.getChildAt(0);
      TextView text = (TextView) navItem.getChildAt(1);

      if (icon != null) {
        icon.setColorFilter(activity.getResources().getColor(R.color.blue_primary));
      }
      if (text != null) {
        text.setTextColor(activity.getResources().getColor(R.color.blue_primary));
      }
    }
  }

  private static void navigateToProfile(Activity activity) {
    Intent intent = new Intent(activity, EditProfileActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToMyParties(Activity activity) {
    Intent intent = new Intent(activity, MainActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToPublicParties(Activity activity) {
    Intent intent = new Intent(activity, PublicGroupsActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToCreateGroup(Activity activity) {
    Intent intent = new Intent(activity, CreateGroupActivity.class);
    activity.startActivity(intent);
  }
}
