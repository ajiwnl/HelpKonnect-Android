package com.helpkonnect.mobileapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.helpkonnect.mobileapp.JournalListAdapter.Journal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TrackerFragment extends Fragment {

    private TextView dateDisplay, activityTitle;
    private List<Journal> journalList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView journalRecyclerView;
    private JournalListAdapter adapter;
    private RequestQueue requestQueue;
    private View loaderView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker, container, false);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(requireContext());
        db = FirebaseFirestore.getInstance();
        journalList = new ArrayList<>();
        activityTitle = rootView.findViewById(R.id.ActivityTitle);
        dateDisplay = rootView.findViewById(R.id.DateDisplay);
        journalRecyclerView = rootView.findViewById(R.id.JournalListView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        SimpleDateFormat dateFormatDefault = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String todayDate = dateFormatDefault.format(new Date());
        dateDisplay.setText(todayDate);
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        adapter = new JournalListAdapter(journalList, journal -> {

            activityTitle.setText(journal.getTitle()); // Set the title
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
            dateDisplay.setText(dateFormat.format(journal.getDate().toDate()));
            userActivity(userId);
            analyzeEmotion(journal.getTranslatedNotes());
        });
        journalRecyclerView.setAdapter(adapter);

        fetchJournals();
        return rootView;
    }

    private void fetchJournals() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        db.collection("journals")
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("TrackerFragment", "Listen failed.", e);
                        Toast.makeText(requireContext(), "Error getting documents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        journalList.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            String documentId = document.getId();
                            String title = document.getString("title");
                            String subtitle = document.getString("subtitle");
                            Timestamp date = document.getTimestamp("dateCreated");
                            String notes = document.getString("notes");
                            String translatedNotes = document.getString("translated_notes"); // Fetch translated_notes
                            String imageUrl = document.getString("imageUrl");
                            String preview = (notes != null && notes.length() > 45) ? notes.substring(0, 45) + "..." : notes;

                            // Create the Journal object with translatedNotes
                            JournalListAdapter.Journal journal = new JournalListAdapter.Journal(imageUrl, title, subtitle, date, preview, documentId);
                            journal.setFullNotes(notes);
                            journal.setTranslatedNotes(translatedNotes); // Set the translated notes using the setter method
                            journalList.add(journal);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void analyzeEmotion(String translatedNotes) {
        if (translatedNotes != null && !translatedNotes.isEmpty()) {
            // Show the loading indicator
            showLoader(true, null);

            String url = "http://192.168.1.5:5000/predict"; // Update with your API URL

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("text", translatedNotes);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        // Handle the response to extract emotions and update the chart
                        handleEmotionResponse(response);
                        // Hide the loading indicator
                        showLoader(false, null);
                    },
                    error -> {
                        error.printStackTrace();
                        // Hide the loading indicator in case of error
                        showLoader(false, null);
                        Toast.makeText(requireContext(), "Error analyzing emotions.", Toast.LENGTH_SHORT).show();
                    }
            );

            requestQueue.add(jsonObjectRequest);
        } else {
            Toast.makeText(requireContext(), "No translated notes available for analysis.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleEmotionResponse(JSONObject response) {
        try {
            String predictedEmotion = response.getString("predicted_emotion");

            PieChart pieChart = getView().findViewById(R.id.EmotionChart);

            // Get top emotions
            JSONArray top4Emotions = response.getJSONArray("top_4_emotions");
            ArrayList<PieEntry> entries = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            for (int i = 0; i < top4Emotions.length(); i++) {
                JSONObject emotionObj = top4Emotions.getJSONObject(i);
                String emotionName = emotionObj.getString("emotion");
                float probability = (float) emotionObj.getDouble("probability");

                // Highlight the predicted emotion
                if (emotionName.equals(predictedEmotion)) {
                    entries.add(new PieEntry(probability, emotionName + " (Predicted)"));
                } else {
                    entries.add(new PieEntry(probability, emotionName));
                }

                // Generate a random color for this entry
                colors.add(generateRandomColor());
            }

            // Create dataset and update chart
            PieDataSet dataSet = new PieDataSet(entries, "Top 4 Emotions");
            dataSet.setColors(colors); // Use the random colors
            PieData pieData = new PieData(dataSet);

            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.invalidate(); // Refresh the chart
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Function to generate a random color
    private int generateRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
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

    private void showLoader(boolean show, String message) {
        if (loaderView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            loaderView = inflater.inflate(R.layout.loader_tracker, null);
        }

        TextView loadingText = loaderView.findViewById(R.id.loadingText);
        if (message != null) {
            loadingText.setText(message);
        }

        if (show) {
            if (loaderView.getParent() == null) {
                getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }
}