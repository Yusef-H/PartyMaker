package com.example.partymaker.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.utils.auth.AuthHelper;
import com.example.partymaker.utils.data.Common;
import com.example.partymaker.utils.data.ExtrasMetadata;
import com.example.partymaker.utils.ui.MapUtilities;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class AdminOptionsActivity extends AppCompatActivity implements OnMapReadyCallback {
  private static final String TAG = "AdminOptionsActivity";
  private LinearLayout mainContent;
  private String AdminKey,
      GroupKey,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      CreatedAt,
      GroupPrice,
      UserKey; // Add UserKey for admin verification
  private int GroupType;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private boolean CanAdd;
  private CardView CardPrice, CardLocation;
  private GoogleMap map;
  private LatLng chosenLatLng;
  private Button saveLocationButton;
  private FrameLayout mapContainer;
  private boolean isAdminVerified = false; // Track admin verification status

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_options);

    // Initialize Places API with proper error handling
    String apiKey = Common.getApiKey(this, "MAPS_KEY");
    if (apiKey.isEmpty()) {
      // Use a default key from resources or show an error message
      Toast.makeText(
              this,
              "Google Maps API key not found. Some features may not work properly.",
              Toast.LENGTH_LONG)
          .show();
      // Try to get key from manifest metadata as fallback
      try {
        apiKey =
            getPackageManager()
                .getApplicationInfo(
                    getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
                .metaData
                .getString("com.google.android.geo.API_KEY");
      } catch (Exception e) {
        apiKey = "YOUR_DEFAULT_API_KEY"; // Replace with your actual key if needed
      }
    }

    if (!Places.isInitialized()) {
      try {
        if (apiKey != null) {
          Places.initialize(getApplicationContext(), apiKey);
        } else {
          Log.d(TAG, "Null API Key");
        }
      } catch (IllegalArgumentException e) {
        Toast.makeText(this, "Error initializing Google Maps: " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
        // Continue without maps functionality
      }
    }

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

    // Get current user key for admin verification
    try {
      UserKey = AuthHelper.getCurrentUserKey(this);
    } catch (Exception e) {
      Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
      finish();
      return;
    }

    // Get Values from GroupScreen By intent + connection between intent and current
    // activity objects
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    GroupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    AdminKey = extras.getAdminKey();
    CreatedAt = extras.getCreatedAt();
    GroupPrice = extras.getGroupPrice();
    GroupType = extras.getGroupType();
    CanAdd = extras.isCanAdd();
    FriendKeys = extras.getFriendKeys();
    ComingKeys = extras.getComingKeys();
    MessageKeys = extras.getMessageKeys();

    // Verify admin status before allowing access
    verifyAdminStatus();
  }

  private void verifyAdminStatus() {
    // Show loading message
    Toast.makeText(this, "Verifying admin permissions...", Toast.LENGTH_SHORT).show();

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group != null
                && group.getAdminKey() != null
                && group.getAdminKey().equals(UserKey)) {
              // User is verified as admin
              isAdminVerified = true;
              // Update local admin key to match server data
              AdminKey = group.getAdminKey();
              // Initialize UI after verification
              initializeUI();
            } else {
              // User is not admin - deny access
              Toast.makeText(
                      AdminOptionsActivity.this,
                      "Access denied. Only group admin can access this page.",
                      Toast.LENGTH_LONG)
                  .show();
              finish();
            }
          }

          @Override
          public void onError(String errorMessage) {
            Toast.makeText(
                    AdminOptionsActivity.this,
                    "Failed to verify admin status: " + errorMessage,
                    Toast.LENGTH_LONG)
                .show();
            finish();
          }
        });
  }

  private void initializeUI() {
    // connection between XML and AdminOptions
    mainContent = findViewById(R.id.mainContent);
    GridLayout myGrid = findViewById(R.id.MyGrid);
    TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);
    CardPrice = findViewById(R.id.CardPrice);
    CardLocation = findViewById(R.id.CardLocation);
    saveLocationButton = findViewById(R.id.saveLocation);
    mapContainer = findViewById(R.id.mapContainer);
    // Added back button
    ImageButton btnBack = findViewById(R.id.btnBack); // Initialize back button

    // Set up back button click listener
    if (btnBack != null) {
      btnBack.setOnClickListener(v -> navigateBackToPartyMain());
    }

    wireMapComponents();

    // settings + things to see when activity starts
    tvAdminEmail.setText(AdminKey.replace(' ', '.'));

    // start AdminOptions
    wireAdminOptions(myGrid);

    eventHandler();
  }

  private void navigateBackToPartyMain() {
    Intent intent = new Intent(this, PartyMainActivity.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            GroupName,
            GroupKey,
            GroupDay,
            GroupMonth,
            GroupYear,
            GroupHour,
            GroupLocation,
            AdminKey,
            CreatedAt,
            GroupPrice,
            GroupType,
            CanAdd,
            FriendKeys,
            ComingKeys,
            MessageKeys);
    Common.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish(); // Close this activity to prevent it from staying in the back stack
  }

  @Override
  public void onBackPressed() {
    // Call super first
    super.onBackPressed();
    // Handle back button press to ensure proper navigation
    navigateBackToPartyMain();
  }

  private void eventHandler() {
    // Check admin verification before allowing any operations
    if (!isAdminVerified) {
      Toast.makeText(this, "Admin verification required", Toast.LENGTH_SHORT).show();
      return;
    }

    CardPrice.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(AdminOptionsActivity.this);
          edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
          edittext.setText(GroupPrice);
          AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptionsActivity.this);
          alert.setMessage("Input new price below");
          alert.setTitle("Change party's entry price");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change price",
              (dialog, whichButton) -> {
                // Double-check admin status before making changes
                if (!isAdminVerified || !AdminKey.equals(UserKey)) {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Access denied. Admin verification failed.",
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }
                // if pressed changed name
                GroupPrice = edittext.getText().toString();
                DBRef.refGroups.child(GroupKey).child("groupPrice").setValue(GroupPrice);
                Toast.makeText(AdminOptionsActivity.this, "Price Changed", Toast.LENGTH_SHORT)
                    .show();
              });

          alert.setNegativeButton(
              "Back",
              (dialog, whichButton) -> {
                // what ever you want to do with Back.
              });

          alert.show();
        });

    CardLocation.setOnClickListener(
        v -> {
          // Double-check admin status before allowing location changes
          if (!isAdminVerified || !AdminKey.equals(UserKey)) {
            Toast.makeText(
                    AdminOptionsActivity.this,
                    "Access denied. Admin verification failed.",
                    Toast.LENGTH_LONG)
                .show();
            return;
          }

          mainContent.setVisibility(View.INVISIBLE);
          MapUtilities.centerMapOnChosenPlace(
              map,
              Place.builder()
                  .setLatLng(MapUtilities.decodeStringLocationToCoordinates(GroupLocation))
                  .build());
          mapContainer.setVisibility(View.VISIBLE);

          saveLocationButton.setOnClickListener(
              v1 -> {
                // Triple-check admin status before saving location
                if (!isAdminVerified || !AdminKey.equals(UserKey)) {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Access denied. Admin verification failed.",
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }

                if (chosenLatLng != null) {
                  String locationValue =
                      MapUtilities.encodeCoordinatesToStringLocation(chosenLatLng);
                  DBRef.refGroups.child(GroupKey).child("groupLocation").setValue(locationValue);
                  GroupLocation = locationValue;
                  Toast.makeText(AdminOptionsActivity.this, "Location Changed", Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Warning: You have not set an address for your party.",
                          Toast.LENGTH_LONG)
                      .show();
                }
                mainContent.setVisibility(View.VISIBLE);
                mapContainer.setVisibility(View.INVISIBLE);
              });
        });
  }

  private void wireAdminOptions(GridLayout MyGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < MyGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) MyGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            Intent intent;
            // if (finalI == 0)  open 1,1 (1) Change Location
            if (finalI == 1) // open 1,2 (2) Change Date
            {
              // intent from AdminOptions to ChangeDate
              intent = new Intent(getBaseContext(), ChangeDateActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      GroupName,
                      GroupKey,
                      GroupDay,
                      GroupMonth,
                      GroupYear,
                      GroupHour,
                      GroupLocation,
                      AdminKey,
                      CreatedAt,
                      GroupPrice,
                      GroupType,
                      CanAdd,
                      FriendKeys,
                      ComingKeys,
                      MessageKeys);
              Common.addExtrasToIntent(intent, extras);
              startActivity(intent);
            } else if (finalI == 2) // open 2,1 (3) Delete People
            {
              intent = new Intent(getBaseContext(), FriendsRemoveActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      GroupName,
                      GroupKey,
                      GroupDay,
                      GroupMonth,
                      GroupYear,
                      GroupHour,
                      GroupLocation,
                      AdminKey,
                      CreatedAt,
                      GroupPrice,
                      GroupType,
                      CanAdd,
                      FriendKeys,
                      ComingKeys,
                      MessageKeys);
              Common.addExtrasToIntent(intent, extras);
              intent.putExtra("groupID", GroupKey); // Add groupID for FriendsRemoveActivity
              startActivity(intent);
            }
            // else if (finalI == 3) // open 2,2 (4) Change Entry Price
            else if (finalI == 4) // open 3,1 (5) Group Options
            {
              // intent to GroupOptions Activity with Values
              intent = new Intent(getBaseContext(), AdminSettingsActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      GroupName,
                      GroupKey,
                      GroupDay,
                      GroupMonth,
                      GroupYear,
                      GroupHour,
                      GroupLocation,
                      AdminKey,
                      CreatedAt,
                      GroupPrice,
                      GroupType,
                      CanAdd,
                      FriendKeys,
                      ComingKeys,
                      MessageKeys);
              Common.addExtrasToIntent(intent, extras);
              startActivity(intent);
            } else if (finalI == 5) // open 2,2 (4) Back
            {
              // Navigate back to PartyMainActivity
              navigateBackToPartyMain();
            }
          });
    }
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    this.map = googleMap;
    // Wherever the user clicks gets stored in chosenLatLng
    map.setOnMapClickListener(
        latlng -> {
          chosenLatLng = latlng;
          map.clear();
          map.addMarker(new MarkerOptions().position(latlng).title("Party here"));
        });
  }

  private void wireMapComponents() {
    SupportMapFragment mapFrag =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    assert mapFrag != null;
    mapFrag.getMapAsync(this);
    AutocompleteSupportFragment autocompleteFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
    assert autocompleteFragment != null;
    autocompleteFragment.setPlaceFields(
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

    autocompleteFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(@NonNull Place place) {
            chosenLatLng = MapUtilities.centerMapOnChosenPlace(map, place);
          }

          @Override
          public void onError(@NonNull Status status) {
            Toast.makeText(
                    AdminOptionsActivity.this,
                    "Search error: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
