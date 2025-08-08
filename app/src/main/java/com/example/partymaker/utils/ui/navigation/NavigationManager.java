package com.example.partymaker.utils.ui.navigation;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.example.partymaker.R;
import com.example.partymaker.ui.features.auxiliary.profile.EditProfileActivity;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.ui.features.groups.creation.CreateGroupActivity;
import com.example.partymaker.ui.features.groups.discovery.PublicGroupsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationManager {

  private static final String TAG = "NavigationManager";

  /**
   * Sets up the bottom navigation with the current page highlighted
   *
   * @param activity The activity
   * @param currentPage The current page identifier
   */
  public static void setupBottomNavigation(Activity activity, String currentPage) {
    Log.d(TAG, "Setting up bottom navigation for activity: " + activity.getClass().getSimpleName());
    BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
    
    // If not found directly, try to find within the include
    if (bottomNavigationView == null) {
      Log.d(TAG, "BottomNavigationView not found directly, searching in include...");
      android.view.View includeView = activity.findViewById(R.id.bottomNavigationInclude);
      if (includeView != null) {
        bottomNavigationView = includeView.findViewById(R.id.bottom_navigation);
        Log.d(TAG, "Include found, bottomNavigationView: " + (bottomNavigationView != null));
      } else {
        Log.d(TAG, "Include not found");
      }
    }

    if (bottomNavigationView != null) {
      Log.d(TAG, "BottomNavigationView found, setting up listener");
      // Set current page highlighting
      setSelectedItem(bottomNavigationView, currentPage);

      // Set up navigation listener
      bottomNavigationView.setOnItemSelectedListener(
          item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom navigation item selected: " + itemId);

            if (itemId == R.id.nav_profile) {
              Log.d(TAG, "Navigating to profile");
              navigateToProfile(activity);
              return true;
            } else if (itemId == R.id.nav_my_parties) {
              Log.d(TAG, "Navigating to my parties");
              navigateToMyParties(activity);
              return true;
            } else if (itemId == R.id.nav_public_parties) {
              Log.d(TAG, "Navigating to public parties");
              navigateToPublicParties(activity);
              return true;
            } else if (itemId == R.id.nav_create_group) {
              Log.d(TAG, "Navigating to create group");
              navigateToCreateGroup(activity);
              return true;
            }

            Log.d(TAG, "Unknown navigation item: " + itemId);
            return false;
          });
    } else {
      Log.e(TAG, "BottomNavigationView not found!");
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

  private static void setSelectedItem(
      BottomNavigationView bottomNavigationView, String currentPage) {
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
    if (activity instanceof EditProfileActivity) {
      Log.d(TAG, "Already in EditProfileActivity, not navigating");
      return;
    }
    Log.d(TAG, "Starting EditProfileActivity");
    Intent intent = new Intent(activity, EditProfileActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToMyParties(Activity activity) {
    if (activity instanceof MainActivity) {
      Log.d(TAG, "Already in MainActivity, not navigating");
      return;
    }
    Log.d(TAG, "Starting MainActivity");
    Intent intent = new Intent(activity, MainActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToPublicParties(Activity activity) {
    if (activity instanceof PublicGroupsActivity) {
      Log.d(TAG, "Already in PublicGroupsActivity, not navigating");
      return;
    }
    Log.d(TAG, "Starting PublicGroupsActivity");
    Intent intent = new Intent(activity, PublicGroupsActivity.class);
    activity.startActivity(intent);
  }

  private static void navigateToCreateGroup(Activity activity) {
    if (activity instanceof CreateGroupActivity) {
      Log.d(TAG, "Already in CreateGroupActivity, not navigating");
      return;
    }
    Log.d(TAG, "Starting CreateGroupActivity");
    Intent intent = new Intent(activity, CreateGroupActivity.class);
    activity.startActivity(intent);
  }
}
