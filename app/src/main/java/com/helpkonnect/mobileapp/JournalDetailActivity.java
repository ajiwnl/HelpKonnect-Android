package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class JournalDetailActivity extends AppCompatActivity {

    private TextView titleTextView, subtitleTextView, dateTextView, notesTextView;
    private ImageView journalImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_detail);

        titleTextView = findViewById(R.id.detailJournalTitle);
        subtitleTextView = findViewById(R.id.detailJournalSubtitle);
        dateTextView = findViewById(R.id.detailJournalDate);
        notesTextView = findViewById(R.id.detailJournalNotes);
        journalImageView = findViewById(R.id.detailJournalImage);

        // Get the journal details from the Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("journalTitle");
        String subtitle = intent.getStringExtra("journalSubtitle");
        String date = intent.getStringExtra("journalDate");
        String notes = intent.getStringExtra("journalNotes");
        String imageUrl = intent.getStringExtra("journalImageUrl");

        // Set the journal details to the views
        titleTextView.setText(title);
        subtitleTextView.setText(subtitle);
        dateTextView.setText(date);
        notesTextView.setText(notes);

        // Load the image using Picasso
        Picasso.get().load(imageUrl).into(journalImageView);
    }
}
