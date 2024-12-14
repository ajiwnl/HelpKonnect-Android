package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceActivity extends AppCompatActivity {
    private ImageView backButton;
    private SwitchCompat prefSwitch1, prefSwitch2, prefSwitch3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        backButton = findViewById(R.id.prefBackButton);
        prefSwitch1 = findViewById(R.id.journalSwitch);
        prefSwitch2 = findViewById(R.id.analysisSwitch);
        prefSwitch3 = findViewById(R.id.resSwitch);

        SharedPreferences sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        prefSwitch1.setChecked(sharedPreferences.getBoolean("journal_notification", true));
        prefSwitch2.setChecked(sharedPreferences.getBoolean("analysis_notification", true));
        prefSwitch3.setChecked(sharedPreferences.getBoolean("resources_notification", true));

        prefSwitch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("journal_notification", isChecked).apply();
        });
        prefSwitch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("analysis_notification", isChecked).apply();
        });
        prefSwitch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("resources_notification", isChecked).apply();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}
