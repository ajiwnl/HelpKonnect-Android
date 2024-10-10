package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateJournalActivity extends AppCompatActivity {

    private ImageView backButton, journalImage;
    private ImageButton saveButton, shareButton, expandButton;
    private View loaderView;
    private EditText journalTitle, journalSubtitle, journalNotes;
    private TextView journalDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private static final String TAG = "Translation";
    private static String API_KEY;
    private OkHttpClient client = new OkHttpClient();  // OkHttpClient instance

    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);


    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_journal);

        loaderView = getLayoutInflater().inflate(R.layout.journal_loader, null);

        // Initialize Firebase Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set up UI components
        backButton = findViewById(R.id.journalBackButton);
        saveButton = findViewById(R.id.saveJournalButton);
        shareButton = findViewById(R.id.shareJournalButton);
        expandButton = findViewById(R.id.ExpandJournalActionButton);

        journalTitle = findViewById(R.id.journalEntryTitleEditText);
        journalSubtitle = findViewById(R.id.journalEntrySubtitleEditText);
        journalNotes = findViewById(R.id.journalEntryNotesEditText);
        journalDate = findViewById(R.id.journalEntryDateTextView);
        journalImage = findViewById(R.id.journalEntryImage);

        backButton.setOnClickListener( v -> {
            finish();
        });

        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleButtons();
            }
        });



        //Get the current timestamp
        com.google.firebase.Timestamp timestamp = com.google.firebase.Timestamp.now();

        fetchApiKey();

        // Format the timestamp into a readable date string
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(timestamp.toDate());

        // Set the formatted date to the journalDate TextView
        journalDate.setText(formattedDate);

        backButton.setOnClickListener(v -> finish());

        // Image picker for the journal image
        journalImage.setOnClickListener(v -> openImagePicker());

        // Save journal entry
        saveButton.setOnClickListener(v -> saveJournalEntry());

        shareButton.setOnClickListener(v -> {
            Toast.makeText(this, "Cannot Share Yet", Toast.LENGTH_SHORT).show();
        });

    }

    private void toggleButtons() {
        Animation animShow = AnimationUtils.loadAnimation(this, R.anim.fab_slide_up);
        Animation animHide = AnimationUtils.loadAnimation(this, R.anim.fab_slide_down);
        Animation animShake = AnimationUtils.loadAnimation(this, R.anim.fab_shake_up);

        expandButton.startAnimation(animShake);

        if (isExpanded) {
            saveButton.startAnimation(animHide);
            shareButton.startAnimation(animHide);
            saveButton.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);
        } else {
            saveButton.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);
            saveButton.startAnimation(animShow);
            shareButton.startAnimation(animShow);
        }
        isExpanded = !isExpanded;
    }

    private void openImagePicker() {
        // Open image picker to select image for journal
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            journalImage.setImageURI(selectedImageUri);
        }
    }

    // Method to fetch the API key
    private void fetchApiKey() {
        String url = "https://helpkonnect.vercel.app/api/androidRapidKey"; // URL to fetch the API key

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch API key: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonResponse = response.body().string(); // Read the response body first
                Log.d(TAG, "Raw JSON response: " + jsonResponse); // Log the raw response

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.has("androidRapidKey")) {
                            API_KEY = jsonObject.getString("androidRapidKey"); // Extract the API key
                            Log.d(TAG, "API Key fetched successfully: " + API_KEY);
                        } else {
                            Log.e(TAG, "API Key not found in the response");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing API key response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Failed to fetch API key: " + response.code());
                }
            }

        });
    }


    private void saveJournalEntry() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String title = journalTitle.getText().toString().trim();
        String subtitle = journalSubtitle.getText().toString().trim();
        String notes = journalNotes.getText().toString().trim();

        if (title.isEmpty() || subtitle.isEmpty() || notes.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            showLoader(false);
            return;
        }

        showLoader(true);
        userActivity(userId);
        StorageReference fileRef = storageReference.child("journal_images/" + System.currentTimeMillis() + ".jpg");

        // Upload the image and get the download URL
        fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Translate the notes before saving the journal entry
                translateText(notes, translatedText -> {
                    // Image uploaded and text translated, now save the journal entry to Firestore
                    Map<String, Object> journal = new HashMap<>();
                    journal.put("userId", userId);
                    journal.put("title", title);
                    journal.put("subtitle", subtitle);
                    journal.put("dateCreated", com.google.firebase.Timestamp.now());
                    journal.put("notes", notes);
                    journal.put("translated_notes", translatedText); // Save translated text
                    journal.put("imageUrl", uri.toString());

                    db.collection("journals")
                            .add(journal)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Journal saved successfully", Toast.LENGTH_SHORT).show();
                                showLoader(false);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error Saving Journal", Toast.LENGTH_SHORT).show();
                                // Clear all fields after error
                                journalTitle.setText("");
                                journalSubtitle.setText("");
                                journalNotes.setText("");
                                journalImage.setImageDrawable(null);
                                showLoader(true);
                            });
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
            // Clear all fields after error
            journalTitle.setText("");
            journalSubtitle.setText("");
            journalNotes.setText("");
            journalImage.setImageDrawable(null);
            showLoader(false);
        });
    }

    // Modified translateText method
    private void translateText(String text, TranslateCallback callback) {
        String url = "https://google-api31.p.rapidapi.com/gtranslate"; // Translation endpoint

        // Creating the JSON body for translation
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("text", text);
            requestBody.put("to", "en"); // Set target language to English
            requestBody.put("from_lang", "auto");
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON body: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));

        // Building the request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", "google-api31.p.rapidapi.com")
                .addHeader("Content-Type", "application/json")
                .build();

        // Logging request details
        Log.d(TAG, "Translation Request URL: " + url);

        // Making the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Translation request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Translation request failed with status code: " + response.code());
                    Log.e(TAG, "Response body: " + response.body().string());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Translation response body: " + responseBody);

                    // Modify this part to correctly parse the response
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String translatedText = jsonResponse.getString("translated_text"); // Corrected to directly get "translated_text"

                    Log.d(TAG, "Translated text: " + translatedText);
                    callback.onTranslate(translatedText); // Callback with translated text
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing translation response: " + e.getMessage());
                }
            }
        });
    }

    // Callback interface for translation
    public interface TranslateCallback {
        void onTranslate(String translatedText);
    }

    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            // Add loaderView to the activity's root view
            ((ViewGroup) findViewById(android.R.id.content)).addView(loaderView);
        } else {
            // Remove loaderView if it is already added
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }


    //For User Activity:
    private void userActivity(String userId) {
        // Get the current time
        Timestamp currentTime = Timestamp.now();  // Use Firestore's Timestamp class

        // Prepare Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to hold the lastActive field with the Timestamp
        Map<String, Object> data = new HashMap<>();
        data.put("lastActive", currentTime);  // Use Timestamp object directly

        // Create a custom document ID using the userId and timestamp
        SimpleDateFormat idSdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = idSdf.format(new Date());  // Use current date for document ID
        String documentId = userId + "_" + timestamp;

        // Add a new document with a custom ID (userId + timestamp) to the "userActivity" collection
        db.collection("userActivity")
                .document(documentId)  // Use the custom document ID
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // Successfully created a new activity document
                    Log.d("Firestore", "New activity recorded successfully with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Failed to record activity: " + e.getMessage());
                });
    }

}