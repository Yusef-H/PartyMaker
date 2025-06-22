package com.example.partymaker.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.group.CreateGroupActivity;
import com.example.partymaker.ui.group.PublicGroupsActivity;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
  private ImageView imgProfile;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_profile);

    // Change title Name and Color
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar)
        .setTitle(Html.fromHtml("<font color='#505050'>Edit Profile</font>"));

    // set actionbar background
    Drawable d = getResources().getDrawable(R.drawable.bg_gray);
    actionBar.setBackgroundDrawable(d);

    // connectionU
    imgProfile = findViewById(R.id.imgProfile);

    String User = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
    String email = Objects.requireNonNull(User).replace('.', ' ');

    DBRef.refStorage
        .child("Users/" + email)
        .getDownloadUrl()
        .addOnSuccessListener(uri -> Picasso.get().load(uri).into(imgProfile))
        .addOnFailureListener(exception -> {});

    evantHandler();
  }

  private void evantHandler() {
    // imgProfile on click
    imgProfile.setOnClickListener(
        v -> {
          Intent i = new Intent();
          i.setType("image/*");
          i.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);
        });
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == 100) {
        Uri uri = data.getData();
        if (null != uri) {
          ((ImageView) findViewById(R.id.imgProfile)).setImageURI(uri);
          String UserImageProfile = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
          String email = Objects.requireNonNull(UserImageProfile).replace('.', ' ');

          DBRef.refStorage
              .child("Users/" + email)
              .putFile(uri)
              .addOnSuccessListener(
                  taskSnapshot ->
                      Toast.makeText(EditProfileActivity.this, "saved", Toast.LENGTH_SHORT).show())
              .addOnFailureListener(
                  exception ->
                      Toast.makeText(EditProfileActivity.this, "error while saving ", Toast.LENGTH_SHORT)
                          .show());
        }
      }
    }
  }

  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    Intent goToNextActivity;

    if (item.getItemId() == R.id.idMenu) {
      goToNextActivity = new Intent(getApplicationContext(), MainActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idAddProfile) {
      goToNextActivity = new Intent(getApplicationContext(), CreateGroupActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idEditProfile) {
      goToNextActivity = new Intent(getApplicationContext(), EditProfileActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idPublicParties) {
      goToNextActivity = new Intent(getApplicationContext(), PublicGroupsActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idLogout) {
      DBRef.Auth.signOut();
      DBRef.CurrentUser = null;
      goToNextActivity = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(goToNextActivity);
    }

    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
}
