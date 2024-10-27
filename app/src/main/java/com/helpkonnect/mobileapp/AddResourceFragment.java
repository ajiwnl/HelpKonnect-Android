package com.helpkonnect.mobileapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Objects;

public class AddResourceFragment extends DialogFragment {

    private ImageView uploadImageView;
    private EditText resourceNameEditText, resourceDescEditText;
    private Button cancelButton, submitButton;

    private static final int FILE_PICKER_REQUEST_CODE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_resource, container, false);

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
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf, audio/mpeg");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            String fileName = getFileName(fileUri);
            resourceNameEditText.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = Objects.requireNonNull(uri.getPath());
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void handleSubmit() {
        String resourceName = resourceNameEditText.getText().toString();
        String resourceDesc = resourceDescEditText.getText().toString();

        if (resourceName.isEmpty()) {
            resourceNameEditText.setError("Resource name is required");
        } else {
            dismiss();
        }
    }
}

