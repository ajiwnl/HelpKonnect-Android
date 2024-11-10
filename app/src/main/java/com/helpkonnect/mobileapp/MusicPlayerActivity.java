package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;


public class MusicPlayerActivity extends AppCompatActivity {

    private ImageButton musicPausePlayButton, musicPreviousButton, musicNextButton, musicBackButton;
    private TextView musicTitleDisplay, currentTimeTextView, totalTimeTextView;
    private SeekBar seekBar;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private List<String> audioUrls = new ArrayList<>();
    private List<String> audioTitles = new ArrayList<>();
    private int currentIndex = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("resources")
                .whereEqualTo("type", "Audio")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String fileUrl = document.getString("fileURL");
                            String title = document.getString("name");
                            if (fileUrl != null && title != null) {
                                audioUrls.add(fileUrl);
                                audioTitles.add(title);
                            }
                        }
                        if (!audioUrls.isEmpty()) {
                            playAudio(currentIndex);
                        }
                    }
                });

        musicPreviousButton = findViewById(R.id.MusicPreviousButton);
        musicPausePlayButton = findViewById(R.id.MusicPausePlayButton);
        musicNextButton = findViewById(R.id.MusicNextButton);
        musicBackButton = findViewById(R.id.MusicBackButton);
        musicBackButton.setOnClickListener(v -> finish());

        musicTitleDisplay = findViewById(R.id.MusicTitleDisplay);
        currentTimeTextView = findViewById(R.id.MusicCurrentTimeDisplay);
        totalTimeTextView = findViewById(R.id.MusicTotalTimeDisplay);
        seekBar = findViewById(R.id.MusicSeekerBar);

        musicPausePlayButton.setOnClickListener(v -> togglePlayPause());
        musicPreviousButton.setOnClickListener(v -> playPreviousAudio());
        musicNextButton.setOnClickListener(v -> playNextAudio());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void playAudio(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            handler.removeCallbacks(updateSeekBar);
        }

        String url = audioUrls.get(index);
        String title = audioTitles.get(index);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();

            musicTitleDisplay.setText(title);
            isPlaying = true;
            musicPausePlayButton.setImageResource(R.drawable.ic_pause_circle_outline);

            seekBar.setMax(mediaPlayer.getDuration());
            totalTimeTextView.setText(formatTime(mediaPlayer.getDuration()));

            handler.postDelayed(updateSeekBar, 1000);

            mediaPlayer.setOnCompletionListener(mp -> {
                if (currentIndex < audioUrls.size() - 1) {
                    currentIndex++;
                    playAudio(currentIndex);
                } else {
                    isPlaying = false;
                    musicPausePlayButton.setImageResource(R.drawable.ic_play_circle_outline);
                    seekBar.setProgress(0);
                    currentTimeTextView.setText("00:00");
                    handler.removeCallbacks(updateSeekBar);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void playNextAudio() {
        if (currentIndex < audioUrls.size() - 1) {
            currentIndex++;
            playAudio(currentIndex);
        }
    }

    private void playPreviousAudio() {
        if (currentIndex > 0) {
            currentIndex--;
            playAudio(currentIndex);
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                musicPausePlayButton.setImageResource(R.drawable.ic_play_circle_outline);
            } else {
                mediaPlayer.start();
                musicPausePlayButton.setImageResource(R.drawable.ic_pause_circle_outline);
            }
            isPlaying = !isPlaying;
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTimeTextView.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}

