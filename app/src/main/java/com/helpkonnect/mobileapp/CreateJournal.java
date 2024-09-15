package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//For Journal Date
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CreateJournal extends AppCompatActivity {

    private ImageView backButton,saveButton,shareButton;
    private EditText journalTitle,journalSubtitle,journalNotes;
    private TextView journalDate;
    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Buttons
        backButton = findViewById(R.id.journalBackButton);
        saveButton = findViewById(R.id.saveJournalButton);
        shareButton = findViewById(R.id.shareJournalButton);

        //EditTextFields
        journalTitle = findViewById(R.id.journalEntryTitleEditText);
        journalSubtitle = findViewById(R.id.journalEntrySubtitleEditText);
        journalNotes = findViewById(R.id.journalEntryNotesEditText);

        //TextView for Date
        journalDate = findViewById(R.id.journalEntryDateTextView);
        journalDate.setText(DateTodayString);

        backButton.setOnClickListener(v -> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Journal Entry Status")
                    .setMessage("Journal Saved")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

        shareButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Cannot Share Yet")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

    }
}