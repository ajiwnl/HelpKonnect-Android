package com.helpkonnect.mobileapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

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

        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userName = currentUser.getDisplayName();
            loadUserData(userId);
            fetchTokenAndConnectUser(userId, userName);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new HomepageFragment());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.homenav) {
                Toast.makeText(MainScreenActivity.this, "Selected Home", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new HomepageFragment());
            } else if (id == R.id.messagenav) {
                Toast.makeText(MainScreenActivity.this, "Selected Messages", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new MessageFragment());
            } else if (id == R.id.chatbotnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Chatbot", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new ChatbotFragment());
            } else if (id == R.id.facnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Facilities", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new FacilitiesFragment());
            } else if (id == R.id.journalnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Journal", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new JournalFragment());
            } else if (id == R.id.locnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Location", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new LocationFragment());
            } else if (id == R.id.tracknav) {
                Toast.makeText(MainScreenActivity.this, "Selected Tracker", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new TrackerFragment());
            } else if (id == R.id.resnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Resources", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new ResourcesFragment());
            } else if (id == R.id.comnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Community", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new CommunityFragment());
            } else if (id == R.id.setnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Settings", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new UserSettingsFragment());
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void enableEdgeToEdge() {
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(MainScreenActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String imageUrl = documentSnapshot.getString("imageUrl");

                profileNameTextView.setText(username);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).transform(new CircleTransform(strokeWidth, strokeColor)).placeholder(R.drawable.userprofileicon)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.userprofileicon);
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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            connectUserToStreamChat(userId, token, userName);
                        } catch (JSONException e) {
                            Log.e("MainScreenActivity", "Failed to parse token from response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MainScreenActivity", "Failed to fetch token", error);
                    }
                }
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
                Log.d("MainScreenActivity", "User connected to Stream Chat successfully");
            } else {
                Log.e("MainScreenActivity", "Failed to connect user to Stream Chat");
            }
        });
    }
}