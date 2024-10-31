package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddResourceFragment extends DialogFragment {

    private static final int FILE_PICKER_REQUEST_CODE = 1;
    private ImageView uploadImageView;
    private EditText resourceNameEditText, resourceDescEditText;
    private Button cancelButton, submitButton;

    private Uri fileUri;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private String facilityName;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_resource, container, false);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Bundle args = getArguments();
        facilityName = args != null ? args.getString("facilityName") : null;

        uploadImageView = rootView.findViewById(R.id.UploadImageView);
        resourceNameEditText = rootView.findViewById(R.id.ResourceNameEditText);
        resourceDescEditText = rootView.findViewById(R.id.ResourceDescEditText);
        cancelButton = rootView.findViewById(R.id.Cancel);
        submitButton = rootView.findViewById(R.id.Submit);

        uploadImageView.setOnClickListener(v -> openFileChooser());
        cancelButton.setOnClickListener(v -> dismiss());
        submitButton.setOnClickListener(v -> handleSubmit());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            resourceNameEditText.setText(fileName);
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    private void handleSubmit() {
        String resourceName = resourceNameEditText.getText().toString();
        String resourceDesc = resourceDescEditText.getText().toString();

        if (resourceName.isEmpty()) {
            resourceNameEditText.setError("Resource name is required");
            return;
        }

        if (fileUri != null) {
            uploadFile(resourceName, resourceDesc);
        } else {
            Toast.makeText(getContext(), "Please select a file to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(String resourceName, String resourceDesc) {
        StorageReference fileRef = storageReference.child("resources/" + System.currentTimeMillis() + "_" + resourceName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileURL = uri.toString();
                    saveResourceDetails(resourceName, resourceDesc, fileURL);
                }))
                .addOnFailureListener(e -> {
                    Log.e("AddResourceFragment", "File upload failed", e);
                    Toast.makeText(getContext(), "File upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveResourceDetails(String name, String description, String fileURL) {
        Map<String, Object> resource = new HashMap<>();
        resource.put("approved", false);
        resource.put("type", "E-book (pdf)");
        resource.put("name", name);
        resource.put("description", description);
        resource.put("fileURL", fileURL);
        resource.put("facilityName", facilityName);
        resource.put("time", Timestamp.now());

        firestore.collection("resources")
                .add(resource)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Resource added successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddResourceFragment", "Error adding resource", e);
                    Toast.makeText(getContext(), "Failed to add resource", Toast.LENGTH_SHORT).show();
                });
    }
}


