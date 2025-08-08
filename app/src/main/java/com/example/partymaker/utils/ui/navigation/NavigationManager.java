package com.example.partymaker.utils.ui.navigation;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.partymaker.R;
import com.example.partymaker.ui.features.auxiliary.profile.EditProfileActivity;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.ui.features.groups.creation.CreateGroupActivity;
import com.example.partymaker.ui.features.groups.discovery.PublicGroupsActivity;

public class NavigationManager {

  /**
   * Sets up the bottom navigation with the current page highlighted
   *
   * @param activity The activity
   * @param currentPage The current page identifier
   */
  public static void setupBottomNavigation(Activity activity, String currentPage) {
    BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);

    if (bottomNavigationView != null) {
      // Set current page highlighting
      setSelectedItem(bottomNavigationView, currentPage);

      // Set up navigation listener
      bottomNavigationView.setOnItemSelectedListener(item -> {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_profile) {
          navigateToProfile(activity);
          return true;
        } else if (itemId == R.id.nav_my_parties) {
          navigateToMyParties(activity);
          return true;
        } else if (itemId == R.id.nav_public_parties) {
          navigateToPublicParties(activity);
          return true;
        } else if (itemId == R.id.nav_create_group) {
          navigateToCreateGroup(activity);
          return true;
        }
        
        return false;
      });
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

  private static void setSelectedItem(BottomNavigationView bottomNavigationView, String currentPage) {
    switch (currentPage) {
      case "profile":
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        break;
      case "myparties":
        bottomNavigationView.setSelectedItemId(R.id.nav_my_parties);
        break;
      case "publicparties":
        bottomNavigationView.setSelectedItemId(R.id.nav_public_parties);
        break;
      case "creategroup":
        bottomNavigationView.setSelectedItemId(R.id.nav_create_group);
        break;
      case "none":
      default:
        // Don't select any item
        break;
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
