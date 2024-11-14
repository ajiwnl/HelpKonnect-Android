package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class ChangePasswordActivity extends AppCompatActivity {

    private View loaderView;
    private FirebaseAuth currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        //Change the Text VAlue of Loader

        findViewById(R.id.changePassBackButton).setOnClickListener(v -> finish());

        loaderView = getLayoutInflater().inflate(R.layout.loader_uam, null);
        TextView updateCredLoaderText = loaderView.findViewById(R.id.loadingText);
        updateCredLoaderText.setText("Updating Your Password");//Warning HArdcodedtext

        currentUser = FirebaseAuth.getInstance();

        setupChangePasswordListener();
    }

    private void setupChangePasswordListener() {
        findViewById(R.id.button_update_password).setOnClickListener(view -> {
            String oldPassword = getOldPasswordInput();
            String newPassword = getNewPasswordInput();
            String confirmPassword = getConfirmPasswordInput();

            if (oldPassword == null || newPassword == null || confirmPassword == null) return;

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "New passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }

            reauthenticateAndChangePassword(oldPassword, newPassword);
        });
    }

    private String getOldPasswordInput() {
        String oldPassword = ((android.widget.EditText) findViewById(R.id.edit_text_old_password)).getText().toString().trim();
        if (oldPassword.isEmpty()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_old_password)).setError("Old password required");
            findViewById(R.id.edit_text_old_password).requestFocus();
            return null;
        }
        return oldPassword;
    }

    private String getNewPasswordInput() {
        String newPassword = ((android.widget.EditText) findViewById(R.id.edit_text_new_password)).getText().toString().trim();

        // Regular expression for the password policy
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";

        if (newPassword.isEmpty()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_new_password)).setError("New password required");
            findViewById(R.id.edit_text_new_password).requestFocus();
            return null;
        }

        if (!newPassword.matches(passwordPattern)) {
            ((android.widget.EditText) findViewById(R.id.edit_text_new_password)).setError("Password must contain at least 6 characters, including uppercase, lowercase, digit, and special character");
            findViewById(R.id.edit_text_new_password).requestFocus();
            return null;
        }

        return newPassword;
    }


    private String getConfirmPasswordInput() {
        String confirmPassword = ((android.widget.EditText) findViewById(R.id.edit_text_confirm_password)).getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_confirm_password)).setError("Confirm new password");
            findViewById(R.id.edit_text_confirm_password).requestFocus();
            return null;
        }
        return confirmPassword;
    }

    private void reauthenticateAndChangePassword(String oldPassword, String newPassword) {
        String email = currentUser.getCurrentUser().getEmail();
        if (email != null) {
            com.google.firebase.auth.AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
            showLoader(true);
            currentUser.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        showLoader(false);
                        if (task.isSuccessful()) {
                            updatePassword(newPassword);
                        } else {
                            handleReauthenticationFailure(task.getException());
                        }
                    });
        }
    }

    private void updatePassword(String newPassword) {
        showLoader(true);
        currentUser.getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    showLoader(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_LONG).show();
                        signOutAndRedirectToLogin();  // Sign out after successful password change
                    } else {
                        handlePasswordUpdateFailure(task.getException());
                    }
                });
    }

    private void signOutAndRedirectToLogin() {
        FirebaseAuth.getInstance().signOut();  // Sign out the user
        Toast.makeText(ChangePasswordActivity.this, "You have been signed out. Please log in with your new password.", Toast.LENGTH_LONG).show();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.FragmentContent, new SigninFragment());
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
        transaction.addToBackStack(null);
        transaction.commit();
        finish();
    }

    private void handleReauthenticationFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            ((android.widget.EditText) findViewById(R.id.edit_text_old_password)).setError("Invalid old password");
            findViewById(R.id.edit_text_old_password).requestFocus();
        } else {
            Toast.makeText(ChangePasswordActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handlePasswordUpdateFailure(Exception exception) {
        Toast.makeText(ChangePasswordActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        Log.d("ChangePasswordActivity", exception.getMessage());
    }

    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            addContentView(loaderView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }

}
