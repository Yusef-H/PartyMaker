package com.example.partymaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;

import java.util.HashMap;

public class AddGroup extends AppCompatActivity {
    private Button btnAddGroup, btnNext1, btnNext2, btnBack1, btnBack2, btnDone;
    private TextView tvPartyName, tvPartyLocation, tvPartyDate, tvGroupPicture, tvHours;
    private EditText etPartyName, etPartyLocation;
    private ImageView imgLogin, imgGroupPicture;
    private String GroupKey1, DaysSelected, MonthsSelected, YearsSelected,HoursSelected;
    private CheckBox cbGroupType;
    private Spinner spnDays, spnMonths, spnYears , spnHours;
    private ArrayAdapter<CharSequence> DaysAdapter, MonthsAdapter, YearsAdapter, HoursAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        //this 2 lines changes title's name
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#039694'>New Party</font>"));

        //set actionbar background
        Drawable d = getResources().getDrawable(R.drawable.background3);
        actionBar.setBackgroundDrawable(d);

        imgLogin = (ImageView) findViewById(R.id.imgLogin);
        imgGroupPicture = (ImageView) findViewById(R.id.imgGroupPicture);
        btnNext1 = (Button) findViewById(R.id.btnNext1);
        btnNext2 = (Button) findViewById(R.id.btnNext2);
        btnBack1 = (Button) findViewById(R.id.btnBack1);
        btnBack2 = (Button) findViewById(R.id.btnBack2);
        btnDone= (Button) findViewById(R.id.btnDone);
        btnAddGroup = (Button) findViewById(R.id.btnAddGroup);
        tvPartyName = (TextView) findViewById(R.id.tvPartyName);
        tvPartyLocation = (TextView) findViewById(R.id.tvPartyLocation);
        tvPartyDate = (TextView) findViewById(R.id.tvPartyDate);
        tvGroupPicture = (TextView) findViewById(R.id.tvGroupPicture);
        tvHours = (TextView) findViewById(R.id.tvHours);
        etPartyName = (EditText) findViewById(R.id.etPartyName);
        etPartyLocation = (EditText) findViewById(R.id.etPartyLocation);
        cbGroupType = (CheckBox) findViewById(R.id.cbGroupType);
        spnDays = (Spinner) findViewById(R.id.spnDateDay);
        spnMonths = (Spinner) findViewById(R.id.spnDateMonth);
        spnYears = (Spinner) findViewById(R.id.spnDateYear);
        spnHours = (Spinner) findViewById(R.id.spnHours);
        //spinner adapter for days
        DaysAdapter = ArrayAdapter.createFromResource(this, R.array.array_days, android.R.layout.simple_spinner_item);
        DaysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for months
        MonthsAdapter = ArrayAdapter.createFromResource(this, R.array.array_months, android.R.layout.simple_spinner_item);
        MonthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for years
        YearsAdapter = ArrayAdapter.createFromResource(this, R.array.array_years, android.R.layout.simple_spinner_item);
        YearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner adapter for hours
        HoursAdapter = ArrayAdapter.createFromResource(this, R.array.array_hours, android.R.layout.simple_spinner_item);
        HoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set adapter for each spinner
        spnDays.setAdapter(DaysAdapter);
        spnMonths.setAdapter(MonthsAdapter);
        spnYears.setAdapter(YearsAdapter);
        spnHours.setAdapter(HoursAdapter);

