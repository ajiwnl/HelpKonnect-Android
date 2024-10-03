package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userFirstNameTextView, userEmailTextView, userLastNameTextView, userNameTextView, userBioTextView, userAddressTextView;

    private ImageView imgProfile;
    private Button editProfileButton, changeEmail;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private int strokeColor = Color.BLACK;
    private int strokeWidth = 1;


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
        imgProfile = findViewById(R.id.userProfile);
        changeEmail = findViewById(R.id.changeEmailBtn);


        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        if (currentUser != null) {

            loadUserData(userId);
        } else {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show();
        }

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userActivity(userId);
                showEditProfileFragment();
            }
        });

        // Set OnClickListener for the changeEmail button
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();  // Ends UserProfileActivity

            }
        });


    }

    private void showEditProfileFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        EditProfileDialogFragment editProfileFragment = new EditProfileDialogFragment();

        // Pass user data to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("firstName", userFirstNameTextView.getText().toString());
        bundle.putString("lastName", userLastNameTextView.getText().toString());
        bundle.putString("username", userNameTextView.getText().toString());
        bundle.putString("email", userEmailTextView.getText().toString());
        bundle.putString("bio", userBioTextView.getText().toString());
        bundle.putString("address", userAddressTextView.getText().toString());

        // Get the current user's ID
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = firestore.collection("credentials").document(userId);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Retrieve the image URL from Firestore
                    String imageUrl = documentSnapshot.getString("imageUrl");
                    bundle.putString("imageUrl", imageUrl); // Add imageUrl to the Bundle
                    editProfileFragment.setArguments(bundle); // Set the arguments after fetching imageUrl

                    editProfileFragment.show(fragmentManager, "EditProfile"); // Show the fragment
                } else {
                    Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(UserProfileActivity.this, "Failed to load image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        // Add Firestore real-time listener
        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(UserProfileActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Retrieve and display user data
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String username = documentSnapshot.getString("username");
                String email = documentSnapshot.getString("email");
                String bio = documentSnapshot.getString("bio");
                String address = documentSnapshot.getString("address");
                String imageUrl = documentSnapshot.getString("imageUrl");

                // Update TextViews with real-time data
                userFirstNameTextView.setText(firstName);
                userLastNameTextView.setText(lastName);
                userNameTextView.setText(username);
                userEmailTextView.setText(email);
                userBioTextView.setText(bio);
                userAddressTextView.setText(address);

                // Load the profile image using Picasso
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().
                            load(imageUrl).
                            transform(new CircleTransform(strokeWidth, strokeColor)).
                            placeholder(R.drawable.userprofileicon)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.userprofileicon); // Default profile picture if no URL
                }
            } else {
                Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userActivity(String userId) {
        // Get the current time
        Timestamp currentTime = Timestamp.now();  // Use Firestore's Timestamp class

        // Prepare Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to hold the lastActive field with the Timestamp
        Map<String, Object> data = new HashMap<>();
        data.put("lastActive", currentTime);  // Use Timestamp object directly

        // Create a custom document ID using the userId and timestamp
        SimpleDateFormat idSdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = idSdf.format(new Date());  // Use current date for document ID
        String documentId = userId + "_" + timestamp;

        // Add a new document with a custom ID (userId + timestamp) to the "userActivity" collection
        db.collection("userActivity")
                .document(documentId)  // Use the custom document ID
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // Successfully created a new activity document
                    Log.d("Firestore", "New activity recorded successfully with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Failed to record activity: " + e.getMessage());
                });
    }

}