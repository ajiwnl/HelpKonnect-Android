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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelfCareMaterialsActivity extends AppCompatActivity {

    private SearchView searchView;
    private ImageButton backButton;
    private RecyclerView itemSearchedRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView noResourcesMessage;
    private ResourceAdapter resourceAdapter;
    private List<Resource> resourceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_self_care_materials);

        itemSearchedRecyclerView = findViewById(R.id.ItemsSearched);
        itemSearchedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceAdapter = new ResourceAdapter(this, resourceList);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        itemSearchedRecyclerView.setAdapter(resourceAdapter);

        searchView = findViewById(R.id.searchMaterials);

        fetchApprovedResources();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.SelfCareBackButton);
        searchView = findViewById(R.id.searchMaterials);
        itemSearchedRecyclerView = findViewById(R.id.ItemsSearched);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        noResourcesMessage = findViewById(R.id.noResourcesMessage);


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

    // Fetch all approved resources from Firestore
    private void fetchApprovedResources() {
        loadingIndicator.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("resources")
                .whereEqualTo("approved", true)
                .get()
                .addOnCompleteListener(task -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        resourceList.clear(); // Clear existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Resource resource = document.toObject(Resource.class);
                            resourceList.add(resource);
                        }
                        resourceAdapter.notifyDataSetChanged(); // Notify adapter of data change

                        if (resourceList.isEmpty()) {
                            noResourcesMessage.setVisibility(View.VISIBLE);
                        } else {
                            noResourcesMessage.setVisibility(View.GONE);
                        }
                    } else {
                        noResourcesMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    // Filter resources based on query
    private void performSearch(String query) {
        List<Resource> filteredList = new ArrayList<>();
        for (Resource resource : resourceList) {
            if (resource.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(resource);
            }
        }

        // Update RecyclerView with filtered results
        resourceAdapter.updateList(filteredList);
    }
}