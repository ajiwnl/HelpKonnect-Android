package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileDialogFragment extends DialogFragment {

    private Button saveButton, cancelButton;
    private EditText userFirstNameEditText, userLastNameEditText, usernameEditText, userBioEditText, userAddressEditText;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile_overlay, container, false);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize EditTexts
        userFirstNameEditText = view.findViewById(R.id.userfirstname);
        userLastNameEditText = view.findViewById(R.id.userlastname);
        usernameEditText = view.findViewById(R.id.username);
        userBioEditText = view.findViewById(R.id.userbio);
        userAddressEditText = view.findViewById(R.id.useraddress);

        // Get user data from bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            userFirstNameEditText.setText(bundle.getString("firstName"));
            userLastNameEditText.setText(bundle.getString("lastName"));
            usernameEditText.setText(bundle.getString("username"));
            userBioEditText.setText(bundle.getString("bio"));
            userAddressEditText.setText(bundle.getString("address"));
        }

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            saveUserProfile();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    // Method to save profile changes to Firestore
    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();

        // Update user data in Firestore
        firestore.collection("credentials").document(userId)
                .update(
                        "firstName", userFirstNameEditText.getText().toString(),
                        "lastName", userLastNameEditText.getText().toString(),
                        "username", usernameEditText.getText().toString(),
                        "bio", userBioEditText.getText().toString(),
                        "address", userAddressEditText.getText().toString()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

