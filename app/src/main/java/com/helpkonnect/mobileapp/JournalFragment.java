package com.helpkonnect.mobileapp;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class JournalFragment extends Fragment {

    private TextView currentDateTextView;
    ProgressBar loaderJournal;
    private ImageView createJournalButton, forWeatherIcon;
    private RecyclerView journalCollections;
    private List<JournalListAdapter.Journal> journalList;
    private JournalListAdapter adapter;
    private FirebaseFirestore db;
    private TextView currentTempTextView;
    private TextView currentWeatherTextView;
    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

    private TextView userGreetingTextView;

    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private String appid;
    private DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_journal, container, false);
        loaderJournal = rootView.findViewById(R.id.LoaderJournal);
        db = FirebaseFirestore.getInstance();
        journalList = new ArrayList<>();

        // Fetch the OpenWeather API Key
        fetchWeatherApiKey();

        currentTempTextView = rootView.findViewById(R.id.currentTemp);
        currentWeatherTextView = rootView.findViewById(R.id.currentWeather);
        forWeatherIcon = rootView.findViewById(R.id.weatherIcon);
        userGreetingTextView = rootView.findViewById(R.id.userGreetings);


        // RecyclerView setup
        journalCollections = rootView.findViewById(R.id.journalcollectionrecyclerview);
        journalCollections.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapter with click listener to navigate to JournalDetailActivity
        adapter = new JournalListAdapter(journalList, journal -> {
            //Convert Timestamp to String
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(journal.getDate().toDate());

            // Intent to pass journal data to the detail activity
            Intent intent = new Intent(requireContext(), JournalDetailActivity.class);
            intent.putExtra("journalId", journal.getDocumentId());
            intent.putExtra("journalTitle", journal.getTitle());
            intent.putExtra("journalSubtitle", journal.getSubtitle());
            intent.putExtra("journalDate", formattedDate);
            intent.putExtra("journalNotes", journal.getNotes());
            intent.putExtra("journalNotes", journal.getFullNotes());
            intent.putExtra("journalImageUrl", journal.getImageUrl());
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intent, options.toBundle());

        });

        journalCollections.setAdapter(adapter);

        currentDateTextView = rootView.findViewById(R.id.currentdate);
        currentDateTextView.setText(DateTodayString);

        createJournalButton = rootView.findViewById(R.id.createjournalbutton);
        createJournalButton.setOnClickListener(v -> {
            Intent toCreateJournal = new Intent(requireContext(), CreateJournalActivity.class);
            toCreateJournal.putExtra("isNewJournal", true);
            startActivity(toCreateJournal);
        });

        setRandomGreeting();
        setDailyNotification();
        fetchJournals(); // Load the journals
        fetchWeatherApiKey();
        return rootView;
    }

    private void fetchJournals() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        userActivity(userId);



        db.collection("journals")
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    loaderJournal.setVisibility(View.GONE);
                    if (e != null) {

                        Log.w("JournalFragment", "Listen failed.", e);
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
                            String imageUrl = document.getString("imageUrl");
                            String preview = (notes != null && notes.length() > 45) ? notes.substring(0, 45) + "..." : notes;

                            // Create Journal object with both truncated preview and full notes
                            JournalListAdapter.Journal journal = new JournalListAdapter.Journal(imageUrl, title, subtitle, date, preview, documentId);
                            journal.setFullNotes(notes);  // Add a new field to store full notes
                            journalList.add(journal);
                        }


                        adapter.notifyDataSetChanged();
                    }
                });
    }


    // Fetch API key from a remote server
    private void fetchWeatherApiKey() {
        String apiKeyUrl = "https://helpkonnect.vercel.app/api/weatherKey";

        // Fetch the API key using GET request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiKeyUrl,
                response -> {
                    Log.d("JournalFragment", "API Key Response: " + response); // Log the response
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        appid = jsonResponse.getString("apiKey"); // Assign the appid

                        // Log the fetched API key for debugging
                        Log.d("WeatherAPI", "Fetched API Key: " + appid);

                        // Now that the appid is fetched, call getWeatherDetails
                        getWeatherDetails();
                    } catch (JSONException e) {
                        Log.e("JournalFragment", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("JournalFragment", "Error fetching API key: " + error.toString()));

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    // Open Weather API for temperature details
    public void getWeatherDetails() {
        // Check if appid is valid
        if (appid == null || appid.isEmpty()) {
            Log.e("WeatherAPI", "API Key is null or empty!");
            Toast.makeText(requireContext(), "API Key is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        String city = "Cebu City";
        String country = "Philippines";

        // Construct URL using the appid
        String tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;

        // Log the tempUrl for debugging purposes
        Log.d("WeatherAPI", "Temp URL: " + tempUrl);

        // Use GET request instead of POST
        StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                double temp = jsonObjectMain.getDouble("temp") - 273.15; // Convert Kelvin to Celsius
                String suggestion = weatherSuggestion(temp); // Get suggestion based on temperature

                // Log temperature and suggestion
                String output = "Temp: " + df.format(temp) + " °C" + "\nSuggestion: " + suggestion;
                Log.d("WeatherDetails", output);

                // Update TextViews with temp and suggestion
                currentTempTextView.setText(df.format(temp) + " °C");
                currentWeatherTextView.setText(suggestion);

                updateWeatherIcon(temp, forWeatherIcon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(requireContext(), error.toString().trim(), Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    private String weatherSuggestion(double temp) {
        String[] hotSuggestions = {
                "Stay cool and hydrated!",
                "Maybe it's time for some ice cream!",
                "Perfect weather for a swim!",
                "Stay in the shade when possible.",
                "Don't forget your sunscreen!",
                "Light, breathable clothing is a must!",
                "How about a cold smoothie?",
                "Best to stay indoors during peak heat.",
                "Take frequent breaks if you're outside.",
                "An icy drink can help cool you down!"
        };

        String[] warmSuggestions = {
                "A nice day for a walk.",
                "Enjoy the outdoors!",
                "Great day for a picnic.",
                "Perfect for some outdoor sports!",
                "A beautiful day for a bike ride.",
                "Open the windows and let the breeze in.",
                "How about a nice outdoor brunch?",
                "Catch up on gardening!",
                "Ideal weather for a scenic hike.",
                "Wear light layers just in case!"
        };

        String[] coolSuggestions = {
                "Perfect for a cozy day indoors.",
                "How about some warm tea?",
                "Cool weather—stay comfy!",
                "Snuggle up with a good book.",
                "Maybe time for a light jacket.",
                "Great weather for a brisk walk.",
                "How about baking something warm?",
                "A light scarf might come in handy.",
                "A nice day for indoor activities.",
                "Consider layering up just in case!"
        };

        String[] chillySuggestions = {
                "Time for a sweater!",
                "A good day for a warm drink.",
                "Stay warm!",
                "Consider bundling up a bit more today.",
                "Perfect day for some hot cocoa!",
                "You might need a coat today.",
                "Stay inside and keep warm if possible.",
                "A chilly day—keep your feet warm!",
                "How about a bowl of soup?",
                "Mittens might be a good idea!"
        };

        Random random = new Random();
        String suggestion = "";

        if (temp > 30) {
            suggestion = hotSuggestions[random.nextInt(hotSuggestions.length)];
        } else if (temp > 24 && temp <= 30) {
            suggestion = warmSuggestions[random.nextInt(warmSuggestions.length)];
        } else if (temp >= 18 && temp <= 24) {
            suggestion = coolSuggestions[random.nextInt(coolSuggestions.length)];
        } else if (temp < 18) {
            suggestion = chillySuggestions[random.nextInt(chillySuggestions.length)];
        }


        return suggestion;
    }

    private void updateWeatherIcon(double temp, ImageView weatherIcon) {
        int iconResId = R.drawable.default_icon;

        if (temp > 30) {
            iconResId = R.drawable.sun_icon;
        } else if (temp > 24 && temp <= 30) {
            iconResId = R.drawable.cloudy_icon;
        } else if (temp >= 18 && temp <= 24) {
            iconResId = R.drawable.rain_icon;
        } else if (temp < 18) {
            iconResId = R.drawable.cold_icon;
        }

        // Set the image resource to the ImageView
        weatherIcon.setImageResource(iconResId);
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

    private void setRandomGreeting() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Greeting arrays for different times of day
        String[] morningGreetings = {
                "Good morning!", "Rise and shine!", "Morning sunshine!", "Hope you have a great day ahead!"
        };
        String[] afternoonGreetings = {
                "Good afternoon!", "Hope you're having a productive day!", "Keep up the good work!"
        };
        String[] eveningGreetings = {
                "Good evening!", "Hope you had a great day!", "Relax, you've earned it!"
        };

        Random random = new Random();
        String selectedGreeting;

        // Select greeting based on the current time
        if (hourOfDay >= 5 && hourOfDay < 12) {
            // Morning (5 AM to 12 PM)
            selectedGreeting = morningGreetings[random.nextInt(morningGreetings.length)];
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            // Afternoon (12 PM to 6 PM)
            selectedGreeting = afternoonGreetings[random.nextInt(afternoonGreetings.length)];
        } else {
            // Evening (6 PM to 5 AM)
            selectedGreeting = eveningGreetings[random.nextInt(eveningGreetings.length)];
        }

        // Set the selected greeting to the TextView
        userGreetingTextView.setText(selectedGreeting);
    }

    private void setDailyNotification() {
        // Check if the app can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API level 33 (Android 13)
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Request permission for exact alarms if not granted
                requestExactAlarmPermission();
                return;  // Return early if the permission is not granted yet
            }
        }

        // Proceed with scheduling the notification if permission is granted
        Intent intent = new Intent(requireContext(), JournalReminderReceiver.class);

        // Use FLAG_IMMUTABLE since you likely don't need to modify this PendingIntent after it is created
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set up AlarmManager to fire the intent every 5 minutes
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long interval = 60 * 60 * 1000;
            long triggerAtMillis = System.currentTimeMillis() + interval;

            // Use setExactAndAllowWhileIdle to ensure the alarm fires exactly at the specified time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For newer versions (Android 6.0 and above), use setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                // For older versions, use setRepeating
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, pendingIntent);
            }
        }
    }

    /*private void setDailyNotification() {
        // Check if the app can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API level 33 (Android 13)
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Request permission for exact alarms if not granted
                requestExactAlarmPermission();
                return;  // Return early if the permission is not granted yet
            }
        }

        Log.d("JournalFragment", "Setting daily notification...");

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("JournalFragment", "AlarmManager is null. Unable to set notification.");
            return;
        }

        Intent intent = new Intent(getContext(), JournalReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 01);  // Set the hour to midnight
        calendar.set(Calendar.MINUTE, 30);  // Set the minute to 50
        calendar.set(Calendar.SECOND, 0);  // Set the second to 0

        // If it's already past midnight today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d("JournalFragment", "Notification scheduled for: " + calendar.getTime());

        // Set the alarm
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }*/


    // Method to request permission for exact alarms
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivityForResult(intent, 1001);  // Custom request code
        }
    }


}