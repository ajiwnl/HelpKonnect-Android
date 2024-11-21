package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout HelpKonnectLogo;
    private TextView startapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("YourPreferencesName", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        Log.d("Splash Value: isLoggedIn:", String.valueOf(isLoggedIn));
        if (isLoggedIn) {
            // If user is logged in, directly go to the MainScreenActivity
            Intent intent = new Intent(SplashActivity.this, MainScreenActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to the splash screen
            return;
        }

        // Otherwise, load the splash screen UI as usual
        setContentView(R.layout.activity_uam_termsandcondition);
        HelpKonnectLogo = findViewById(R.id.HelpKonnectLogo);
        startapp = findViewById(R.id.touseraccount);

        Animation floatUpAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_float_up);
        long animationDuration = floatUpAnimation.getDuration();

        overridePendingTransition(R.anim.appear_in, R.anim.disappear_out);

        startapp.setOnClickListener(v -> {
            startapp.setEnabled(false);
            startapp.setVisibility(View.GONE);

            HelpKonnectLogo.startAnimation(floatUpAnimation);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                HelpKonnectLogo.setVisibility(View.GONE);
                Intent tosignin = new Intent(SplashActivity.this, UserAccountManagementActivity.class);
                startActivity(tosignin);
                finish();
            }, animationDuration + 500);
        });
    }


}
