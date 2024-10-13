package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
        setContentView(R.layout.activity_uam_termsandcondition);

        HelpKonnectLogo = findViewById(R.id.HelpKonnectLogo);
        startapp = findViewById(R.id.touseraccount);
        //Animation to float up the logo after user clicks the app
        Animation floatUpAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_float_up);
        long animationDuration = floatUpAnimation.getDuration(); // Get the animation duration

        overridePendingTransition(R.anim.appear_in, R.anim.disappear_out);

        startapp.setOnClickListener(v -> {

            startapp.setEnabled(false);
            startapp.setVisibility(View.GONE);

            HelpKonnectLogo.startAnimation(floatUpAnimation);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                HelpKonnectLogo.setVisibility(View.GONE);
                Intent tosignin = new Intent(SplashActivity.this, UserAccountManagementActivity.class);
                startActivity(tosignin);
               //Need Fix Later
                finish();

            }, animationDuration + 500);
        });

    }
}
