package com.helpkonnect.testpush;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserAccountManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useraccountmanagement);


        // Display the SigninFragment
        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new SigninFragment());
    }
}
