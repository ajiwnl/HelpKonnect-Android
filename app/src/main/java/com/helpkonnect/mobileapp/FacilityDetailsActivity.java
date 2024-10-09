package com.helpkonnect.mobileapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
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

public class FacilityDetailsActivity extends AppCompatActivity {

    private TextView listTitle;
    private ImageButton backButton,commentButton;
    private TextSwitcher listTitleSwitcher;
    private String[] ListTitles = {"Professionals", "Comments"};
    private String currentFacility;
    private int currentIndex = 0;
    private ProgressBar loadingIndicator;
    private TextView errorTextView;
    private RecyclerView recyclerView;
    private List<CommentModel> comments = new ArrayList<>();

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

        commentButton.setOnClickListener( v -> {
        showCommentDialog();
        });


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
            if(currentIndex == 1){
                commentButton.setVisibility(View.VISIBLE);
                loadCommentData();
            }else {
                commentButton.setVisibility(View.GONE);
                loadProfessionalData();
            }

        });

        // For Facility Details Display
        Bundle args = getIntent().getExtras();
        if (args != null) {
            int logo = args.getInt("logo");
            String name = args.getString("name");
            String location = args.getString("location");
            float rating = args.getFloat("rating");
            currentFacility = name;

            ImageView facilityImage = findViewById(R.id.facilityImage);
            TextView facilityName = findViewById(R.id.FacilityName);
            TextView facilityLocation = findViewById(R.id.FacilityLocation);
            RatingBar facilityRatingBar = findViewById(R.id.FacilityRating);
            // Set Data
            facilityImage.setImageResource(logo);
            facilityName.setText(name);
            facilityLocation.setText(location);
            facilityRatingBar.setRating(rating);
        }

        loadProfessionalData();
    }

    private void loadProfessionalData() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            List<ProfessionalAdapter.Professional> professionals = new ArrayList<>();
            //Sample Data set
            professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "Jane Doe", "Counselor", "Helping you find your way."));
            professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "John Smith", "Therapist", "Supporting mental wellness."));
            professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "Emily White", "Life Coach", "Empowering you to reach your goals."));

            if (professionals.isEmpty()) {
                showError("Professionals list not available.");
            } else {
                setupProfessionalRecyclerView(professionals);
            }
        }, 2000);
    }
    private void loadCommentData() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            //comments.clear();

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
            intent.putExtra("image", professional.getImage());
            intent.putExtra("name", professional.getName());
            intent.putExtra("facility", currentFacility);
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
        //getUserid;
        CommentModel newComment = new CommentModel(userName, commentText);

        comments.add(newComment);
        if(comments.size() == 1){
            setupCommentRecyclerView(comments);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

}

