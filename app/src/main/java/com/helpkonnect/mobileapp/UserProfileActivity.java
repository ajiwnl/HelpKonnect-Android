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
    private ImageView imgProfile, backButton;
    private Button editProfileButton, changeEmail, changePassword;
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
        changePassword = findViewById(R.id.changePassBtn);
        backButton = findViewById(R.id.profileBackButton); // Corrected initialization

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user is signed in
        }

        backButton.setOnClickListener(v -> finish());

        editProfileButton.setOnClickListener(v -> {
            showEditProfileFragment();
        });

        changeEmail.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, UpdateEmailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        changePassword.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, ChangePasswordActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = firestore.collection("credentials").document(userId);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("imageUrl");
                    bundle.putString("imageUrl", imageUrl);
                    editProfileFragment.setArguments(bundle);
                    editProfileFragment.show(fragmentManager, "EditProfile");
                } else {
                    Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Error fetching user data: ", e);
                Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);
        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(UserProfileActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                userFirstNameTextView.setText(documentSnapshot.getString("firstName"));
                userLastNameTextView.setText(documentSnapshot.getString("lastName"));
                userNameTextView.setText(documentSnapshot.getString("username"));
                userEmailTextView.setText(documentSnapshot.getString("email"));
                userBioTextView.setText(documentSnapshot.getString("bio"));
                userAddressTextView.setText(documentSnapshot.getString("address"));

                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl)
                            .transform(new CircleTransform(strokeWidth, strokeColor))
                            .placeholder(R.drawable.userprofileicon)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.userprofileicon); // Default image
                }
            } else {
                Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userActivity(String userId) {
        Timestamp currentTime = Timestamp.now();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("lastActive", currentTime);

        SimpleDateFormat idSdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = idSdf.format(new Date());
        String documentId = userId + "_" + timestamp;

        db.collection("userActivity").document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "New activity recorded successfully with ID: " + documentId))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to record activity: " + e.getMessage()));
    }
}