package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UpdateEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private EditText passwordInput, newEmailInput;
    private Button updateEmailButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        // Initialize Firebase and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        passwordInput = findViewById(R.id.passwordInput);
        newEmailInput = findViewById(R.id.newEmailInput);
        updateEmailButton = findViewById(R.id.updateEmailButton);

        // Initially disable new email input
        newEmailInput.setEnabled(false);

        // Set button listener to verify password
        updateEmailButton.setOnClickListener(v -> verifyPassword());
    }

    private void verifyPassword() {
        String password = passwordInput.getText().toString().trim();
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your current password.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            // Reauthenticate the user with the provided credentials
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Enable new email input if password is verified
                    newEmailInput.setEnabled(true);

                    // Set listener to update email when the new email is entered
                    updateEmailButton.setOnClickListener(v -> {
                        String newEmail = newEmailInput.getText().toString().trim();
                        if (!newEmail.isEmpty()) {
                            updateEmail(newEmail);
                        } else {
                            Toast.makeText(this, "Please enter a new email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Password verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateEmail(String newEmail) {
        // Check if the new email already exists in Firestore
        firestore.collection("credentials")
                .whereEqualTo("email", newEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Email is already in use. Please choose another.", Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Update the user's email in Firebase Auth
                                user.updateEmail(newEmail).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        sendVerificationEmail();
                                        saveNewEmailToFirestore(newEmail);
                                        Toast.makeText(this, "Email updated successfully!", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity after update
                                    } else {
                                        Toast.makeText(this, "Failed to update email. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveNewEmailToFirestore(String newEmail) {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        userDocRef.update("email", newEmail)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Email updated in Firestore."))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update email: " + e.getMessage()));
    }
}
