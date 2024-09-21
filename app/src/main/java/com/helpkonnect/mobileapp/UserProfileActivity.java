package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView journalBackButton, userProfile;
    private Button profileEditButton;
    private EditText userBioEditText, userfullname, emailEditText, houseUnitEditText, streetEditText, barangayEditText, cityEditText, provinceEditText, postalCodeEditText, countryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        journalBackButton = findViewById(R.id.journalBackButton);
        profileEditButton = findViewById(R.id.profileEditButton);

        userProfile = findViewById(R.id.userProfile);
        userBioEditText = findViewById(R.id.userBioEditText);
        userfullname = findViewById(R.id.userfullname);
        emailEditText = findViewById(R.id.emailEditText);

        houseUnitEditText = findViewById(R.id.houseUnitEditText);
        streetEditText = findViewById(R.id.streetEditText);
        barangayEditText = findViewById(R.id.barangayEditText);
        cityEditText = findViewById(R.id.cityEditText);
        provinceEditText = findViewById(R.id.provinceEditText);
        postalCodeEditText = findViewById(R.id.postalCodeEditText);
        countryEditText = findViewById(R.id.countryEditText);
    }
}