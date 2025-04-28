package com.example.partymaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.data.DBref;

public class Splash extends AppCompatActivity {

    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        imgLogo = findViewById(R.id.imgLogo);

        //splash options
        Thread mSplashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        //connect res/tween to splash and picture
                        Animation myFadeInAnimation = AnimationUtils.loadAnimation(Splash.this, R.anim.tween);
                        imgLogo.startAnimation(myFadeInAnimation);
                        //time till go to next activity
                        wait(3000);
                    }
                } catch (InterruptedException ignored) {
                }
                finish();
                //retrieves the value of isChecked from cache which was set in Login (as boolean in Splash)
                SharedPreferences settings1 = getSharedPreferences("PREFS_NAME", 0);
                boolean isChecked = settings1.getBoolean("isChecked", false);
                //if account already signed - intent to main if not intent to login activity
                Intent intent = new Intent();
                //if account exist and RememberMe=True
                if (DBref.Auth.getCurrentUser() != null && isChecked) {
                    intent.setClass(Splash.this, MainActivity.class);
                } else
                    intent.setClass(Splash.this, Login.class);
                startActivity(intent);
            }
        };
        mSplashThread.start();
    }
}
