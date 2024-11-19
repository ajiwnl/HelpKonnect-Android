package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UserSettingsFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private int strokeColor = Color.BLACK;
    private int strokeWidth = 1;

    private LinearLayout profileMenu;

    private TextView userFullName, userBio, userAddress;

    private ImageView userProfile;

    private ConstraintLayout profileLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_settings, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        currentUser = mAuth.getCurrentUser();


        // Initialize views
        userFullName = rootView.findViewById(R.id.fullnametextview);
        userBio = rootView.findViewById(R.id.biotitle);
        userAddress = rootView.findViewById(R.id.addresstitle);
        userProfile = rootView.findViewById(R.id.userProfileImg);
        profileLayout = rootView.findViewById(R.id.ProfileBox);
        profileMenu = rootView.findViewById(R.id.profileMenu);

        // Load user data
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }

        //Navigate to Profile Settings
        rootView.findViewById(R.id.profileMenu).setOnClickListener(v -> {
            Intent toProfileSettings = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(toProfileSettings);
        });

        // Handle logout button click
        rootView.findViewById(R.id.logoutMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });

        rootView.findViewById(R.id.ProfileBooking).setOnClickListener( v ->{
            Intent toBookingCollection = new Intent(getActivity(), BookingActivity.class);
            startActivity(toBookingCollection);
        });

        rootView.findViewById(R.id.PreferencesMenu).setOnClickListener( v ->{
            FragmentMethods.displayFragment(getActivity().getSupportFragmentManager(),R.id.fragment_container,new PreferenceFragment()) ;
        });

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                startActivity(intent);  // Navigate to UserProfileActivity

            }
        });

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                startActivity(intent);  // Navigate to UserProfileActivity

            }
        });

        return rootView;
    }


    // Method to handle sign out
    private void signOutUser() {
        LogoutDialogFragment dialog = new LogoutDialogFragment();
        dialog.setOnLogoutConfirmationListener(() -> {
            // This runs when the user confirms the logout
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();

                updateUserSession(userId, false); // Update session in Firestore
            }

            // Sign out from Firebase Auth
            mAuth.signOut();

           //Go back to Account MAnagement Activity with fragment of Sign in
            Intent intent = new Intent(requireContext(), UserAccountManagementActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        // Show the dialog
        dialog.show(requireFragmentManager(), "LogoutConfirmationDialog");
    }


    private void updateUserSession(String userId, boolean isActive) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("userSessions").document(userId);

        // Create a map to hold the session data
        Map<String, Object> sessionData = new HashMap<>();
        if (isActive) {
            sessionData.put("isActive", true);
            sessionData.put("sessionStart", Timestamp.now());
            sessionData.put("sessionEnd", null);// sessionEnd is not set on sign-in
        } else {
            sessionData.put("isActive", false);
            sessionData.put("sessionEnd", Timestamp.now());
        }

        // Update the document
        docRef.set(sessionData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Session data updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update session data: " + e.getMessage());
                });
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        // Add Firestore real-time listener
        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Retrieve and display user data
                String firstName = documentSnapshot.getString("firstName");
                String lastName = documentSnapshot.getString("lastName");
                String fullName = firstName + " " + lastName;
                String bio = documentSnapshot.getString("bio");
                String address = documentSnapshot.getString("address");
                String imageUrl = documentSnapshot.getString("imageUrl");

                // Update TextViews with real-time data
                userFullName.setText(fullName);
                userBio.setText(bio);
                userAddress.setText(address);

                // Load the profile image using Picasso
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().
                            load(imageUrl).
                            transform(new CircleTransform(strokeWidth, strokeColor)).
                            placeholder(R.drawable.userprofileicon)
                            .into(userProfile);
                } else {
                    userProfile.setImageResource(R.drawable.userprofileicon); // Default profile picture if no URL
                }
            } else {
            }
        });
    }
}