        evantHandler();
    }

    private void evantHandler() {
        btnNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPartyName.setVisibility(View.INVISIBLE);
                etPartyName.setVisibility(View.INVISIBLE);
                tvPartyLocation.setVisibility(View.VISIBLE);
                etPartyLocation.setVisibility(View.VISIBLE);
                btnNext1.setVisibility(View.INVISIBLE);
                btnNext2.setVisibility(View.VISIBLE);
                btnBack1.setVisibility(View.VISIBLE);

            }
        });
        btnNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPartyLocation.setVisibility(View.INVISIBLE);
                etPartyLocation.setVisibility(View.INVISIBLE);
                cbGroupType.setVisibility(View.INVISIBLE);
                tvPartyDate.setVisibility(View.VISIBLE);
                tvHours.setVisibility(View.VISIBLE);
                spnDays.setVisibility(View.VISIBLE);
                spnMonths.setVisibility(View.VISIBLE);
                spnYears.setVisibility(View.VISIBLE);
                spnHours.setVisibility(View.VISIBLE);
                btnNext2.setVisibility(View.INVISIBLE);
                btnAddGroup.setVisibility(View.VISIBLE);
                btnBack1.setVisibility(View.INVISIBLE);
                btnBack2.setVisibility(View.VISIBLE);
            }
        });
        btnBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPartyName.setVisibility(View.VISIBLE);
                etPartyName.setVisibility(View.VISIBLE);
                tvPartyLocation.setVisibility(View.INVISIBLE);
                etPartyLocation.setVisibility(View.INVISIBLE);
                btnNext1.setVisibility(View.VISIBLE);
                btnNext2.setVisibility(View.INVISIBLE);
                btnBack1.setVisibility(View.INVISIBLE);
            }
        });
        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPartyLocation.setVisibility(View.VISIBLE);
                etPartyLocation.setVisibility(View.VISIBLE);
                cbGroupType.setVisibility(View.VISIBLE);
                tvPartyDate.setVisibility(View.INVISIBLE);
                tvHours.setVisibility(View.INVISIBLE);
                spnDays.setVisibility(View.INVISIBLE);
                spnMonths.setVisibility(View.INVISIBLE);
                spnYears.setVisibility(View.INVISIBLE);
                spnHours.setVisibility(View.INVISIBLE);
                btnNext2.setVisibility(View.VISIBLE);
                btnAddGroup.setVisibility(View.INVISIBLE);
                btnBack1.setVisibility(View.VISIBLE);
                btnBack2.setVisibility(View.INVISIBLE);
            }
        });
        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Group p = new Group();
                //Group's name
                p.setGroupName(etPartyName.getText().toString());
                //admin name
                p.setAdminKey(DBref.Auth.getCurrentUser().getEmail().replace('.', ' '));

                //set group's type if 0 so Public group if 1 so Private group
                if (cbGroupType.isChecked()) {
                    p.setGroupType(1);
                } else {
                    p.setGroupType(0);
                }

                //set if people can add their friends
                if (p.getGroupType()==0) //if group is public
                    p.setCanAdd(true); // so people can add
                else if (p.getGroupType()==1) //if group is private
                    p.setCanAdd(false); //so people cant add

                //Time when opened Group
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());
                p.setCreatedAt(strDate);

                //set Group's entry price to 0 (free)
                p.setGroupPrice("0");

                //Group's Location
                p.setGroupLocation(etPartyLocation.getText().toString());

                //Group's date
                p.setGroupDays(DaysSelected);
                p.setGroupMonths(MonthsSelected);
                p.setGroupYears(YearsSelected);
                p.setGroupHours(HoursSelected);

                //create unique key for Group
                String GroupKey = DBref.refGroups.push().getKey();
                GroupKey1 = GroupKey;
                p.setGroupKey(GroupKey);

                //create unique key for FriendKey
                String FriendKey = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');

                //set key in database
                DBref.refGroups.child(GroupKey).setValue(p);

                // adding reference to FriendKeys with the admin's email
                HashMap<String, Object> result1 = new HashMap<>();
                result1.put(FriendKey, "true");
                DBref.refGroups.child(p.getGroupKey()).child("FriendKeys").updateChildren(result1);
                // adding reference to ComingKeys with the admin's email
                DBref.refGroups.child(p.getGroupKey()).child("ComingKeys").updateChildren(result1);

                //create empty HashMap for Chat
                HashMap<String, Object> result3 = new HashMap<>();
                DBref.refGroups.child(p.getGroupKey()).child("MessageKeys").updateChildren(result3);

                //write Group created successfully
                Toast.makeText(AddGroup.this, "Group successfully created", Toast.LENGTH_SHORT).show();

                //Design
                imgLogin.setVisibility(View.INVISIBLE);
                tvPartyDate.setVisibility(View.INVISIBLE);
                tvHours.setVisibility(View.INVISIBLE);
                spnDays.setVisibility(View.INVISIBLE);
                spnMonths.setVisibility(View.INVISIBLE);
                spnYears.setVisibility(View.INVISIBLE);
                spnHours.setVisibility(View.INVISIBLE);
                btnBack2.setVisibility(View.INVISIBLE);
                btnAddGroup.setVisibility(View.INVISIBLE);
                cbGroupType.setVisibility(View.INVISIBLE);
                imgGroupPicture.setVisibility(View.VISIBLE);
                tvGroupPicture.setVisibility(View.VISIBLE);
                btnDone.setVisibility(View.VISIBLE);

                //Set title in action bar - i chose no title
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(Html.fromHtml("<font color='#039694'>Set party's picture</font>"));

                //wait 2 seconds and after it makes text Disappeared
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        tvGroupPicture.setText("Tap on the picture above to set a profile picture");
                    }
                }, 3000);
            }
        });
        imgGroupPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);
                tvGroupPicture.setVisibility(View.INVISIBLE);
            }
        });
        spnDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DaysSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MonthsSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnYears.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                YearsSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HoursSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                Uri uri = data.getData();
                if (null != uri) {

                    ((ImageView) findViewById(R.id.imgGroupPicture)).setImageURI(uri);

                    DBref.refStorage.child("Groups/" + GroupKey1).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(AddGroup.this, "saved", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(AddGroup.this, "error while saving ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
