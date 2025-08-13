package com.example.partymaker.ui.features.groups.management;

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
import com.example.partymaker.ui.base.BaseActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.features.groups.main.PartyMainActivity;
import com.example.partymaker.ui.features.groups.members.FriendsRemoveActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.ui.maps.MapUtilitiesManager;
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

public class AdminOptionsActivity extends BaseActivity implements OnMapReadyCallback {
  // Constants
  private static final String TAG = "AdminOptionsActivity";
  private static final String API_KEY_METADATA = "com.google.android.geo.API_KEY";
  private static final String MAPS_KEY = "MAPS_KEY";

  // Error Messages
  private static final String ERROR_API_KEY_NOT_FOUND =
      "Google Maps API key not found. Some features may not work properly.";
  private static final String ERROR_AUTH_FAILED = "Authentication error. Please login again.";
  private static final String ERROR_MISSING_INTENT = "Missing intent data";
  private static final String ERROR_ACCESS_DENIED =
      "Access denied. Only group admin can access this page.";
  private static final String ERROR_ADMIN_VERIFICATION = "Admin verification failed.";
  private static final String ERROR_LOCATION_NOT_SET =
      "Warning: You have not set an address for your party.";

  // Success Messages
  private static final String MSG_VERIFYING_ADMIN = "Verifying admin permissions...";
  private static final String MSG_PRICE_CHANGED = "Price Changed";
  private static final String MSG_LOCATION_CHANGED = "Location Changed";
  private static final String MSG_ADMIN_VERIFICATION_REQUIRED = "Admin verification required";
  // UI Components
  private LinearLayout mainContent;
  private CardView cardPrice;
  private CardView cardLocation;
  private Button saveLocationButton;
  private FrameLayout mapContainer;

  // Group Data
  private String adminKey;
  private String groupKey;
  private String groupName;
  private String groupDay;
  private String groupMonth;
  private String groupYear;
  private String groupHour;
  private String groupLocation;
  private String createdAt;
  private String groupPrice;
  private String userKey;
  private int groupType;
  private HashMap<String, Object> friendKeys;
  private HashMap<String, Object> comingKeys;
  private HashMap<String, Object> messageKeys;
  private boolean canAdd;

  // Map Components
  private GoogleMap map;
  private LatLng chosenLatLng;

