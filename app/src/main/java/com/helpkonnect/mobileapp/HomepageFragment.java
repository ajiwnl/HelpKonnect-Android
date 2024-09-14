package com.helpkonnect.mobileapp;

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

public class HomepageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_homepage, container, false);

        // Setting up RecyclerView1
        RecyclerView recyclerView1 = rootView.findViewById(R.id.toolsRecyclerView);
        recyclerView1.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setting up RecyclerView2
        RecyclerView recyclerView2 = rootView.findViewById(R.id.resourcesRecyclerView);
        recyclerView2.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // List of options (items)
        List<HomepageListAdapter.Item> items = new ArrayList<>();
        items.add(new HomepageListAdapter.Item("Item 1", R.drawable.edittextusericon));
        items.add(new HomepageListAdapter.Item("Item 2", R.drawable.edittextpasswordicon));
        items.add(new HomepageListAdapter.Item("Item 3", R.drawable.edittextemailicon));

        // Setting up the adapter
        HomepageListAdapter adapter = new HomepageListAdapter(items, item ->
                Toast.makeText(requireContext(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );

        recyclerView1.setAdapter(adapter);
        recyclerView2.setAdapter(adapter);

        return rootView;
    }
}
