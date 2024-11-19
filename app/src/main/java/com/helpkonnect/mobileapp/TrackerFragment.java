package com.helpkonnect.mobileapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.Manifest;



public class TrackerFragment extends Fragment {

    private TextView dateDisplay, predictEmotionTxtView, totalEmotionTxtView, dateTxtView, specificEmotionsTxtView, weeklyTitleTxtView;

    private ImageButton saveBtn, shareBtn, expandBtn;
    private Boolean isExpanded = false;

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
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final String TAG = "PDFSave";


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
        weeklyTitleTxtView = rootView.findViewById(R.id.weeklySummaryTitle);

        //Save and share button on tracker fragment
        saveBtn = rootView.findViewById(R.id.saveBtn);
        shareBtn = rootView.findViewById(R.id.shareBtn);
        expandBtn = rootView.findViewById(R.id.MoreOptionsBtn);

        expandBtn.setOnClickListener( v ->{
            toggleButtons(rootView.getContext());
        });

        saveBtn.setOnClickListener( v ->{
            if (hasStoragePermission()) {
                Log.d(TAG, "Storage permission granted.");
                Toast.makeText(rootView.getContext(), "Weekly Emotion Summary, Saved successfully!", Toast.LENGTH_SHORT).show();
                saveViewAsPDF();
            } else {
                Log.d(TAG, "Requesting storage permission...");
                requestStoragePermission();
            }
        });