  // Admin Verification
  private boolean isAdminVerified = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_options);

    initializePlacesApi();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    if (!authenticateUser()) {
      return;
    }

    if (!loadIntentData()) {
      return;
    }

    verifyAdminStatus();
  }

  private void initializePlacesApi() {
    String apiKey = IntentExtrasManager.getApiKey(this, MAPS_KEY);
    if (apiKey.isEmpty()) {
      Toast.makeText(this, ERROR_API_KEY_NOT_FOUND, Toast.LENGTH_LONG).show();
      try {
        apiKey =
            getPackageManager()
                .getApplicationInfo(
                    getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
                .metaData
                .getString(API_KEY_METADATA);
      } catch (Exception e) {
        Log.e(TAG, "Failed to retrieve API key from metadata", e);
      }
    }

    if (!Places.isInitialized() && apiKey != null) {
      try {
        Places.initialize(getApplicationContext(), apiKey);
      } catch (IllegalArgumentException e) {
        Toast.makeText(this, "Error initializing Google Maps: " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  private boolean authenticateUser() {
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      return true;
    } catch (Exception e) {
      Toast.makeText(this, ERROR_AUTH_FAILED, Toast.LENGTH_LONG).show();
      finish();
      return false;
    }
  }

  private boolean loadIntentData() {
    ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, ERROR_MISSING_INTENT, Toast.LENGTH_SHORT).show();
      finish();
      return false;
    }

    groupName = extras.getGroupName();
    groupKey = extras.getGroupKey();
    groupDay = extras.getGroupDays();
    groupMonth = extras.getGroupMonths();
    groupYear = extras.getGroupYears();
    groupHour = extras.getGroupHours();
    groupLocation = extras.getGroupLocation();
    adminKey = extras.getAdminKey();
    createdAt = extras.getCreatedAt();
    groupPrice = extras.getGroupPrice();
    groupType = extras.getGroupType();
    canAdd = extras.isCanAdd();
    friendKeys = extras.getFriendKeys();
    comingKeys = extras.getComingKeys();
    messageKeys = extras.getMessageKeys();
    return true;
  }

  private void verifyAdminStatus() {
    Toast.makeText(this, MSG_VERIFYING_ADMIN, Toast.LENGTH_SHORT).show();

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group != null
                && group.getAdminKey() != null
                && group.getAdminKey().equals(userKey)) {
              isAdminVerified = true;
              adminKey = group.getAdminKey();
              initializeUI();
            } else {
              Toast.makeText(AdminOptionsActivity.this, ERROR_ACCESS_DENIED, Toast.LENGTH_LONG)
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
    mainContent = findViewById(R.id.mainContent);
    GridLayout myGrid = findViewById(R.id.MyGrid);
    TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);
    cardPrice = findViewById(R.id.CardPrice);
    cardLocation = findViewById(R.id.CardLocation);
    saveLocationButton = findViewById(R.id.saveLocation);
    mapContainer = findViewById(R.id.mapContainer);
    ImageButton btnBack = findViewById(R.id.btnBack);

    if (btnBack != null) {
      btnBack.setOnClickListener(v -> navigateBackToPartyMain());
    }

    wireMapComponents();
    tvAdminEmail.setText(adminKey.replace(' ', '.'));
    wireAdminOptions(myGrid);
    eventHandler();
  }

  private void navigateBackToPartyMain() {
    Intent intent = new Intent(this, PartyMainActivity.class);
    ExtrasMetadata extras =
        new ExtrasMetadata(
            groupName,
            groupKey,
            groupDay,
            groupMonth,
            groupYear,
            groupHour,
            groupLocation,
            adminKey,
            createdAt,
            groupPrice,
            groupType,
            canAdd,
            friendKeys,
            comingKeys,
            messageKeys);
    IntentExtrasManager.addExtrasToIntent(intent, extras);
    startActivity(intent);
    finish();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    navigateBackToPartyMain();
  }

  private void eventHandler() {
    if (!isAdminVerified) {
      Toast.makeText(this, MSG_ADMIN_VERIFICATION_REQUIRED, Toast.LENGTH_SHORT).show();
      return;
    }

    cardPrice.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(AdminOptionsActivity.this);
          edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
          edittext.setText(groupPrice);
          AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptionsActivity.this);
          alert.setMessage("Input new price below");
          alert.setTitle("Change party's entry price");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change price",
              (dialog, whichButton) -> {
                if (!isAdminVerified || !adminKey.equals(userKey)) {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Access denied. Admin verification failed.",
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }
                groupPrice = edittext.getText().toString();
                DBRef.refGroups.child(groupKey).child("groupPrice").setValue(groupPrice);
                Toast.makeText(AdminOptionsActivity.this, MSG_PRICE_CHANGED, Toast.LENGTH_SHORT)
                    .show();
              });

          alert.setNegativeButton(
              "Back",
              (dialog, whichButton) -> {
                // what ever you want to do with Back.
              });

          alert.show();
        });

    cardLocation.setOnClickListener(
        v -> {
          if (!isAdminVerified || !adminKey.equals(userKey)) {
            Toast.makeText(
                    AdminOptionsActivity.this,
                    "Access denied. Admin verification failed.",
                    Toast.LENGTH_LONG)
                .show();
            return;
          }

          mainContent.setVisibility(View.INVISIBLE);
          MapUtilitiesManager.centerMapOnChosenPlace(
              map,
              Place.builder()
                  .setLatLng(MapUtilitiesManager.decodeStringLocationToCoordinates(groupLocation))
                  .build());
          mapContainer.setVisibility(View.VISIBLE);

          saveLocationButton.setOnClickListener(
              v1 -> {
                if (!isAdminVerified || !adminKey.equals(userKey)) {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Access denied. Admin verification failed.",
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }

                if (chosenLatLng != null) {
                  String locationValue =
                      MapUtilitiesManager.encodeCoordinatesToStringLocation(chosenLatLng);
                  DBRef.refGroups.child(groupKey).child("groupLocation").setValue(locationValue);
                  groupLocation = locationValue;
                  Toast.makeText(
                          AdminOptionsActivity.this, MSG_LOCATION_CHANGED, Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          AdminOptionsActivity.this, ERROR_LOCATION_NOT_SET, Toast.LENGTH_LONG)
                      .show();
                }
                mainContent.setVisibility(View.VISIBLE);
                mapContainer.setVisibility(View.INVISIBLE);
              });
        });
  }

  private void wireAdminOptions(GridLayout myGrid) {
    for (int i = 0; i < myGrid.getChildCount(); i++) {
      final CardView cardView = (CardView) myGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            Intent intent;
            if (finalI == 1) {
              intent = new Intent(getBaseContext(), ChangeDateActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      groupName,
                      groupKey,
                      groupDay,
                      groupMonth,
                      groupYear,
                      groupHour,
                      groupLocation,
                      adminKey,
                      createdAt,
                      groupPrice,
                      groupType,
                      canAdd,
                      friendKeys,
                      comingKeys,
                      messageKeys);
              IntentExtrasManager.addExtrasToIntent(intent, extras);
              startActivity(intent);
            } else if (finalI == 2) {
              intent = new Intent(getBaseContext(), FriendsRemoveActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      groupName,
                      groupKey,
                      groupDay,
                      groupMonth,
                      groupYear,
                      groupHour,
                      groupLocation,
                      adminKey,
                      createdAt,
                      groupPrice,
                      groupType,
                      canAdd,
                      friendKeys,
                      comingKeys,
                      messageKeys);
              IntentExtrasManager.addExtrasToIntent(intent, extras);
              intent.putExtra("groupID", groupKey);
              startActivity(intent);
            } else if (finalI == 4) {
              intent = new Intent(getBaseContext(), AdminSettingsActivity.class);
              ExtrasMetadata extras =
                  new ExtrasMetadata(
                      groupName,
                      groupKey,
                      groupDay,
                      groupMonth,
                      groupYear,
                      groupHour,
                      groupLocation,
                      adminKey,
                      createdAt,
                      groupPrice,
                      groupType,
                      canAdd,
                      friendKeys,
                      comingKeys,
                      messageKeys);
              IntentExtrasManager.addExtrasToIntent(intent, extras);
              startActivity(intent);
            } else if (finalI == 5) {
              navigateBackToPartyMain();
            }
          });
    }
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    this.map = googleMap;
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
            chosenLatLng = MapUtilitiesManager.centerMapOnChosenPlace(map, place);
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

  @Override
  protected void clearActivityReferences() {
    // Clear UI components
    mainContent = null;
    cardPrice = null;
    cardLocation = null;
    saveLocationButton = null;
    mapContainer = null;

    // Clear data objects
    friendKeys = null;
    comingKeys = null;
    messageKeys = null;
    map = null;
    chosenLatLng = null;

    // Clear string fields
    adminKey = null;
    groupKey = null;
    groupName = null;
    groupDay = null;
    groupMonth = null;
    groupYear = null;
    groupHour = null;
    groupLocation = null;
    createdAt = null;
    groupPrice = null;
    userKey = null;
  }
}
