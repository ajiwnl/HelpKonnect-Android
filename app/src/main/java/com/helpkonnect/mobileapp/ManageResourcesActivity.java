package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageResourcesActivity extends AppCompatActivity {

    private ImageView uploadResourceButton, uploadResourceBackButton;
    private RecyclerView resourceListView;
    private TextView resourceErrorTextView;
    private ProgressBar loadingIndicator;
    private List<ResourceModel> resourceList = new ArrayList<>();
    private ManageResourceAdapter resourceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_resources);

        // Initialize other UI components
        uploadResourceButton = findViewById(R.id.UploadResourceButton);
        uploadResourceBackButton = findViewById(R.id.UploadResourceBackButton);
        resourceListView = findViewById(R.id.ResourceListView);
        resourceErrorTextView = findViewById(R.id.ResourceErrorTextView);
        loadingIndicator = findViewById(R.id.LoadingIndicator);

        uploadResourceButton.setOnClickListener(v -> showAddResourceDialog());
        uploadResourceBackButton.setOnClickListener(v -> finish());

        resourceAdapter = new ManageResourceAdapter(resourceList);
        resourceListView.setLayoutManager(new LinearLayoutManager(this));
        resourceListView.setAdapter(resourceAdapter);

        populateResourceList();

    }

    private void showAddResourceDialog() {
        AddResourceFragment dialog = new AddResourceFragment();
        dialog.show(getSupportFragmentManager(), "AddResourceDialogFragment");
    }

    private void populateResourceList() {

        loadingIndicator.setVisibility(View.VISIBLE);
        resourceErrorTextView.setVisibility(View.GONE);

        resourceList.add(new ResourceModel("Resource 1", "Description for Resource 1", R.drawable.addjournalimageicon));

        new Handler().postDelayed(() -> {
            loadingIndicator.setVisibility(View.GONE);

            if (resourceList.isEmpty()) {
                resourceErrorTextView.setText("No resources available.");
                resourceErrorTextView.setVisibility(View.VISIBLE);
            } else {
                resourceErrorTextView.setVisibility(View.GONE);
                resourceAdapter.notifyDataSetChanged();
            }
        }, 1500);
    }
}

