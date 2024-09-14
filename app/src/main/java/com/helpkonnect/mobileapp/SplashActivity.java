package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uam_termsandcondition);


        // Find and set up the "startapp" TextView
        TextView startapp = findViewById(R.id.touseraccount);

        // Set an OnClickListener for navigating to the UserAccountManagementActivity
        startapp.setOnClickListener(v -> {
            Intent tosignin = new Intent(SplashActivity.this, UserAccountManagementActivity.class);
            startActivity(tosignin);
        });
    }
}
