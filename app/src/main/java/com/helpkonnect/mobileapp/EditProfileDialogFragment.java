
package com.helpkonnect.mobileapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditProfileDialogFragment extends DialogFragment {

    private Button saveButton, cancelButton, getAddBtn;
    private ImageView userProfilePhoto;
    private EditText userFirstNameEditText, userLastNameEditText, usernameEditText, userBioEditText, userAddressEditText;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 102;
    private int strokeColor = Color.BLACK;
    private int strokeWidth = 1;

    private View loaderView;

    public EditProfileDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile_overlay, container, false);
        loaderView = inflater.inflate(R.layout.update_loader, container, false);
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize edit texts, buttons, and others
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        userFirstNameEditText = view.findViewById(R.id.userfirstname);
        userLastNameEditText = view.findViewById(R.id.userlastname);
        usernameEditText = view.findViewById(R.id.username);
        userBioEditText = view.findViewById(R.id.userbio);
        userAddressEditText = view.findViewById(R.id.useraddress);
        getAddBtn = view.findViewById(R.id.addressButton);
        userProfilePhoto = view.findViewById(R.id.userProfilePic);

        userProfilePhoto.setOnClickListener(v -> openGallery());

        //Get address
        getAddBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                fetchCurrentLocation();
            }
        });

        // Get user data from bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            userFirstNameEditText.setText(bundle.getString("firstName"));
            userLastNameEditText.setText(bundle.getString("lastName"));
            usernameEditText.setText(bundle.getString("username"));
            userBioEditText.setText(bundle.getString("bio"));
            userAddressEditText.setText(bundle.getString("address"));

            String imageUrl = bundle.getString("imageUrl");

            Picasso.get()
                    .load(imageUrl)
                    .transform(new CircleTransform(strokeWidth, strokeColor))
                    .placeholder(R.drawable.userprofileicon)
                    .error(R.drawable.userprofileicon)
                    .into(userProfilePhoto);

        }

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            saveUserProfile();
        });

        cancelButton.setOnClickListener(v -> dismiss());



        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            userProfilePhoto.setImageURI(selectedImageUri); // Set the selected image in the ImageView

            // Now upload the image to Firebase Storage
            uploadImageToFirebase(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation(); // Location permission granted
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(); // Storage permission granted, now you can open the gallery
            } else {
                Toast.makeText(getContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            try {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (!addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        String addressLine = address.getAddressLine(0);
                                        userAddressEditText.setText(addressLine); // Update EditText with address
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), "Failed to get address", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (SecurityException e) {
                // Handle the SecurityException if it occurs
                Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateImageUrlInFirestore(downloadUrl); // Update Firestore with the image URL
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void updateImageUrlInFirestore(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        firestore.collection("credentials").document(userId)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Optionally log the success
                    Log.d("EditProfile", "Profile picture updated successfully");
                })
                .addOnFailureListener(e -> {
                    // Optionally log the failure
                    Log.d("EditProfile", "Failed to update profile picture: " + e.getMessage());
                });
    }


    // Method to save profile changes to Firestore
    private void saveUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();

        showLoader(true);
        // Update user data in Firestore
        firestore.collection("credentials").document(userId)
                .update(
                        "firstName", userFirstNameEditText.getText().toString(),
                        "lastName", userLastNameEditText.getText().toString(),
                        "username", usernameEditText.getText().toString(),
                        "bio", userBioEditText.getText().toString(),
                        "address", userAddressEditText.getText().toString()
                )
                .addOnSuccessListener(aVoid -> {
                    showLoader(false);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    showLoader(false);
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }
}