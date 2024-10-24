package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.view.MotionEvent;
import android.text.InputType;


public class SigninFragment extends Fragment {

    private ImageView formLayout;
    private TextView forgotPasswordTextView, signupTextView;
    private EditText emailEditText,passwordEditText;
    private Button loginButton;
    private View loaderView;
    private FirebaseAuth mAuth;

    private ListenerRegistration listenerRegistration;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);
        //Change the Text of loader to signing in

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //Initialize views
        emailEditText = rootView.findViewById(R.id.EmailEditText);
        passwordEditText = rootView.findViewById(R.id.PasswordEditText);
        loginButton = rootView.findViewById(R.id.SignupButton);
        forgotPasswordTextView = rootView.findViewById(R.id.ToForgotPasswordTextView);
        signupTextView = rootView.findViewById(R.id.ToSignInTextView);

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_off, 0);
                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_on, 0);
                    }

                    // Move cursor to the end of the text
                    passwordEditText.setSelection(passwordEditText.length());
                    return true;
                }
            }
            return false;
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> signInUser());

        // Handle forgot password click
        forgotPasswordTextView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new ForgotPasswordFragment());
        });

        // Handle signup click
        signupTextView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new RegisterFragment());
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
            showLoader(false,null);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            showLoader(false,null);
            return;
        }
        showLoader(true, "Signing You In");
        // Sign in with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if the user's email is verified
                            if (user.isEmailVerified()) {
                                String userId = user.getUid();
                                updateUserSession(userId, true);
                                userActivity(userId);
                                Intent intent = new Intent(getContext(), MainScreenActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                mAuth.signOut();
                                showLoader(false, null);
                                Toast.makeText(getContext(), "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Handle sign-in failures
                        showLoader(false, null);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(getContext(), "User does not exist, please try again.", Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            FirebaseAuthInvalidCredentialsException credentialException = (FirebaseAuthInvalidCredentialsException) exception;
                            String errorCode = credentialException.getErrorCode();
                            if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                                Toast.makeText(getContext(), "Wrong password, please try again.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                Toast.makeText(getContext(), "Invalid email format, please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Sign in failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove the listener when the fragment is stopped
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null; // Optional: clear the reference to avoid potential memory leaks
        }
    }


    private void showLoader(boolean show, String message) {
        if (loaderView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            loaderView = inflater.inflate(R.layout.loader_uam, null);
        }

        TextView loadingText = loaderView.findViewById(R.id.loadingText);
        if (message != null) {
            loadingText.setText(message);
        }

        if (show) {
            if (loaderView.getParent() == null) {
                getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
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
