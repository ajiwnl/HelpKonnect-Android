package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.User;

public class MainScreenActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle drawerToggle;
    private TextView profileNameTextView;
    private ImageView profileImageView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private int strokeColor = Color.BLACK;
    private int strokeWidth = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profileNameTextView = findViewById(R.id.profile_name);
        profileImageView = findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        navView = findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userName = currentUser.getDisplayName();
            loadUserData(userId, menu);
            fetchTokenAndConnectUser(userId, userName);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new HomepageFragment());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.homenav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new HomepageFragment());
            } else if (id == R.id.messagenav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new MessageFragment());
            } else if (id == R.id.chatbotnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new ChatbotFragment());
            } else if (id == R.id.facnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new FacilitiesFragment());
            } else if (id == R.id.journalnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new JournalFragment());
            } else if (id == R.id.locnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new LocationFragment());
            } else if (id == R.id.tracknav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new TrackerFragment());
            } else if (id == R.id.resnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new ResourcesFragment());
            } else if (id == R.id.comnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new CommunityFragment());
            } else if (id == R.id.setnav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new UserSettingsFragment());
            } else if (id == R.id.assonav) {
                FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new AssociateFragment());
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            signOutUser();
        }
    }

    private void signOutUser() {
        LogoutDialogFragment dialog = new LogoutDialogFragment();
        dialog.setOnLogoutConfirmationListener(() -> {
            if (currentUser != null) {
                String userId = currentUser.getUid();
                updateUserSession(userId, false);
            }
            mAuth.signOut();
            Intent intent = new Intent(MainScreenActivity.this, UserAccountManagementActivity.class);
            startActivity(intent);
            finish();
        });
        dialog.show(getSupportFragmentManager(), "LogoutConfirmationDialog");
    }

    private void updateUserSession(String userId, boolean isActive) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("userSessions").document(userId);

        Map<String, Object> sessionData = new HashMap<>();
        if (isActive) {
            sessionData.put("isActive", true);
            sessionData.put("sessionStart", Timestamp.now());
            sessionData.put("sessionEnd", null);
        } else {
            sessionData.put("isActive", false);
            sessionData.put("sessionEnd", Timestamp.now());
        }

        docRef.set(sessionData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Session data updated successfully."))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update session data: " + e.getMessage()));
    }

    private void loadUserData(String userId, Menu menu) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(MainScreenActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                String username = documentSnapshot.getString("username");
                String imageUrl = documentSnapshot.getString("imageUrl");

                profileNameTextView.setText("Hello, " + username);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).transform(new CircleTransform(strokeWidth, strokeColor)).placeholder(R.drawable.userprofileicon)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.userprofileicon);
                }

                if ("Professional".equals(role)) {
                    menu.findItem(R.id.homenav).setVisible(false);
                    menu.findItem(R.id.journalnav).setVisible(false);
                    menu.findItem(R.id.tracknav).setVisible(false);
                    menu.findItem(R.id.chatbotnav).setVisible(false);
                    menu.findItem(R.id.resnav).setVisible(false);
                } else {
                    menu.findItem(R.id.assonav).setVisible(false);
                }
            } else {
                Toast.makeText(MainScreenActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTokenAndConnectUser(String userId, String userName) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://helpkonnect.vercel.app/api/generateToken";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", userId);
        } catch (JSONException e) {
            Log.e("MainScreenActivity", "Failed to create JSON request body", e);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        String token = response.getString("token");
                        connectUserToStreamChat(userId, token, userName);
                    } catch (JSONException e) {
                        Log.e("MainScreenActivity", "Failed to parse token from response", e);
                    }
                },
                error -> Log.e("MainScreenActivity", "Failed to fetch token", error)
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void connectUserToStreamChat(String userId, String token, String userName) {
        if (userName == null) {
            userName = "Default Name";
        }

        User user = UserKt.connectUser(userId, userName);

        ChatClient chatClient = ChatClient.instance();
        chatClient.connectUser(user, token).enqueue(result -> {
            if (result.isSuccess()) {
                Log.d("MainScreenActivity", "Connected to Stream Chat successfully");
            } else {
                Log.e("MainScreenActivity", "Failed to connect to Stream Chat");
            }
        });
    }
}