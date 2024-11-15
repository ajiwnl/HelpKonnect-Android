package com.helpkonnect.mobileapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.helpkonnect.mobileapp.JournalListAdapter.Journal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Date;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.onesignal.OneSignal;

public class TrackerFragment extends Fragment {

    private TextView dateDisplay, predictEmotionTxtView, totalEmotionTxtView, dateTxtView, specificEmotionsTxtView;
    private ImageButton saveBtn, shareBtn;
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
    private Spinner selectOptions ;
    private CardView barChartView, WeeklySummary;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracker, container, false);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(requireContext());
        db = FirebaseFirestore.getInstance();
        journalList = new ArrayList<>();
        dateDisplay = rootView.findViewById(R.id.DateDisplay);
        journalRecyclerView = rootView.findViewById(R.id.JournalListView);
        predictEmotionTxtView = rootView.findViewById(R.id.predictedEmotion);
        pieChart = rootView.findViewById(R.id.EmotionChart);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        emotionBarChart = rootView.findViewById(R.id.EmotionBarChart);
        selectOptions = rootView.findViewById(R.id.summarySpinner);
        barChartView = rootView.findViewById(R.id.BarChartHolder);
        WeeklySummary = rootView.findViewById(R.id.WeeklySummary);
        totalEmotionTxtView = rootView.findViewById(R.id.totalEmotionsTextView);
        specificEmotionsTxtView = rootView.findViewById(R.id.specEmotionTextView);
        dateTxtView =  rootView.findViewById(R.id.currentWeekTextView);
        //Save and share button on tracker fragment
        saveBtn = rootView.findViewById(R.id.saveBtn);
        shareBtn = rootView.findViewById(R.id.shareBtn);

        SimpleDateFormat dateFormatDefault = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String todayDate = dateFormatDefault.format(new Date());
        dateDisplay.setText(todayDate);
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        // Set up the Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.summary_options,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectOptions.setAdapter(spinnerAdapter);

        adapter = new JournalListAdapter(journalList, journal -> {
            String documentId = journal.getDocumentId(); // Retrieve the document ID of the clicked journal
            String journalUserId = mAuth.getCurrentUser().getUid(); // Retrieve the user ID

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
            dateDisplay.setText(dateFormat.format(journal.getDate().toDate()));

            // Log the documentId and userId for verification
            Log.d("TrackerFragment", "Clicked Journal Document ID: " + documentId);
            Log.d("TrackerFragment", "User ID: " + journalUserId);

            // Perform actions using documentId and userId as needed
            userActivity(userId);
            analyzeEmotion(journal.getTranslatedNotes(), documentId);
        });

        selectOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                if ("Overall Summary".equals(selectedOption)) {
                    barChartView.setVisibility(View.VISIBLE);
                    WeeklySummary.setVisibility(View.GONE); // Hide WeeklySummary card
                    fetchAndAggregateEmotionData();
                } else if ("Weekly Summary".equals(selectedOption)) {
                    barChartView.setVisibility(View.GONE); // Hide bar chart
                    WeeklySummary.setVisibility(View.VISIBLE); // Show WeeklySummary card
                    fetchAndAggregateEmotionDataText();
                } else {
                    barChartView.setVisibility(View.GONE); // Hide bar chart
                    WeeklySummary.setVisibility(View.GONE); // Hide WeeklySummary card
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                barChartView.setVisibility(View.GONE); // Hide bar chart
                WeeklySummary.setVisibility(View.GONE); // Hide WeeklySummary card
            }
        });

        journalRecyclerView.setAdapter(adapter);
        fetchJournals();
        /*setDailyNotification();*/
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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w("TrackerFragment", "User not logged in.");
            return;
        }
        String userId = user.getUid();

        db.collection("emotion_analysis")
                .whereEqualTo("journalUserId", userId)
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

    private void fetchAndAggregateEmotionDataText() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w("TrackerFragment", "User not logged in.");
            return;
        }
        String userId = user.getUid();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfCurrentWeek = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfCurrentWeek = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM. dd", Locale.getDefault());
        String startWeekString = dateFormat.format(startOfCurrentWeek);
        String endWeekString = dateFormat.format(endOfCurrentWeek);
        String weekRange = startWeekString + " - " + endWeekString;
        dateTxtView.setText(weekRange);


        db.collection("emotion_analysis")
                .whereEqualTo("journalUserId", userId)
                .whereGreaterThanOrEqualTo("dateCreated", startOfCurrentWeek)
                .whereLessThan("dateCreated", new Date())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("TrackerFragment", "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        emotionData.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : snapshots) {
                            Map<String, Object> emotionsRaw = (Map<String, Object>) document.getData().get("emotions");
                            if (emotionsRaw != null) {
                                for (Map.Entry<String, Object> entry : emotionsRaw.entrySet()) {
                                    String emotionName = entry.getKey();
                                    float emotionValue = ((Number) entry.getValue()).floatValue();
                                    emotionData.put(emotionName, emotionData.getOrDefault(emotionName, 0f) + emotionValue);
                                }
                            }
                        }
                        // Build a string with formatted emotion data
                        StringBuilder aggregatedDetails = new StringBuilder();
                        for (Map.Entry<String, Float> entry : emotionData.entrySet()) {
                            String formattedLog = String.format("Emotion: %s, Total Sum: %.2f%%", entry.getKey(), entry.getValue());
                            Log.w("TrackerFragment", formattedLog);
                            aggregatedDetails.append(formattedLog).append("\n");
                        }

                        // Get the total number of different emotions
                        int totalNumberOfEmotions = emotionData.size();
                        Log.w("TrackerFragment", "Total Number of Emotions: " + totalNumberOfEmotions);

                        // Update TextViews with the data
                        totalEmotionTxtView.setText("Total Emotions: " + totalNumberOfEmotions);
                        specificEmotionsTxtView.setText(aggregatedDetails.toString());
                    } else {
                        Log.w("TrackerFragment", "No emotion data found for the current week.");
                        totalEmotionTxtView.setText("Total Emotions: 0");
                        specificEmotionsTxtView.setText("No emotion data found for the current week.");

                    }
                });
    }



    private void displayBarChart(Map<String, Float> emotions) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : emotions.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
            labels.add(entry.getKey());
            colors.add(generateRandomColor());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Emotions");
        dataSet.setColors(colors); // Set the list of random colors
        BarData barData = new BarData(dataSet);

        emotionBarChart.setData(barData);
        emotionBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Set the position of the X-axis labels below the bars
        emotionBarChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

        emotionBarChart.invalidate(); // Refresh the chart
    }



    private void analyzeEmotion(String translatedNotes, String journalId) { // Add journalId parameter
        if (translatedNotes != null && !translatedNotes.isEmpty()) {
            // Show the loading indicator
            showLoader(true, null);

            String url = "http://192.168.1.9:5000/predict"; // Update with your API URL

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
            predictEmotionTxtView.setText("Highest Predicted Emotion: " + predictedEmotion);

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

   /* private void setDailyNotification() {
        Log.d("TrackerFragment", "Setting up daily notification with OneSignal...");

        // Check if the user hasn’t logged an entry today
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            Date startOfDay = calendar.getTime();

            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            Date endOfDay = calendar.getTime();

            Timestamp startTimestamp = new Timestamp(startOfDay);
            Timestamp endTimestamp = new Timestamp(endOfDay);

            db.collection("emotion_analysis")
                    .whereEqualTo("journalUserId", userId)
                    .whereGreaterThanOrEqualTo("dateCreated", startTimestamp)
                    .whereLessThanOrEqualTo("dateCreated", endTimestamp)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            Log.d("TrackerFragment", "No entry for today. Sending reminder...");

                            try {
                                // Fetch the correct OneSignal player ID
                                String oneSignalPlayerId = OneSignal.getDeviceState().getUserId();


                                if (oneSignalPlayerId != null) {
                                    // Create the notification content
                                    JSONObject json = new JSONObject();
                                    json.put("app_id", "3d064d90-5221-48da-aaa6-0702f8531c99");  // Replace with your OneSignal app ID

                                    // Add title (headings) and content (contents)
                                    JSONObject headings = new JSONObject();
                                    headings.put("en", "Emotion Analysis Reminder");  // Set your desired title here
                                    json.put("headings", headings);

                                    JSONObject contents = new JSONObject();
                                    contents.put("en", "It’s time to analyze your emotions today!");
                                    json.put("contents", contents);
                                    json.put("include_player_ids", new JSONArray().put(oneSignalPlayerId));


                                    // Replace with your OneSignal REST API key
                                    String apiKey = "os_v2_app_hude3ecsefenvkvga4bpquy4te4mxajyftqerken66en4mhhwrart2exdozrwrjhrf7sbivyriviti42pc32aev5ffkcwdsm26o63hy";  // Use your actual OneSignal API key here

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                            Request.Method.POST,
                                            "https://onesignal.com/api/v1/notifications",
                                            json,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    Log.d("OneSignal", "Notification sent successfully: " + response.toString());
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e("OneSignal", "Failed to send notification", error);
                                                }
                                            }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> headers = new HashMap<>();
                                            headers.put("Content-Type", "application/json");
                                            headers.put("Authorization", "Basic " + apiKey);
                                            return headers;
                                        }
                                    };

                                    // Add the request to the queue
                                    Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
                                } else {
                                    Log.e("TrackerFragment", "Invalid OneSignal player ID.");
                                }

                            } catch (JSONException e) {
                                Log.e("OneSignal", "Error creating notification request", e);
                            }
                        }
                    });
        }
    }*/


}