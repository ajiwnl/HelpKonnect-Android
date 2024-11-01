package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageResourcesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ResourceAdapter resourceAdapter;
    private List<Resource> resourceList;
    private FirebaseFirestore firestore;
    private String facilityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_resources);

        facilityName = getIntent().getStringExtra("facilityName");

        resourceList = new ArrayList<>();

        recyclerView = findViewById(R.id.ResourceListView);
        resourceAdapter = new ResourceAdapter(this, resourceList);
        recyclerView.setAdapter(resourceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ImageView uploadResourceButton = findViewById(R.id.UploadResourceButton);
        uploadResourceButton.setOnClickListener(v -> showAddResourceDialog());

        firestore = FirebaseFirestore.getInstance();
        fetchResources(facilityName);
    }

    private void showAddResourceDialog() {

        AddResourceFragment addResourceFragment = new AddResourceFragment();
        Bundle args = new Bundle();
        args.putString("facilityName", facilityName);
        addResourceFragment.setArguments(args);

        addResourceFragment.show(getSupportFragmentManager(), "AddResourceFragment");
    }

    private void fetchResources(String facilityName) {
        firestore.collection("resources")
                .whereEqualTo("facilityName", facilityName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resourceList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Resource resource = document.toObject(Resource.class);
                        if (resource.getTime() != null) {
                            resourceList.add(resource);
                        }
                    }
                    Log.d("ManageResources", "Fetched " + resourceList.size() + " resources");
                    resourceAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", e.getMessage()));
    }
}