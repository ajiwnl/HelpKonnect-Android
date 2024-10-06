package com.helpkonnect.mobileapp;

import android.content.Context;
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

public class FacilitiesFragment extends Fragment {

    private ProgressBar loaderNearbyFacilities;
    private TextView noFacilityTextView, searchResultTextView;
    private SearchView searchFacilities;
    private RecyclerView facilityRecyclerView;
    private List<FacilityAdapter.Facility> facilities = new ArrayList<>();
    private FacilityAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_facilities, container, false);

        // Initialize facility views
        loaderNearbyFacilities = rootView.findViewById(R.id.LoaderNearbyFacilities);
        noFacilityTextView = rootView.findViewById(R.id.noFacilityTextView);
        facilityRecyclerView = rootView.findViewById(R.id.FacilityRecyclerView);

        // Initialize search views
        searchResultTextView = rootView.findViewById(R.id.SearchResultTextview);
        searchFacilities = rootView.findViewById(R.id.SearchFacilities);

        // Set up RecyclerView layout manager
        facilityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        // Show loader and load facilities data
        loaderNearbyFacilities.setVisibility(View.VISIBLE);

        loadFacilitiesData(); //Initial Load



        //For clicking the searchview
        searchFacilities.setOnClickListener( v -> {
            searchFacilities.setIconified(false);
            searchFacilities.requestFocus();
        });


        // Set up SearchView listener for text submission
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
        new Handler().postDelayed(() -> {
            // Sample list of facilities change to get from firebaswe
            facilities.clear();
            facilities.add(new FacilityAdapter.Facility(R.drawable.facilitybg, "Facility A", "Sample Location 1", 4.5f, "100 - 200"));
            facilities.add(new FacilityAdapter.Facility(R.drawable.facilitybg, "Facility B", "Sample Location 2", 3.8f, "300 - 400"));
            facilities.add(new FacilityAdapter.Facility(R.drawable.facilitybg, "Facility C", "Sample Location 3", 4.2f, "250 - 350"));

            //For getting data from firebase
            //getFacilityData();

            // Hide loader after data is displayed
            loaderNearbyFacilities.setVisibility(View.GONE);

            if (facilities.isEmpty()) {
                noFacilityTextView.setVisibility(View.VISIBLE);
            } else {
                noFacilityTextView.setVisibility(View.GONE);
                adapter = new FacilityAdapter(facilities, facility -> {
                    // Navigate to FacilityDetailsFragment with selected facility data
                    Bundle bundle = new Bundle();
                    bundle.putInt("logo", facility.getImage());
                    bundle.putString("name", facility.getTitle());
                    bundle.putString("location", facility.getLocation());
                    bundle.putFloat("rating", facility.getRating());

                    FacilityDetailsFragment detailsFragment = new FacilityDetailsFragment();
                    detailsFragment.setArguments(bundle);

                    FragmentMethods.displayFragment(requireActivity().getSupportFragmentManager(), R.id.fragmentContent, detailsFragment);
                });

                facilityRecyclerView.setAdapter(adapter);
            }
        }, 2000);
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


