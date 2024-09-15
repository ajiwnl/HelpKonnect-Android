package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SigninFragment extends Fragment {

    private TextView forgotPasswordTextView;
    private TextView signupTextView;
    private Button login;
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_signin, container, false);

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
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
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
                                Intent intent = new Intent(getContext(), MainScreenActivity.class);
                                startActivity(intent);
                            } else {
                                // If email is not verified, sign out and notify the user
                                mAuth.signOut();  // Sign out the user
                                Toast.makeText(getContext(), "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Get the exception that occurred during sign-in
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            // This exception means that the email does not exist
                            Toast.makeText(getContext(), "User does not exist, please try again.", Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            FirebaseAuthInvalidCredentialsException credentialException = (FirebaseAuthInvalidCredentialsException) exception;
                            String errorCode = credentialException.getErrorCode();

                            // Check the specific error code to differentiate between invalid email and wrong password
                            if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                                // Email exists but the password is incorrect
                                Toast.makeText(getContext(), "Wrong password, please try again.", Toast.LENGTH_SHORT).show();
                            } else if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                // Invalid email format
                                Toast.makeText(getContext(), "Invalid email format.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle other credential errors
                                Toast.makeText(getContext(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle any other types of exceptions
                            Toast.makeText(getContext(), "Sign in failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}

