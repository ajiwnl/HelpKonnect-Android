package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResourcesFragment extends Fragment {

    private FirebaseAuth mAuth;

    CardView musicPlayerContainer,selfcareContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_resources, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        musicPlayerContainer = rootView.findViewById(R.id.MusicPlayer);
        selfcareContainer = rootView.findViewById(R.id.SelfCare);

        musicPlayerContainer.setOnClickListener( v -> {
            Intent toMusicPlayer = new Intent(requireContext(), MusicPlayerActivity.class);
            startActivity(toMusicPlayer);
        });

        selfcareContainer.setOnClickListener( v -> {
            Intent toSelfCare = new Intent(requireContext(), SelfCareMaterialsActivity.class);
            startActivity(toSelfCare);
        });

        userActivity(userId);
        return rootView;
    }

    private void userActivity(String userId) {
        // Get the current time
        Timestamp currentTime = Timestamp.now();  // Use Firestore's Timestamp class

        // Prepare Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to hold the activity details
        Map<String, Object> data = new HashMap<>();
        data.put("lastActive", currentTime);  // Timestamp of the activity
        data.put("featureAccessed", "ResourcesFragment");  // The feature the user accessed
        data.put("userId", userId);

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
