package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Objects;

public class ManageResourcesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_resources);

        // Find UploadResourceButton by id and set up click listener
        ImageView uploadResourceButton = findViewById(R.id.UploadResourceButton);
        uploadResourceButton.setOnClickListener(v -> showAddResourceDialog());
    }

    // Method to display the dialog fragment
    private void showAddResourceDialog() {
        AddResourceFragment dialog = new AddResourceFragment();
        dialog.show(getSupportFragmentManager(), "AddResourceDialogFragment");
    }
}
