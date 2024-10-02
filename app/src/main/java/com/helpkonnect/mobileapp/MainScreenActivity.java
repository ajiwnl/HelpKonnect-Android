package com.helpkonnect.mobileapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

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
        // Enable edge-to-edge display (this is a new Android feature)
        enableEdgeToEdge();
        setContentView(R.layout.activity_main);

        // Initialize FragmentManager
        androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        profileNameTextView = findViewById(R.id.profile_name);
        profileImageView = findViewById(R.id.profile_image);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        if (currentUser != null) {
            loadUserData(userId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Setup DrawerLayout and NavigationView
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

        // Load the default fragment
        FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new HomepageFragment());

        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.homenav) {
                Toast.makeText(MainScreenActivity.this, "Selected Home", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new HomepageFragment());
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

            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

    }

    private void enableEdgeToEdge() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
            dispatcher.onBackPressed();
        }
    }

    private void loadUserData(String userId) {
        DocumentReference userDocRef = firestore.collection("credentials").document(userId);

        // Add Firestore real-time listener
        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(MainScreenActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String imageUrl = documentSnapshot.getString("imageUrl");

                // Update TextViews with real-time data
                profileNameTextView.setText(username);
                // Load the profile image using Picasso
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).transform(new CircleTransform(strokeWidth, strokeColor)).placeholder(R.drawable.userprofileicon) // Use a placeholder image
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.userprofileicon); // Default profile picture if no URL
                }
            } else {
                Toast.makeText(MainScreenActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
