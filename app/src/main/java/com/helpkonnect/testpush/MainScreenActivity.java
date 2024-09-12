package com.helpkonnect.testpush;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainScreenActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle drawerToggle;

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
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new CommunicationFragment());
            } else if (id == R.id.setnav) {
                Toast.makeText(MainScreenActivity.this, "Selected Settings", Toast.LENGTH_SHORT).show();
                FragmentMethods.displayFragment(fragmentManager, R.id.fragmentContent, new UserSettingsFragment());
            }

            // Close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

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

    private void enableEdgeToEdge() {
        // You will need to implement this method for enabling edge-to-edge.
    }
}
