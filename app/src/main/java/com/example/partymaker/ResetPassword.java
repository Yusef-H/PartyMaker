package com.example.partymaker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.example.partymaker.data.DBref;

public class ResetPassword extends AppCompatActivity {
    private Button btnWhite, btnBlack, btnReset, btnHelp, btnHide;
    private RelativeLayout rltReset;
    private TextView tvForgotPass, tvHelp, tvInstructions, tvHide;
    private EditText etInputEmail;
    private ImageView imgWhiteCake;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //this 2 lines disables the action bar only in this activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        btnBlack = (Button) findViewById(R.id.btnBlack);
        btnWhite = (Button) findViewById(R.id.btnWhite);
        btnHelp = (Button) findViewById(R.id.btnHelp);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnHide = (Button) findViewById(R.id.btnHide);
        etInputEmail = (EditText) findViewById(R.id.etInputEmail);
        tvForgotPass = (TextView) findViewById(R.id.tvForgotPass);
        tvHelp = (TextView) findViewById(R.id.tvHelp);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        tvHide = (TextView) findViewById(R.id.tvHide);
        rltReset = (RelativeLayout) findViewById(R.id.rltReset);
        imgWhiteCake = (ImageView) findViewById(R.id.imgWhiteCake);

        eventHandler();
    }

    private void ResetPass() {
        final String email = etInputEmail.getText().toString(); // the user email
        if (email.matches("")) {
            Toast.makeText(ResetPassword.this, "input email", Toast.LENGTH_SHORT).show();
        }
        else {
            DBref.Auth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPassword.this, "Email successfully sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPassword.this, "Email Error sending", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void eventHandler() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPass();
            }
        });
        btnWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code that invert color(In this case rom White/Black to Black/White)
                final float[] NEGATIVE = {-1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0}; //every number is a color (Red,Green,Blue,Alpha)
                imgWhiteCake.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));//set color to the color above
                imgWhiteCake.setColorFilter(0x00ff0000, PorterDuff.Mode.ADD);//invert back
                imgWhiteCake = (ImageView) findViewById(R.id.imgWhiteCake);
                rltReset.setBackgroundColor(Color.WHITE);
                tvForgotPass.setTextColor(Color.BLACK);
                tvHelp.setTextColor(Color.BLACK);
                tvInstructions.setTextColor(Color.BLACK);
                tvHide.setTextColor(Color.BLACK);
                etInputEmail.setHintTextColor(Color.BLACK);
                etInputEmail.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                etInputEmail.setTextColor(Color.BLACK);
                btnReset.setTextColor(Color.BLACK);
                btnHelp.setTextColor(Color.BLACK);
                btnHide.setTextColor(Color.BLACK);
                btnWhite.setVisibility(view.INVISIBLE);
                btnBlack.setVisibility(View.VISIBLE);
            }
        });
        btnBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code that invert color(In this case rom Black/White to White/Black)
                final float[] NEGATIVE = {-1.0f, 0, 0, 0, 255, 0, -1.0f, 0, 0, 255, 0, 0, -1.0f, 0, 255, 0, 0, 0, 1.0f, 0}; //every number is a color (Red,Green,Blue,Alpha)
                imgWhiteCake.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));//set color to the color above
                rltReset.setBackgroundColor(Color.BLACK);
                tvForgotPass.setTextColor(Color.WHITE);
                tvHelp.setTextColor(Color.WHITE);
                tvInstructions.setTextColor(Color.WHITE);
                tvHide.setTextColor(Color.WHITE);
                etInputEmail.setHintTextColor(Color.WHITE);
                etInputEmail.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                etInputEmail.setTextColor(Color.WHITE);
                btnReset.setTextColor(Color.WHITE);
                btnHelp.setTextColor(Color.WHITE);
                btnHide.setTextColor(Color.WHITE);
                btnBlack.setVisibility(View.INVISIBLE);
                btnWhite.setVisibility(view.VISIBLE);
            }
        });
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvInstructions.setVisibility(View.VISIBLE);
                tvHelp.setVisibility(view.INVISIBLE);
                btnHelp.setVisibility(view.INVISIBLE);
                tvHide.setVisibility(view.VISIBLE);
                btnHide.setVisibility(view.VISIBLE);
            }
        });
        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvInstructions.setVisibility(View.INVISIBLE);
                tvHelp.setVisibility(view.VISIBLE);
                btnHelp.setVisibility(view.VISIBLE);
                tvHide.setVisibility(view.INVISIBLE);
                btnHide.setVisibility(view.INVISIBLE);
            }
        });

    }
}
