package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class JournalDetailActivity extends AppCompatActivity {

    private TextView titleTextView, subtitleTextView, dateTextView, notesTextView;
    private ImageView journalImageView, backButton;
    private ImageButton removeJournalButton, shareJournalDetailButton,expandJournalButton;

    private boolean isExpanded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_detail);

        // Initialize views
        titleTextView = findViewById(R.id.detailJournalTitle);
        subtitleTextView = findViewById(R.id.detailJournalSubtitle);
        dateTextView = findViewById(R.id.detailJournalDate);
        notesTextView = findViewById(R.id.detailJournalNotes);
        journalImageView = findViewById(R.id.detailJournalImage);

        expandJournalButton = findViewById(R.id.expandJournalButton);
        removeJournalButton = findViewById(R.id.removeJournalButton);
        shareJournalDetailButton = findViewById(R.id.shareJournalDetailButton);
        backButton = findViewById(R.id.journalBackButton);

        // Get journal details from intent
        Intent intent = getIntent();
        String journalId = intent.getStringExtra("journalId");  // Use for delete
        String title = intent.getStringExtra("journalTitle");
        String subtitle = intent.getStringExtra("journalSubtitle");
        String date = intent.getStringExtra("journalDate");
        String fullNotes = getIntent().getStringExtra("journalNotes");
        String imageUrl = intent.getStringExtra("journalImageUrl");

        // Set views with journal details
        titleTextView.setText(title);
        subtitleTextView.setText(subtitle);
        dateTextView.setText(date);
        notesTextView.setText(fullNotes);
        Picasso.get().load(imageUrl).into(journalImageView);

        expandJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtons();
            }
        });

        // Handle delete
        removeJournalButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("journals").document(journalId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Journal deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting journal", Toast.LENGTH_SHORT).show();
                    });
        });

        // Handle share
        shareJournalDetailButton.setOnClickListener(v -> {
            String shareContent = "Journal Title: " + title + "\n"
                    + "Subtitle: " + subtitle + "\n"
                    + "Date: " + date + "\n"
                    + "Notes: " + fullNotes;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
            startActivity(Intent.createChooser(shareIntent, "Share journal via"));
        });

        // Inside onCreate()
        backButton.setOnClickListener(v -> {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        });



    }

    private void toggleButtons() {
        Animation animShow = AnimationUtils.loadAnimation(this, R.anim.fab_slide_up);
        Animation animHide = AnimationUtils.loadAnimation(this, R.anim.fab_slide_down);
        Animation animShake = AnimationUtils.loadAnimation(this, R.anim.fab_shake_up);

        expandJournalButton.startAnimation(animShake);

        if (isExpanded) {
            removeJournalButton.startAnimation(animHide);
            shareJournalDetailButton.startAnimation(animHide);
            removeJournalButton.setVisibility(View.GONE);
            shareJournalDetailButton.setVisibility(View.GONE);
        } else {
            removeJournalButton.setVisibility(View.VISIBLE);
            shareJournalDetailButton.setVisibility(View.VISIBLE);
            removeJournalButton.startAnimation(animShow);
            shareJournalDetailButton.startAnimation(animShow);
        }
        isExpanded = !isExpanded;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
