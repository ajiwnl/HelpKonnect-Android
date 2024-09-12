package com.helpkonnect.testpush;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FacilitiesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_facilities, container, false);

        // List of facilities
        List<FacilityAdapter.Facility> facilities = new ArrayList<>();
        facilities.add(new FacilityAdapter.Facility(R.drawable.help_konnect_logo, "Facility A", "Sample Location 1", 4.5f, "100 - 200"));
        facilities.add(new FacilityAdapter.Facility(R.drawable.help_konnect_logo, "Facility B", "Sample Location 2", 3.8f, "300 - 400"));
        facilities.add(new FacilityAdapter.Facility(R.drawable.help_konnect_logo, "Facility C", "Sample Location 3", 4.2f, "250 - 350"));

        // Setup RecyclerView for Facilities
        RecyclerView nearbyRecyclerView = rootView.findViewById(R.id.nearbyRecyclerView);
        nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        FacilityAdapter adapter = new FacilityAdapter(facilities, facility ->
                Toast.makeText(requireContext(), "Clicked: " + facility.getTitle(), Toast.LENGTH_SHORT).show());

        nearbyRecyclerView.setAdapter(adapter);

        // Setup RecyclerView for Events
        RecyclerView eventsOffered = rootView.findViewById(R.id.eventsRecyclerView);
        eventsOffered.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Events Samples
        List<HomepageListAdapter.Item> items = new ArrayList<>();
        items.add(new HomepageListAdapter.Item("Event 1", R.drawable.edittextusericon));
        items.add(new HomepageListAdapter.Item("Event 2", R.drawable.edittextpasswordicon));
        items.add(new HomepageListAdapter.Item("Event 3", R.drawable.edittextemailicon));

        HomepageListAdapter eventsOfferedAdapter = new HomepageListAdapter(items, item ->
                Toast.makeText(requireContext(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show());

        eventsOffered.setAdapter(eventsOfferedAdapter);

        return rootView;
    }
}
