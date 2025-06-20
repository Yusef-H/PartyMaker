package com.example.partymaker;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.data.DBref;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.MapUtilities;
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

public class AdminOptions extends AppCompatActivity implements OnMapReadyCallback {
  private LinearLayout mainContent;
  private GridLayout MyGrid;
  private String AdminKey,
      GroupKey,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      CreatedAt,
      GroupPrice;
  private TextView tvAdminEmail;
  private int GroupType;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private boolean CanAdd;
  private CardView CardPrice, CardLocation;
  private GoogleMap map;
  private LatLng chosenLatLng;
  private Button saveLocationButton;
  private FrameLayout mapContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_options);

    if (!Places.isInitialized()) {
      Places.initialize(getApplicationContext(), Common.getApiKey(this, "MAPS_KEY"));
    }

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

    // Get Values from GroupScreen By intent + connection between intent and current
    // activity objects
    GroupName =
        Objects.requireNonNull(getIntent().getExtras()).getString("GroupName", "defaultKey");
    GroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
    GroupDay = getIntent().getExtras().getString("groupDays", "defaultKey");
    GroupMonth = getIntent().getExtras().getString("groupMonths", "defaultKey");
    GroupYear = getIntent().getExtras().getString("groupYears", "defaultKey");
    GroupHour = getIntent().getExtras().getString("groupHours", "defaultKey");
    GroupLocation = getIntent().getExtras().getString("groupLocation", "defaultKey");
    AdminKey = getIntent().getExtras().getString("adminKey", "defaultKey");
    CreatedAt = getIntent().getExtras().getString("createdAt", "defaultKey");
    GroupType =
        getIntent().getExtras().getInt("GroupType"); // if 0 so Public group if 1 so Private group
    GroupPrice = getIntent().getExtras().getString("GroupPrice");
    CanAdd = getIntent().getExtras().getBoolean("CanAdd");
    FriendKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("FriendKeys");
    ComingKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("ComingKeys");
    MessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");

    // connection between XML and AdminOptions
    mainContent = findViewById(R.id.mainContent);
    MyGrid = findViewById(R.id.MyGrid);
    tvAdminEmail = findViewById(R.id.tvAdminEmail);
    CardPrice = findViewById(R.id.CardPrice);
    CardLocation = findViewById(R.id.CardLocation);
    saveLocationButton = findViewById(R.id.saveLocation);
    mapContainer = findViewById(R.id.mapContainer);

    wireMapComponents();

    // settings + things to see when activity starts
    tvAdminEmail.setText(AdminKey.replace(' ', '.'));

    // start AdminOptions
    wireAdminOptions(MyGrid);

    eventHandler();
  }

  private void eventHandler() {
    CardPrice.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(AdminOptions.this);
          edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
          edittext.setText(GroupPrice);
          AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptions.this);
          alert.setMessage("Input new price below");
          alert.setTitle("Change party's entry price");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change price",
              (dialog, whichButton) -> {
                // if pressed changed name
                GroupPrice = edittext.getText().toString();
                DBref.refGroups.child(GroupKey).child("groupPrice").setValue(GroupPrice);
                Toast.makeText(AdminOptions.this, "Name Changed", Toast.LENGTH_SHORT).show();
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
          mainContent.setVisibility(View.INVISIBLE);
          MapUtilities.centerMapOnChosenPlace(
              map,
              Place.builder()
                  .setLatLng(MapUtilities.decodeStringLocationToCoordinates(GroupLocation))
                  .build());
          mapContainer.setVisibility(View.VISIBLE);

          saveLocationButton.setOnClickListener(
              v1 -> {
                if (chosenLatLng != null) {
                  String locationValue =
                      MapUtilities.encodeCoordinatesToStringLocation(chosenLatLng);
                  DBref.refGroups.child(GroupKey).child("groupLocation").setValue(locationValue);
                  GroupLocation = locationValue;
                  Toast.makeText(AdminOptions.this, "Location Changed", Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(
                          AdminOptions.this,
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
            Intent intent = new Intent(getBaseContext(), AdminOptions.class);
            if (finalI == 0) // open 1,1 (1) Change Location
            {
            } else if (finalI == 1) // open 1,2 (2) Change Date
            {
              // intent from AdminOptions to ChangeDate
              intent = new Intent(getBaseContext(), ChangeDate.class);
            } else if (finalI == 2) // open 2,1 (3) Delete People
            {
              intent = new Intent(getBaseContext(), DeletePeople.class);
            } else if (finalI == 3) // open 2,2 (4) Change Entry Price
            {
            } else if (finalI == 4) // open 3,1 (5) Group Options
            {
              // intent to GroupOptions Activity with Values
              intent = new Intent(getBaseContext(), GroupOptions.class);

            } else if (finalI == 5) // open 3,2 (6) Back
            {
              // intent back to GroupScreen Activity with Values
              intent = new Intent(getBaseContext(), GroupScreen.class);
            }
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
                    AdminOptions.this,
                    "Search error: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
