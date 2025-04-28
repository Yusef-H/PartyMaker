package com.example.partymaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
import com.example.partymaker.data.DBref;
import com.squareup.picasso.Picasso;

public class EditProfile extends AppCompatActivity {
    private ImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Change title Name and Color
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#505050'>Edit Profile</font>"));

        //set actionbar background
        Drawable d = getResources().getDrawable(R.drawable.background4);
        actionBar.setBackgroundDrawable(d);

        //connectionU
        imgProfile = (ImageView) findViewById(R.id.imgProfile);

        String User = DBref.Auth.getCurrentUser().getEmail().toString();
        String email = User.replace('.', ' ');

        DBref.refStorage.child("Users/" + email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imgProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        evantHandler();
    }

    private void evantHandler() {
        //imgProfile on click
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                Uri uri = data.getData();
                if (null != uri) {
                    ((ImageView) findViewById(R.id.imgProfile)).setImageURI(uri);
                    String UserImageProfile = DBref.Auth.getCurrentUser().getEmail();
                    String email = UserImageProfile.replace('.', ' ');

                    DBref.refStorage.child("Users/" + email).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditProfile.this, "saved", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(EditProfile.this, "error while saving ", Toast.LENGTH_SHORT).show();
                        }
                    });
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
            goToNextActivity = new Intent(getApplicationContext(), AddGroup.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idEditProfile) {
            goToNextActivity = new Intent(getApplicationContext(), EditProfile.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idPublicParties) {
            goToNextActivity = new Intent(getApplicationContext(), PublicGroups.class);
            startActivity(goToNextActivity);
        } else if (item.getItemId() == R.id.idLogout) {
            DBref.Auth.signOut();
            DBref.CurrentUser = null;
            goToNextActivity = new Intent(getApplicationContext(), Login.class);
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
