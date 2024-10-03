package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MusicPlayerActivity extends AppCompatActivity {

    private ImageButton musicPausePlayButton, musicPreviousButton, musicNextButton, musicBackButton;
    private TextView musicTitleDisplay;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ImageButtons
        musicPreviousButton = findViewById(R.id.MusicPreviousButton);
        musicPausePlayButton = findViewById(R.id.MusicPausePlayButton);
        musicNextButton = findViewById(R.id.MusicNextButton);
        musicBackButton = findViewById(R.id.MusicBackButton);

        // TextView
        musicTitleDisplay = findViewById(R.id.MusicTitleDisplay);
        musicTitleDisplay.setSelected(true);

        // Event Handlders
        musicBackButton.setOnClickListener(v -> finish());
        musicPausePlayButton.setOnClickListener(v -> {
            togglePlayPause();
        });
        musicPreviousButton.setOnClickListener(v -> {

        });
        musicNextButton.setOnClickListener(v -> {
        });

        musicTitleDisplay.requestFocus();
    }

    private void togglePlayPause() {
        if (isPlaying) {
            musicPausePlayButton.setImageResource(R.drawable.ic_play_circle_outline);
        } else {
            musicPausePlayButton.setImageResource(R.drawable.ic_pause_circle_outline);
        }
        isPlaying = !isPlaying;
    }
}
