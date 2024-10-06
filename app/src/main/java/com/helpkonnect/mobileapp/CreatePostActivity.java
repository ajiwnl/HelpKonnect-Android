package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//for current date create or posted
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Arrays;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CreatePostActivity extends AppCompatActivity {

    private ImageView postingBackButton,postingAddImage;
    private TextView postingButton;
    private EditText postingEditText;
    //For Image Selector
    private Uri selectedImageUri;
    //For Firebase Authentication
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    //For Current Date and Time
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.ENGLISH);
    private final Date dateTimeToday = new Date();
    private final String formattedDateTime = sdf.format(dateTimeToday);

    private static final int PICK_IMAGE_REQUEST = 100; // Use the same request code

    private String uploadedImageUrl; // Variable to store the uploaded image URL

    private RequestQueue requestQueue;

    private String filterKey;
    private String filterHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        postingBackButton = findViewById(R.id.postingBackButton);
        postingButton = findViewById(R.id.postingButton);

        //Post Contents
        postingAddImage = findViewById(R.id.postingAddImage);
        postingEditText = findViewById(R.id.postingContentsEditText);

        postingBackButton.setOnClickListener(v -> {
            finish();
        });

        postingButton.setOnClickListener(v -> {
            savePost(); // Trigger post saving, including image upload if necessary
        });

        postingAddImage.setOnClickListener(v -> {
            openImagePicker();
        });

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Fetch API keys from Vercel
        fetchApiKeys();
    }

    private void filterText(String text, FilterCallback callback) {
        String url = "https://community-purgomalum.p.rapidapi.com/json?text=" + Uri.encode(text);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Check if the response contains asterisks indicating profanity
                        boolean containsProfanity = response.getString("result").contains("***");
                        callback.onResult(containsProfanity);
                    } catch (JSONException e) {
                        callback.onFailure(e);
                    }
                },
                error -> callback.onFailure(error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (filterKey != null && filterHost != null) {
                    headers.put("X-RapidAPI-Key", filterKey);
                    headers.put("X-RapidAPI-Host", filterHost);
                }
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    interface FilterCallback {
        void onResult(boolean containsProfanity);
        void onFailure(Exception e);
    }

    private void savePost() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String postContent = postingEditText.getText().toString().trim();

        filterText(postContent, new FilterCallback() {
            @Override
            public void onResult(boolean containsProfanity) {
                if (containsProfanity) {
                    postingEditText.setText("");
                    Toast.makeText(CreatePostActivity.this, "Post contains inappropriate content.", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedImageUri != null) {
                        try {
                            byte[] imageData = convertImageUriToByteArray(selectedImageUri);
                            uploadImageToFirebaseStorage(imageData, userId, postContent);
                        } catch (IOException e) {
                            Toast.makeText(CreatePostActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        createPostInFirestore(userId, postContent, null);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreatePostActivity.this, "Failed to filter text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebaseStorage(byte[] imageData, String userId, String postContent) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileReference.putBytes(imageData)
            .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString(); // Store the uploaded image URL
                createPostInFirestore(userId, postContent, uploadedImageUrl);
            }))
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
    }

    private void createPostInFirestore(String userId, String postContent, String imageUrl) {
        // Fetch user details from Firestore
        db.collection("credentials").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    String userProfile = documentSnapshot.getString("imageUrl");

                    // Create a map for the post data
                    HashMap<String, Object> post = new HashMap<>();
                    post.put("caption", postContent);
                    post.put("heart", 0);
                    post.put("time", new Date());
                    post.put("userId", userId);
                    post.put("userProfile", userProfile);
                    post.put("username", username);

                    // Only add image URL if it exists
                    if (imageUrl != null) {
                        post.put("imageUrls", Arrays.asList(imageUrl));
                    }

                    // Add the post to the Firestore collection
                    db.collection("community")
                        .add(post)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful post
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(this, "User details not found!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            postingAddImage.setImageURI(selectedImageUri);

            try {
                byte[] imageData = convertImageUriToByteArray(selectedImageUri);
                uploadImageToFirebaseStorage(imageData);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] convertImageUriToByteArray(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private void uploadImageToFirebaseStorage(byte[] imageData) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileReference.putBytes(imageData)
            .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString(); // Store the uploaded image URL
                Log.d("Success", "Image Prepared");
            }))
            .addOnFailureListener(e -> {
                Log.e("Error", "Image Not Prepared");
            });
    }

    private void storeImageUrlInFirestore(String downloadUrl) {
        HashMap<String, Object> post = new HashMap<>();
        post.put("imageUrl", downloadUrl);
        // Add other post details as needed

        db.collection("community").add(post)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void fetchApiKeys() {
        String url = "https://helpkonnect.vercel.app/api/filterKey";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        filterKey = jsonObject.getString("filterKey");
                        filterHost = jsonObject.getString("filterHost");
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse API keys", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch API keys", Toast.LENGTH_SHORT).show());

        requestQueue.add(stringRequest);
    }
}