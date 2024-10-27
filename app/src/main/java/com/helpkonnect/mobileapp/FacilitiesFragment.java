package com.helpkonnect.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FacilitiesFragment extends Fragment {

    private ProgressBar loaderNearbyFacilities;
    private TextView noFacilityTextView, searchResultTextView;
    private SearchView searchFacilities;
    private RecyclerView facilityRecyclerView;
    private List<FacilityAdapter.Facility> facilities = new ArrayList<>();
    private FacilityAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_facilities, container, false);

        loaderNearbyFacilities = rootView.findViewById(R.id.LoaderNearbyFacilities);
        noFacilityTextView = rootView.findViewById(R.id.noFacilityTextView);
        facilityRecyclerView = rootView.findViewById(R.id.FacilityRecyclerView);

        searchResultTextView = rootView.findViewById(R.id.SearchResultTextview);
        searchFacilities = rootView.findViewById(R.id.SearchFacilities);

        facilityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        loaderNearbyFacilities.setVisibility(View.VISIBLE);

        loadFacilitiesData();



        //For clicking the searchview
        searchFacilities.setOnClickListener( v -> {
            searchFacilities.setIconified(false);
            searchFacilities.requestFocus();
        });


        searchFacilities.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFacilities(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {

                    adapter.updateList(facilities);
                    searchResultTextView.setVisibility(View.GONE);
                    noFacilityTextView.setVisibility(View.GONE);
                    searchFacilities.clearFocus();
                    searchFacilities.setIconified(true);
                }
                return false;
            }
        });
        //When pressing the x button on the search view
        searchFacilities.setOnCloseListener(() -> {
            loadFacilitiesData();
            searchResultTextView.setVisibility(View.GONE);
            noFacilityTextView.setVisibility(View.GONE);
            searchFacilities.clearFocus();
            return false;
        });

        return rootView;
    }

    private void getFacilityData(){

    }

    // Method to data display for facilities
    private void loadFacilitiesData() {
        loaderNearbyFacilities.setVisibility(View.VISIBLE);
        facilities.clear();

        // Fetch data from Firestore
        db.collection("credentials")
            .whereEqualTo("role", "facility")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        FacilityAdapter.Facility facility = new FacilityAdapter.Facility(
                            document.getString("imageUrl"),
                            document.getString("facilityName"),
                            document.getString("facilityLocation"),
                            document.getString("userId"),
                            0,
                            ""
                        );
                        facilities.add(facility);
                    }

                    // Hide loader after data is displayed
                    loaderNearbyFacilities.setVisibility(View.GONE);

                    if (facilities.isEmpty()) {
                        noFacilityTextView.setVisibility(View.VISIBLE);
                    } else {
                        noFacilityTextView.setVisibility(View.GONE);
                        adapter = new FacilityAdapter(facilities, facility -> {
                            Intent intent = new Intent(requireActivity(), FacilityDetailsActivity.class);
                            intent.putExtra("imageUrl", facility.getImage());
                            intent.putExtra("name", facility.getTitle());
                            intent.putExtra("location", facility.getLocation());
                            intent.putExtra("rating", facility.getRating());
                            intent.putExtra("userId", facility.getUserId());

                            // Start the activity
                            startActivity(intent);
                        });
                        facilityRecyclerView.setAdapter(adapter);
                    }
                } else {
                    // Handle the error
                    loaderNearbyFacilities.setVisibility(View.GONE);
                    noFacilityTextView.setVisibility(View.VISIBLE);
                }
            });
    }

    //Filter results and display from fetched data
    private void filterFacilities(String query) {
        List<FacilityAdapter.Facility> filteredFacilities = new ArrayList<>();
        for (FacilityAdapter.Facility facility : facilities) {
            if (facility.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredFacilities.add(facility);
            }
        }

        if (filteredFacilities.isEmpty()) {
            searchResultTextView.setText("No facilities found for \"" + query + "\"");
            searchResultTextView.setVisibility(View.VISIBLE);
        } else {
            searchResultTextView.setText("Search results for \"" + query + "\"");
            searchResultTextView.setVisibility(View.VISIBLE);
        }
        adapter.updateList(filteredFacilities);
    }

}



