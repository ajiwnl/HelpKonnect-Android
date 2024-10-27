package com.helpkonnect.mobileapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FacilityDetailsActivity extends AppCompatActivity {

    private TextView listTitle;
    private ImageButton backButton, commentButton;
    private TextSwitcher listTitleSwitcher;
    private String[] ListTitles = {"Professionals", "Comments"};
    private String currentFacility;
    private int currentIndex = 0;
    private ProgressBar loadingIndicator;
    private TextView errorTextView;
    private RecyclerView recyclerView;
    private List<CommentModel> comments = new ArrayList<>();
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_details);

        listTitle = findViewById(R.id.ListTitleTextView);
        listTitleSwitcher = findViewById(R.id.ListChanger);
        backButton = findViewById(R.id.FacilityBackButton);
        loadingIndicator = findViewById(R.id.ListLoading);
        errorTextView = findViewById(R.id.ListNotAvailableError);
        recyclerView = findViewById(R.id.FacilityListView);
        commentButton = findViewById(R.id.CreateCommentButton);

        backButton.setOnClickListener(v -> finish());

        commentButton.setOnClickListener(v -> showCommentDialog());

        listTitleSwitcher.setFactory(() -> {
            TextView textView = new TextView(this);
            textView.setTextSize(10);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setText(ListTitles[currentIndex]);
            listTitle.setText(ListTitles[currentIndex]);
            commentButton.setVisibility(View.GONE);
            return textView;
        });

        listTitleSwitcher.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % ListTitles.length;
            listTitleSwitcher.setText(ListTitles[currentIndex]);
            listTitle.setText(ListTitles[currentIndex]);
            if (currentIndex == 1) {
                commentButton.setVisibility(View.VISIBLE);
                loadCommentData();
            } else {
                commentButton.setVisibility(View.GONE);
                loadAssociatedProfessionals();
            }
        });

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String imageUrl = args.getString("imageUrl");
            name = args.getString("name");
            String location = args.getString("location");
            float rating = args.getFloat("rating");
            currentFacility = args.getString("userId");

            ImageView facilityImage = findViewById(R.id.facilityImage);
            TextView facilityName = findViewById(R.id.FacilityName);
            TextView facilityLocation = findViewById(R.id.FacilityLocation);
            RatingBar facilityRatingBar = findViewById(R.id.FacilityRating);
            
            Glide.with(this)
                .load(imageUrl)
                .into(facilityImage);

            facilityName.setText(name);
            facilityLocation.setText(location);
            facilityRatingBar.setRating(rating);
        }

        loadAssociatedProfessionals();
    }

    private void loadAssociatedProfessionals() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("credentials")
            .whereEqualTo("associated", currentFacility) // Match the associated field with the facility's userId
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<ProfessionalAdapter.Professional> professionals = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        ProfessionalAdapter.Professional professional = new ProfessionalAdapter.Professional(
                            document.getString("imageUrl"),
                            document.getString("username"),
                            document.getString("role"),
                            document.getString("bio")
                        );
                        professionals.add(professional);
                    }

                    if (professionals.isEmpty()) {
                        showError("No associated professionals found.");
                    } else {
                        setupProfessionalRecyclerView(professionals);
                    }
                } else {
                    showError("Failed to load professionals: " + task.getException().getMessage());
                }
            });
    }

    private void loadCommentData() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            if (comments.isEmpty()) {
                showError("No Comments Yet Add One...");
            } else {
                setupCommentRecyclerView(comments);
            }
        }, 2000);
    }

    private void setupCommentRecyclerView(List<CommentModel> comments) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CommentAdapter adapter = new CommentAdapter(comments);
        recyclerView.setAdapter(adapter);

        loadingIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setupProfessionalRecyclerView(List<ProfessionalAdapter.Professional> professionals) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ProfessionalAdapter adapter = new ProfessionalAdapter(professionals, professional -> {
            Intent intent = new Intent(FacilityDetailsActivity.this, ProfessionalDetailsActivity.class);
            intent.putExtra("imageUrl", professional.getImage());
            intent.putExtra("name", professional.getName());
            intent.putExtra("facility", currentFacility);
            intent.putExtra("facilityName", name);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadingIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        loadingIndicator.setVisibility(View.GONE);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void showCommentDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_comment, null);
        EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        CheckBox checkBoxAnonymous = dialogView.findViewById(R.id.CommentAsAnonymous);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Comment")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialogInterface, i) -> {
                    String commentText = editTextComment.getText().toString();
                    boolean isAnonymous = checkBoxAnonymous.isChecked();
                    if (!commentText.isEmpty()) {
                        addComment(commentText, isAnonymous);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void addComment(String commentText, boolean isAnonymous) {
        String userName = isAnonymous ? "Anonymous Client" : "User";
        CommentModel newComment = new CommentModel(userName, commentText);

        comments.add(newComment);
        if (comments.size() == 1) {
            setupCommentRecyclerView(comments);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
