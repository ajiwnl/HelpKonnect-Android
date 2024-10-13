package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.firebase.auth.FirebaseAuth;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

public class ForgotPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button forgotPasswordButton;
    private TextView signupTextView;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements
        emailEditText = rootView.findViewById(R.id.PasswordEditText);
        forgotPasswordButton = rootView.findViewById(R.id.forgotpasswordButton);
        signupTextView = rootView.findViewById(R.id.ToSignInTextView);

        // Handle Forgot Password Button
        forgotPasswordButton.setOnClickListener(v -> sendPasswordResetEmail());

        // Navigate to Register Fragment
        signupTextView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new RegisterFragment());
        });

        return rootView;
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            return;
        }

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent. Please check your email.", Toast.LENGTH_SHORT).show();

                        // Custom animation for fragment transition
                        FragmentManager fragmentManager = requireFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                        transaction.replace(R.id.FragmentContent, new SigninFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Toast.makeText(getContext(), "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

