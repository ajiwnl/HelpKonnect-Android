package com.helpkonnect.mobileapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class UserAccountManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useraccountmanagement);


        // Display the SigninFragment
        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new SigninFragment());
    }
}
