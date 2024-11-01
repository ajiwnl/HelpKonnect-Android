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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.helpkonnect.mobileapp.JournalListAdapter.Journal;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TrackerFragment extends Fragment {

    private TextView dateDisplay, activityTitle, predictEmotionTxtView, getPredictEmotionTxtView;
    private List<Journal> journalList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView journalRecyclerView;
    private JournalListAdapter adapter;
    private RequestQueue requestQueue;
    private PieChart pieChart;
    private BarChart emotionBarChart;
    private View loaderView;
    private Map<String, Float> emotionData = new HashMap<>();

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
        predictEmotionTxtView = rootView.findViewById(R.id.predictedEmotion);
        pieChart = rootView.findViewById(R.id.EmotionChart);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        getPredictEmotionTxtView = rootView.findViewById(R.id.predictedEmotion);
        emotionBarChart = rootView.findViewById(R.id.EmotionBarChart);

        SimpleDateFormat dateFormatDefault = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String todayDate = dateFormatDefault.format(new Date());
        dateDisplay.setText(todayDate);
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        adapter = new JournalListAdapter(journalList, journal -> {
            String documentId = journal.getDocumentId(); // Retrieve the document ID of the clicked journal
            String journalUserId = mAuth.getCurrentUser().getUid(); // Retrieve the user ID

            // Set the title and date based on the selected journal
            activityTitle.setText(journal.getTitle());
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
            dateDisplay.setText(dateFormat.format(journal.getDate().toDate()));

            // Log the documentId and userId for verification
            Log.d("TrackerFragment", "Clicked Journal Document ID: " + documentId);
            Log.d("TrackerFragment", "User ID: " + journalUserId);

            // Perform actions using documentId and userId as needed
            userActivity(userId);
            analyzeEmotion(journal.getTranslatedNotes(), documentId);
        });

        journalRecyclerView.setAdapter(adapter);
        fetchAndAggregateEmotionData();
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

    private void fetchAndAggregateEmotionData() {
        db.collection("emotion_analysis")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("TrackerFragment", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        emotionData.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : snapshots) {
                            Map<String, Object> emotionsRaw = (Map<String, Object>) document.getData().get("emotions");
                            if (emotionsRaw != null) {
                                for (Map.Entry<String, Object> entry : emotionsRaw.entrySet()) {
                                    String emotionName = entry.getKey();
                                    float emotionValue = ((Number) entry.getValue()).floatValue();

                                    // Aggregate emotion values
                                    emotionData.put(emotionName, emotionData.getOrDefault(emotionName, 0f) + emotionValue);
                                }
                            }
                        }
                        displayBarChart(emotionData); // Call to display the aggregated data in the bar chart
                    }
                });
    }

    private void displayBarChart(Map<String, Float> emotions) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>(); // List to hold colors

        int index = 0;
        for (Map.Entry<String, Float> entry : emotions.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
            labels.add(entry.getKey());
            colors.add(generateRandomColor()); // Add a random color for each entry
        }

        BarDataSet dataSet = new BarDataSet(entries, "Emotions");
        dataSet.setColors(colors); // Set the list of random colors
        BarData barData = new BarData(dataSet);

        emotionBarChart.setData(barData);
        emotionBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        emotionBarChart.invalidate(); // Refresh the chart
    }


    private void analyzeEmotion(String translatedNotes, String journalId) { // Add journalId parameter
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
                        handleEmotionResponse(response, journalId); // Pass journalId here
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

    private void handleEmotionResponse(JSONObject response, String journalId) { // Add journalId parameter
        try {
            String predictedEmotion = response.getString("predicted_emotion");

            // Update the TextView with concatenated string
            predictEmotionTxtView.setText("Journal Highest Predicted Emotion: " + predictedEmotion);

            // Get top emotions
            JSONArray top4Emotions = response.getJSONArray("top_4_emotions");
            ArrayList<PieEntry> entries = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            emotionData.clear();

            for (int i = 0; i < top4Emotions.length(); i++) {
                JSONObject emotionObj = top4Emotions.getJSONObject(i);
                String emotionName = emotionObj.getString("emotion");
                float probability = (float) emotionObj.getDouble("probability");

                // Highlight the predicted emotion
                entries.add(new PieEntry(probability, emotionName));
                colors.add(generateRandomColor());

                // Store the emotion name and its probability in the map
                emotionData.put(emotionName, probability);

            }

            // Create dataset and update chart
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors); // Use the random colors
            PieData pieData = new PieData(dataSet);

            pieData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value + "%";
                }
            });
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.invalidate(); // Refresh the chart

            checkIfEntryExists(journalId);
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

    private void checkIfEntryExists(String journalId) {
        db.collection("journals")
                .document(journalId) // Get the journal document to retrieve the dateCreated
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the dateCreated from the journal document
                            Timestamp dateCreated = document.getTimestamp("dateCreated");

                            // Check if the emotion entry already exists
                            db.collection("emotion_analysis")
                                    .document(journalId)
                                    .get()
                                    .addOnCompleteListener(entryTask -> {
                                        if (entryTask.isSuccessful()) {
                                            DocumentSnapshot entryDocument = entryTask.getResult();
                                            if (entryDocument.exists()) {
                                                // The entry already exists, do nothing or log if necessary
                                                Log.d("TrackerFragment", "Entry already exists for journal ID: " + journalId);
                                            } else {
                                                // Save journalId and journalUserId to Firestore
                                                saveEmotionAnalysis(journalId, mAuth.getCurrentUser().getUid(), true, emotionData, dateCreated);
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.e("TrackerFragment", "Failed to get journal document: " + task.getException().getMessage());
                    }
                });
    }


    private void saveEmotionAnalysis(String journalId, String journalUserId, boolean isClicked, Map<String, Float> emotionData, Timestamp dateCreated) {
        Map<String, Object> data = new HashMap<>();
        data.put("journalUserId", journalUserId);
        data.put("emotions", emotionData);
        data.put("isClicked", isClicked);
        data.put("dateCreated", dateCreated);

        db.collection("emotion_analysis")
                .document(journalId)  // Use the journalId as document name
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("TrackerFragment", "Emotion analysis saved successfully"))
                .addOnFailureListener(e -> Log.w("TrackerFragment", "Error saving emotion analysis", e));
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

       /* private void fetchAndAggregateEmotionDataSimpleSum() {
        db.collection("emotion_analysis")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<EmotionSummary> weeklyEmotionSummary = new ArrayList<>();
                        for (int i = 0; i < 7; i++) {
                            String day = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date(System.currentTimeMillis() - (i * 86400000)));
                            weeklyEmotionSummary.add(new EmotionSummary(day));
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> emotionsRaw = (Map<String, Object>) document.getData().get("emotions");
                            if (emotionsRaw != null) {
                                Timestamp dateCreated = document.getTimestamp("dateCreated");
                                if (dateCreated != null) {
                                    String day = new SimpleDateFormat("EEE", Locale.getDefault()).format(dateCreated.toDate());
                                    for (Map.Entry<String, Object> entry : emotionsRaw.entrySet()) {
                                        String emotionName = entry.getKey();
                                        float emotionValue = ((Number) entry.getValue()).floatValue();
                                        for (EmotionSummary summary : weeklyEmotionSummary) {
                                            if (summary.getDay().equals(day)) {
                                                summary.addEmotion(emotionName, emotionValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        displayWeeklySummary(weeklyEmotionSummary);
                    } else {
                        Log.d("TrackerFragment", "Error getting documents: ", task.getException());
                    }
                });
    }*/

/*    private void displayWeeklySummary(List<EmotionSummary> weeklySummary) {
        // Set up a RecyclerView and an adapter
        RecyclerView summaryRecyclerView = getView().findViewById(R.id.SummaryRecyclerView);
        summaryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SummaryAdapter summaryAdapter = new SummaryAdapter(weeklySummary);
        summaryRecyclerView.setAdapter(summaryAdapter);
    }*/
}