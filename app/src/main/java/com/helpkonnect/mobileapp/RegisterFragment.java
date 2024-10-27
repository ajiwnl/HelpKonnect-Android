package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.view.MotionEvent;
import android.text.InputType;

public class RegisterFragment extends Fragment {

    private Button signup;
    private TextView login;
    private View loaderView;

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, usernameEditText;
    private RadioGroup radioGroup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String createUser = "https://helpkonnect.vercel.app/api/createUser";

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        //Change Loader TExt for creating Account
        loaderView = inflater.inflate(R.layout.loader_uam, container, false);
        TextView registerLoaderText = loaderView.findViewById(R.id.loadingText);
        registerLoaderText.setText("Creating New Account");//Warning HArdcodedtext


        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find UI elements
        signup = rootView.findViewById(R.id.SignupButton);
        login = rootView.findViewById(R.id.ToSignInTextView);
        emailEditText = rootView.findViewById(R.id.EmailEditText);
        passwordEditText = rootView.findViewById(R.id.PasswordEditText);
        confirmPasswordEditText = rootView.findViewById(R.id.confirmpasswordEditText);
        usernameEditText = rootView.findViewById(R.id.usernameEditText);
        radioGroup = rootView.findViewById(R.id.radioGroup);

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_off, 0); // Change the eye icon to "eye off"
                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_on, 0); // Change the eye icon back to "eye on"
                    }

                    // Move cursor to the end of the text
                    passwordEditText.setSelection(passwordEditText.length());
                    return true;
                }
            }
            return false;
        });

        confirmPasswordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_off, 0); // Change the eye icon to "eye off"
                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edittextpasswordicon, 0, R.drawable.ic_eye_on, 0); // Change the eye icon back to "eye on"
                    }

                    // Move cursor to the end of the text
                    passwordEditText.setSelection(passwordEditText.length());
                    return true;
                }
            }
            return false;
        });

        // Handle Signup Button
        signup.setOnClickListener(v -> registerUser());

        // Navigate to login fragment
        login.setOnClickListener(v -> {
            FragmentMethods.displayFragment(requireFragmentManager(), R.id.FragmentContent, new SigninFragment());
        });

        return rootView;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        int selectedRoleId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRoleButton = radioGroup.findViewById(selectedRoleId);
        String role = selectedRoleButton.getText().toString();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            showLoader(false);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            showLoader(false);
            return;
        }

        if (!isValidPassword(password)) {
            passwordEditText.setError("Password must be at least 6 characters long, contain upper and lower case letters, a number, and a special character.");
            showLoader(false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            showLoader(false);
            return;
        }

        showLoader(true);

        // Check if the username or email already exists
        checkForDuplicateUsernameAndEmail(username, email, () -> {
            // Register user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Get the registered user's ID
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Send email verification
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Store additional details in Firestore
                                                String userId = user.getUid();
                                                storeUserDetails(username, userId, email, role);

                                                // Notify user to check their email
                                                Toast.makeText(getContext(), "Registration successful. Please verify your email.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "Failed to send verification email: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                            showLoader(false);
                                        });
                            }
                        } else {
                            Toast.makeText(getContext(), "Registration failed please see the details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            showLoader(false);
                        }
                    });
        });
    }


    private void storeUserDetails(String username, String userId, String email, String role) {
        long currentTimeMillis = System.currentTimeMillis();
        String formattedDate = getFormattedDate(currentTimeMillis);
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("userId", userId);
        user.put("email", email);
        user.put("role", role);
        user.put("dateCreated", formattedDate);
        user.put("firstName", "");
        user.put("lastName", "");
        user.put("bio", "No information given");
        user.put("associated", "");
        user.put("imageUrl", "https://csassets.nintendo.com/noaext/image/private/f_auto/q_auto/t_KA_default/icon-menu-user-737373?_a=DATC1RAAZAA0");

        // Save the user in Firestore
        db.collection("credentials")
                .document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Make a Volley request to register the user with Stream Chat API
                        registerUserWithStreamChat(userId, username);
                        
                        // Navigate to SigninFragment and clear the back stack
                        FragmentTransaction transaction = requireFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                        );
                        transaction.replace(R.id.FragmentContent, new SigninFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Toast.makeText(getContext(), "Registration failed please see the details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUserWithStreamChat(String userId, String username) {
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("id", userId);
            userObject.put("name", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, createUser, userObject,
                response -> {
                    // Handle the response
                    Toast.makeText(getContext(), "User registered with Stream Chat successfully.", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Handle error
                    Toast.makeText(getContext(), "Failed to register user with Stream Chat: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    // Helper function to format the date
    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void checkForDuplicateUsernameAndEmail(String username, String email, Runnable onSuccess) {
        db.collection("credentials")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        usernameEditText.setError("Username is already taken.");
                        showLoader(false);
                    } else {
                        // Check for duplicate email
                        db.collection("credentials")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                                        emailEditText.setError("Email is already in use.");
                                        showLoader(false);
                                    } else {
                                        // No duplicates found, proceed with registration
                                        onSuccess.run();
                                    }
                                });
                    }
                });
    }

    // Password validation: at least 6 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special character
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";
        return password.matches(passwordPattern);
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
}
