package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateJournalActivity extends AppCompatActivity {

    private ImageView backButton, saveButton, shareButton, journalImage;
    private View loaderView;
    private EditText journalTitle, journalSubtitle, journalNotes;
    private TextView journalDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private StorageReference storageReference;

    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

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

        journalTitle = findViewById(R.id.journalEntryTitleEditText);
        journalSubtitle = findViewById(R.id.journalEntrySubtitleEditText);
        journalNotes = findViewById(R.id.journalEntryNotesEditText);
        journalDate = findViewById(R.id.journalEntryDateTextView);
        journalImage = findViewById(R.id.journalEntryImage);

        journalDate.setText(DateTodayString);

        backButton.setOnClickListener(v -> finish());

        // Image picker for the journal image
        journalImage.setOnClickListener(v -> openImagePicker());

        // Save journal entry
        saveButton.setOnClickListener(v -> saveJournalEntry());

        shareButton.setOnClickListener(v -> {
            Toast.makeText(this, "Cannot Share Yet", Toast.LENGTH_SHORT).show();
        });
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

        StorageReference fileRef = storageReference.child("journal_images/" + System.currentTimeMillis() + ".jpg");
        fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Image uploaded, now save the journal entry to Firestore
                Map<String, Object> journal = new HashMap<>();
                journal.put("userId", userId);
                journal.put("title", title);
                journal.put("subtitle", subtitle);
                journal.put("dateCreated", DateTodayString);
                journal.put("notes", notes);
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

}