        shareBtn.setOnClickListener( v ->{
            Toast.makeText(rootView.getContext(), "Sharing PDF...", Toast.LENGTH_SHORT).show();
            sharePDF();
        });

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
                    isExpanded = false;
                    saveBtn.setVisibility(View.GONE);
                    shareBtn.setVisibility(View.GONE);
                    fetchAndAggregateEmotionData();
                } else if ("Weekly Summary".equals(selectedOption)) {
                    barChartView.setVisibility(View.GONE); // Hide bar chart
                    WeeklySummary.setVisibility(View.VISIBLE); // Show WeeklySummary card
                    fetchAndAggregateEmotionDataText();
                } else {
                    isExpanded = false;
                    saveBtn.setVisibility(View.GONE);
                    shareBtn.setVisibility(View.GONE);
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

    private void toggleButtons(Context context) {
        Animation animShow = AnimationUtils.loadAnimation(context, R.anim.fab_slide_up);
        Animation animHide = AnimationUtils.loadAnimation(context, R.anim.fab_slide_down);
        Animation animShake = AnimationUtils.loadAnimation(context, R.anim.fab_shake_up);

        expandBtn.startAnimation(animShake);

        if (isExpanded) {
            saveBtn.startAnimation(animHide);
            shareBtn.startAnimation(animHide);
            saveBtn.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
        } else {
            saveBtn.setVisibility(View.VISIBLE);
            shareBtn.setVisibility(View.VISIBLE);
            saveBtn.startAnimation(animShow);
            shareBtn.startAnimation(animShow);
        }
        isExpanded = !isExpanded;
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

            String url = "http://192.168.1.9:5000/predict";

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

        // Create a map to hold the activity details
        Map<String, Object> data = new HashMap<>();
        data.put("lastActive", currentTime);  // Timestamp of the activity
        data.put("featureAccessed", "TrackerFragment");  // The feature the user
        data.put("userId", userId);

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

    private boolean hasStoragePermission() {
        boolean permissionGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Storage permission status: " + permissionGranted);
        return permissionGranted;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(requireContext(), "Storage permission is required to save PDF", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Showing permission rationale to the user.");
        }
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "User granted storage permission.");
                saveViewAsPDF();
            } else {
                Log.d(TAG, "User denied storage permission.");
                Toast.makeText(requireContext(), "Permission denied to save PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveViewAsPDF() {
        Log.d(TAG, "Starting PDF creation process...");

        // Define page dimensions (8.5 x 11 inches in points, 1 inch = 72 points)
        int pageWidth = (int) (8.5 * 72); // Width in points
        int pageHeight = (int) (8 * 72); // Height in points

        // Create a PdfDocument
        PdfDocument document = new PdfDocument();

        // Define the page size
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        // Start a new page
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the canvas to draw
        Canvas canvas = page.getCanvas();

        // Set up Paint for drawing text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(16);

        // Load the Help Konnect logo
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.hk_headv2);

        // Scale the logo proportionally to fit the page width
        int logoMaxWidth = pageWidth; // Max width available for the logo
        double aspectRatio = (double) logo.getHeight() / logo.getWidth(); // Aspect ratio (height/width)
        int logoScaledWidth = logoMaxWidth; // Use full width of the page
        int logoScaledHeight = (int) (logoMaxWidth * aspectRatio); // Scale height proportionally

        // Create a scaled bitmap of the logo
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoScaledWidth, logoScaledHeight, true);

        // Draw the logo at the very top of the page
        float logoX = 0; // X position at the very left
        float logoY = 0; // Y position at the very top
        canvas.drawBitmap(scaledLogo, logoX, logoY, null);

        // Position the text below the logo
        float textX = 50; // X position for text with some margin
        float textY = logoY + logoScaledHeight + 20; // Y position below the logo with padding

        // Set up Paint for the bold date text
        Paint dateTextPaint = new Paint();
        dateTextPaint.setColor(Color.BLACK);
        dateTextPaint.setTextSize(18);
        dateTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Extract text from TextViews
        String dateText = dateTxtView.getText().toString();
        String totalEmotion = totalEmotionTxtView.getText().toString();
        String specificEmotions = specificEmotionsTxtView.getText().toString();

        // Draw the date text with bold font and size 18
        canvas.drawText(dateText, textX, textY, dateTextPaint);
        textY += 40; // Move down for the next line (adjusted for larger font size)

        // Draw the totalEmotion text
        canvas.drawText(totalEmotion, textX, textY, textPaint);
        textY += 30; // Move down for the next line

        // Split and draw multi-line text for specific emotions
        String[] emotionLines = specificEmotions.split("\n");
        for (String line : emotionLines) {
            canvas.drawText(line, textX, textY, textPaint);
            textY += 30; // Move down for each line
        }

        // Finish the page
        document.finishPage(page);

        // Save the PDF to storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePDFUsingMediaStore(document);
        } else {
            savePDFUsingFileSystem(document);
        }

        Log.d(TAG, "PDF creation process completed successfully.");
    }

    private void sharePDF() {
        // Define the file location (same path as in your savePDF methods)
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyPDFs");
        File pdfFile = new File(pdfDir, "EmotionSummary_" + dateTxtView.getText().toString() + ".pdf");

        if (pdfFile.exists()) {
            // Create a URI for the PDF file
            Uri pdfUri = FileProvider.getUriForFile(requireContext(), "com.helpkonnect.mobileapp.fileprovider", pdfFile);

            // Create an Intent to share the PDF
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Emotion Summary PDF");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's the Emotion Summary PDF.");

            // Grant temporary permission to the receiving app to access the file
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start the chooser (this allows the user to pick an app to share with)
            startActivity(Intent.createChooser(shareIntent, "Share PDF"));
        } else {
            Log.e(TAG, "PDF file does not exist.");
            Toast.makeText(requireContext(), "PDF file not found. Please save it first.", Toast.LENGTH_LONG).show();
        }
    }

    private void savePDFUsingMediaStore(PdfDocument document) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Log.e(TAG, "MediaStore requires API level 29 or higher");
            savePDFUsingFileSystem(document);
            return;
        }

        try {
            String fileName = "EmotionSummary_"+dateTxtView.getText().toString();;

            // Set up values for the PDF file
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/MyPDFs");

            // Insert the new file into MediaStore and get the URI for the file
            Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                // Open the OutputStream for the URI returned by the insert
                try (OutputStream fos = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (fos != null) {
                        document.writeTo(fos);
                        Log.d(TAG, "PDF saved successfully using MediaStore.");
                        Toast.makeText(requireContext(), "PDF saved in Downloads/MyPDFs folder.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Failed to get OutputStream from MediaStore.");
                        Toast.makeText(requireContext(), "Failed to save PDF.", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error saving PDF using MediaStore: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Failed to insert file into MediaStore.");
                Toast.makeText(requireContext(), "Failed to save PDF in MediaStore.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting PDF file into MediaStore: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
            Log.d(TAG, "PDF document closed.");
        }
    }

    private void savePDFUsingFileSystem(PdfDocument document) {
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyPDFs");
        if (!pdfDir.exists()) {
            boolean dirCreated = pdfDir.mkdirs();
            Log.d(TAG, "Creating PDF directory: " + dirCreated);
        }

        File pdfFile = new File(pdfDir, "textview_content.pdf");

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
            Log.d(TAG, "PDF saved successfully at: " + pdfFile.getAbsolutePath());
            Toast.makeText(requireContext(), "PDF saved at: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
            Log.d(TAG, "PDF document closed.");
        }
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