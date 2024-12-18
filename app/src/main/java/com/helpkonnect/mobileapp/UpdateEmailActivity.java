package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UpdateEmailActivity extends AppCompatActivity {
    private View loaderView;
    private FirebaseAuth currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);


        findViewById(R.id.changeEmailBackButton).setOnClickListener(v -> finish());
        // Initialize loaderView by inflating the loader layout
        loaderView = getLayoutInflater().inflate(R.layout.loader_uam, null);
        TextView updateEmailLoaderText = loaderView.findViewById(R.id.loadingText);
        updateEmailLoaderText.setText("Updating Your Email");//Warning HArdcodedtext

        currentUser = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore

        setupInitialView();
        setupAuthenticationListener();
        setupUpdateEmailListener();
    }

    private void setupInitialView() {
        findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutUpdateEmail).setVisibility(View.GONE);
    }

    private void setupAuthenticationListener() {
        findViewById(R.id.button_authenticate).setOnClickListener(view -> {
            String password = getPasswordInput();
            if (password == null) return;

            if (currentUser.getCurrentUser() != null) {
                reauthenticateUser(password);
            }
        });
    }

    private void setupUpdateEmailListener() {
        findViewById(R.id.button_update).setOnClickListener(view -> {
            String newEmail = getEmailInput();
            if (newEmail == null) return;

            checkIfEmailExists(newEmail); // Check if email exists before proceeding
        });
    }

    private String getPasswordInput() {
        String password = ((android.widget.EditText) findViewById(R.id.edit_text_password)).getText().toString().trim();

        if (password.isEmpty()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_password)).setError("Password required");
            findViewById(R.id.edit_text_password).requestFocus();
            return null;
        }
        return password;
    }

    private String getEmailInput() {
        String newEmail = ((android.widget.EditText) findViewById(R.id.edit_text_email)).getText().toString().trim();

        if (newEmail.isEmpty()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_email)).setError("Email Required");
            findViewById(R.id.edit_text_email).requestFocus();
            return null;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            ((android.widget.EditText) findViewById(R.id.edit_text_email)).setError("Valid Email Required");
            findViewById(R.id.edit_text_email).requestFocus();
            return null;
        }
        return newEmail;
    }

    private void reauthenticateUser(String password) {
        String email = currentUser.getCurrentUser().getEmail();
        if (email != null) {
            com.google.firebase.auth.AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            setLoaderText("Verifying your account for a while...");
            showLoader(true);
            currentUser.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        showLoader(false);
                        if (task.isSuccessful()) {
                            findViewById(R.id.layoutPassword).setVisibility(View.GONE);
                            findViewById(R.id.layoutUpdateEmail).setVisibility(View.VISIBLE);
                        } else {
                            handleReauthenticationFailure(task.getException());
                        }
                    });
        }
    }

    private void handleReauthenticationFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            ((android.widget.EditText) findViewById(R.id.edit_text_password)).setError("Invalid Password");
            findViewById(R.id.edit_text_password).requestFocus();
        } else {
            Toast.makeText(UpdateEmailActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkIfEmailExists(String newEmail) {
        setLoaderText("Checking if email exists...");
        showLoader(true);

        // Query Firestore to check if email already exists in the 'credentials' collection
        firestore.collection("credentials")
                .whereEqualTo("email", newEmail)
                .get()
                .addOnCompleteListener(task -> {
                   showLoader(false);

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Email already exists
                            ((android.widget.EditText) findViewById(R.id.edit_text_email)).setError("Email already exists");
                            findViewById(R.id.edit_text_email).requestFocus();
                        } else {
                            // Email does not exist, proceed with email verification
                            sendVerificationEmail(newEmail);
                        }
                    } else {
                        Toast.makeText(UpdateEmailActivity.this, "Failed to check email existence", Toast.LENGTH_LONG).show();
                        Log.e("FirestoreError", task.getException().getMessage());
                    }
                });
    }

    private void sendVerificationEmail(String newEmail) {
       showLoader(true);
        currentUser.getCurrentUser().verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    showLoader(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateEmailActivity.this, "Verification email sent. Please verify and log back in.", Toast.LENGTH_LONG).show();
                        updateEmailInFirestore(newEmail);
                    } else {
                        handleEmailUpdateFailure(task.getException());
                    }
                });
    }

    // Update the email field in Firestore
    private void updateEmailInFirestore(String newEmail) {
        String userId = currentUser.getCurrentUser().getUid(); // Get the user ID
        firestore.collection("credentials").document(userId)
                .update("email", newEmail)  // Update the email field
                .addOnSuccessListener(aVoid -> {
                    handleEmailUpdateSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", e.getMessage());
                });
    }

    private void handleEmailUpdateSuccess() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }

    private void handleEmailUpdateFailure(Exception exception) {
        Toast.makeText(UpdateEmailActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        Log.d("UpdateEmailActivity", exception.getMessage());
    }

    private void navigateToLogin() {
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

    private void setLoaderText(String text) {
        if (loaderView != null) {
            TextView loadingText = loaderView.findViewById(R.id.loadingText);
            if (loadingText != null) {
                loadingText.setText(text);
            }
        }
    }

}
