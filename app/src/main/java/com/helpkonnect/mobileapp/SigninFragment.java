package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SigninFragment extends Fragment {

    private TextView forgotPasswordTextView;
    private TextView signupTextView;
    private View loaderView;
    private Button login;
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);
        loaderView = inflater.inflate(R.layout.signin_loader, container, false);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        login = rootView.findViewById(R.id.signupbutton);
        forgotPasswordTextView = rootView.findViewById(R.id.forgotpasswordtextView);
        signupTextView = rootView.findViewById(R.id.tosignintextview);

        // Handle login button click
        login.setOnClickListener(v -> signInUser());

        // Handle forgot password click
        forgotPasswordTextView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new ForgotPasswordFragment());
        });

        // Handle signup click
        signupTextView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new RegisterFragment());
        });

        return rootView;
    }

    // Sign in the user
    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            showLoader(false);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            showLoader(false);
            return;
        }

        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the user's email is verified
                            if (user.isEmailVerified()) {
                                // If email is verified, navigate to Main Screen

                                // Get the user's unique ID (userId)
                                String userId = mAuth.getCurrentUser().getUid();

                                updateUserSession(userId, true);

                                // Update the last active timestamp for this user
                                userActivity(userId);
                                showLoader(true);
                                Intent intent = new Intent(getContext(), MainScreenActivity.class);
                                startActivity(intent);
                            } else {
                                // If email is not verified, sign out and notify the user
                                mAuth.signOut();  // Sign out the user
                                showLoader(false);
                                Toast.makeText(getContext(), "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else {
                        // Get the exception that occurred during sign-in
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            // This exception means the email does not exist in Firebase
                            Toast.makeText(getContext(), "User does not exist, please try again.", Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            // Handle credential errors
                            FirebaseAuthInvalidCredentialsException credentialException = (FirebaseAuthInvalidCredentialsException) exception;
                            String errorCode = credentialException.getErrorCode();

                            // Check the specific error code to handle wrong password first
                            if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                                // Email exists but the password is incorrect
                                Toast.makeText(getContext(), "Wrong password, please try again.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                // Invalid email format entered
                                Toast.makeText(getContext(), "Invalid email format, please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle any other credential errors
                                Toast.makeText(getContext(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle any other types of exceptions
                            Toast.makeText(getContext(), "Sign in failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }


    //For User Activity:
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


    //For User Session
    private void updateUserSession(String userId, boolean isActive) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("userSessions").document(userId);

        // Create a map to hold the session data
        Map<String, Object> sessionData = new HashMap<>();
        if (isActive) {
            sessionData.put("isActive", true);
            sessionData.put("sessionStart", Timestamp.now());
            sessionData.put("sessionEnd", null); // sessionEnd is not set on sign-in
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



}

