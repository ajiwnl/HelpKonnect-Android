package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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


        postingBackButton = findViewById(R.id.postingBackButton);
        postingButton = findViewById(R.id.postingButton);

        //Post Contents
        postingAddImage = findViewById(R.id.postingAddImage);
        postingEditText = findViewById(R.id.postingContentsEditText);

        postingBackButton.setOnClickListener(v -> {
            finish();
        });

        postingButton.setOnClickListener(v -> {
            savePost();
        });

        postingAddImage.setOnClickListener(v -> {
            openImagePicker();
        });
    }

    private void savePost() {
        //No clue
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String postContent = postingEditText.getText().toString().trim();
        //formattedDateTime

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
            postingAddImage.setImageURI(selectedImageUri);
        }
    }
}