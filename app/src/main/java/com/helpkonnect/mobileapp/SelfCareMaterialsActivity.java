package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Arrays;
import java.util.List;

public class SelfCareMaterialsActivity extends AppCompatActivity {

    private SearchView searchView;
    private ImageButton backButton;
    private RecyclerView suggestionRecyclerView,itemSearchedRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView noResourcesMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_self_care_materials);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.SelfCareBackButton);
        searchView = findViewById(R.id.searchMaterials);
        suggestionRecyclerView = findViewById(R.id.SearchQuerySuggestions);
        itemSearchedRecyclerView = findViewById(R.id.ItemsSearched);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        noResourcesMessage = findViewById(R.id.noResourcesMessage);

        //Add Suggestion Searches
        List<String> suggestions = Arrays.asList("Books", "Online Podcast", "Self Care Manuals");

        ResourcesSearchSuggestionAdapter adapter = new ResourcesSearchSuggestionAdapter(this, suggestions);
        suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setAdapter(adapter);

        searchView.setOnClickListener( v -> {
            searchView.setIconified(false);
            searchView.requestFocus();
        });

        backButton.setOnClickListener( v -> {
            finish();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    public void autoFillSuggested(String query) {
        searchView.setQuery(query, true);
        searchView.setIconified(false);
        searchView.requestFocus();
    }

    public void performSearch(String query) {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        noResourcesMessage.setVisibility(View.GONE);

        // Simulate network or data fetching (replace with actual search logic)
        new android.os.Handler().postDelayed(() -> {
            // Hide loading indicator
            loadingIndicator.setVisibility(View.GONE);

            // For demonstration, assume no results are found
            boolean hasResults = false; // Replace with actual check for results

            if (hasResults) {
                // Update RecyclerView with results
                // itemSearchedRecyclerView.setAdapter(adapter);
            } else {
                // Show no resources message
                noResourcesMessage.setVisibility(View.VISIBLE);
            }
        }, 2000); // Simulated delay for loading
    }


}