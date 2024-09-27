package com.helpkonnect.mobileapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userFirstNameTextView, userEmailTextView, userLastNameTextView, userNameTextView, userBioTextView, userAddressTextView;
    private Button editProfileButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        userFirstNameTextView = findViewById(R.id.userfirstname);
        userEmailTextView = findViewById(R.id.useremail);
        userNameTextView = findViewById(R.id.username);
        userLastNameTextView = findViewById(R.id.userlastname);
        userBioTextView = findViewById(R.id.userbio);
        userAddressTextView = findViewById(R.id.useraddress);
        editProfileButton = findViewById(R.id.profileEditButton);


        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadUserData(userId);
        } else {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show();
        }

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileFragment();
            }
        });

    }

    private void showEditProfileFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        EditProfileDialogFragment editProfileFragment = new EditProfileDialogFragment();
        editProfileFragment.show(fragmentManager, "EditProfile");
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve the user data
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String username = documentSnapshot.getString("username");
                String email = documentSnapshot.getString("email");
                String bio = documentSnapshot.getString("bio");
                String address = documentSnapshot.getString("address");

                // Display the data in the corresponding TextViews
                userFirstNameTextView.setText(firstName);
                userLastNameTextView.setText(lastName);
                userNameTextView.setText(username);
                userEmailTextView.setText(email);
                userBioTextView.setText(bio);
                userAddressTextView.setText(address);
            } else {
                Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UserProfileActivity.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


}
