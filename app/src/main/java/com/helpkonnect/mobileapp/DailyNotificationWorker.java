package com.helpkonnect.mobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.Result;

public class DailyNotificationWorker extends Worker {

    private static final String TAG = "DailyNotificationWorker";
    private static final String jTAG = "Journal Reminder Tag";
    private static final String tTAG = "Tracker Reminder Tag";
    private static final String rTAG = "Resources Reminder Tag";
    private static final String ONE_KEY_URL = "https://helpkonnect.vercel.app/api/onesignalKey";
    private String oneSignalKey = null;
    private String oneSignalID = null;

    public DailyNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        fetchOneSignalKeys();

        return Result.success();
    }

    private void fetchOneSignalKeys() {
        // Fetch the API keys using a GET request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ONE_KEY_URL,
                response -> {
                    Log.d(TAG, "API Key Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        oneSignalID = jsonResponse.getString("onesignalID");
                        oneSignalKey = jsonResponse.getString("onesignalKey");

                        Log.d(TAG, "Fetched oneSignalID: " + oneSignalID);
                        Log.d(TAG, "Fetched oneSignalKey: " + oneSignalKey);

                        sendNotificationsIfNeeded();

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Error fetching API keys: " + error.toString()));

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void sendNotificationsIfNeeded() {
        // Ensure that the OneSignal keys are available before sending notifications
        if (oneSignalID != null && oneSignalKey != null) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);

            // Check preferences and send notifications if needed
            if (sharedPreferences.getBoolean("journal_notification", true)) {
                sendJournalDailyNotification();
            }
            if (sharedPreferences.getBoolean("analysis_notification", true)) {
                sendTrackerDailyNotification();
            }
            if (sharedPreferences.getBoolean("resources_notification", true)) {
                sendActivityReminderNotification();
            }
        } else {
            Log.e(TAG, "OneSignal keys are not available. Cannot send notifications.");
        }
    }

    private void sendTrackerDailyNotification() {
        Log.d(tTAG, "Setting up daily notification with OneSignal...");

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
                            Log.d(tTAG, "No entry for today. Sending reminder...");

                            try {
                                // Fetch the correct OneSignal player ID
                                String oneSignalPlayerId = OneSignal.getDeviceState().getUserId();


                                if (oneSignalPlayerId != null) {
                                    // Create the notification content
                                    JSONObject json = new JSONObject();
                                    json.put("app_id", oneSignalID);

                                    // Add title (headings) and content (contents)
                                    JSONObject headings = new JSONObject();
                                    headings.put("en", "Emotion Analysis Reminder");  // Set your desired title here
                                    json.put("headings", headings);

                                    JSONObject contents = new JSONObject();
                                    contents.put("en", "It’s time to analyze your emotions today!");
                                    json.put("contents", contents);
                                    json.put("include_player_ids", new JSONArray().put(oneSignalPlayerId));

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                            Request.Method.POST,
                                            "https://onesignal.com/api/v1/notifications",
                                            json,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    Log.d(tTAG, "Notification sent successfully: " + response.toString());
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e(tTAG, "Failed to send notification", error);
                                                }
                                            }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> headers = new HashMap<>();
                                            headers.put("Content-Type", "application/json");
                                            headers.put("Authorization", "Basic " + oneSignalKey);
                                            return headers;
                                        }
                                    };

                                    // Add the request to the queue
                                    Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
                                } else {
                                    Log.e(tTAG, "Invalid OneSignal player ID.");
                                }

                            } catch (JSONException e) {
                                Log.e(tTAG, "Error creating notification request", e);
                            }
                        }
                    });
        }
    }

    private void sendJournalDailyNotification() {
        Log.d(jTAG, "Setting up daily notification with OneSignal...");

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

            db.collection("journals")
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("dateCreated", startTimestamp)
                    .whereLessThanOrEqualTo("dateCreated", endTimestamp)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            Log.d(jTAG, "No entry for today. Sending reminder...");

                            try {
                                // Fetch the correct OneSignal player ID
                                String oneSignalPlayerId = OneSignal.getDeviceState().getUserId();


                                if (oneSignalPlayerId != null) {
                                    // Create the notification content
                                    JSONObject json = new JSONObject();
                                    json.put("app_id", oneSignalID);  // Replace with your OneSignal app ID

                                    // Add title (headings) and content (contents)
                                    JSONObject headings = new JSONObject();
                                    headings.put("en", "Journal Reminder");  // Set your desired title here
                                    json.put("headings", headings);

                                    JSONObject contents = new JSONObject();
                                    contents.put("en", "You haven't written your journal for today. Remember to write!");
                                    json.put("contents", contents);
                                    json.put("include_player_ids", new JSONArray().put(oneSignalPlayerId));

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                            Request.Method.POST,
                                            "https://onesignal.com/api/v1/notifications",
                                            json,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    Log.d(jTAG, "Notification sent successfully: " + response.toString());
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e(jTAG, "Failed to send notification", error);
                                                }
                                            }) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> headers = new HashMap<>();
                                            headers.put("Content-Type", "application/json");
                                            headers.put("Authorization", "Basic " + oneSignalKey);
                                            return headers;
                                        }
                                    };

                                    // Add the request to the queue
                                    Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
                                } else {
                                    Log.e(jTAG, "Invalid OneSignal player ID.");
                                }

                            } catch (JSONException e) {
                                Log.e(jTAG, "Error creating notification request", e);
                            }
                        }
                    });
        }
    }

    private void sendActivityReminderNotification() {
        Log.d(rTAG, "Setting up daily activity reminder with OneSignal...");

        // Check if the user is logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Calendar calendar = Calendar.getInstance();

            // Set the start of the day (00:00)
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startOfDay = calendar.getTime();

            // Set the end of the day (23:59)
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endOfDay = calendar.getTime();

            Timestamp startTimestamp = new Timestamp(startOfDay);
            Timestamp endTimestamp = new Timestamp(endOfDay);

            // Query the userActivity collection to count how many times "ResourcesFragment" was accessed today
            db.collection("userActivity")
                    .whereEqualTo("userId", userId) // Match the user's ID
                    .whereEqualTo("featureAccessed", "ResourcesFragment") // Match the feature accessed
                    .whereGreaterThanOrEqualTo("lastActive", startTimestamp) // Activity after the start of the day
                    .whereLessThanOrEqualTo("lastActive", endTimestamp) // Activity before the end of the day
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().size() < 3) {
                            Log.d(rTAG, "Less than 3 accesses for ResourcesFragment today. Sending reminder...");

                            try {
                                // Fetch the correct OneSignal player ID
                                String oneSignalPlayerId = OneSignal.getDeviceState().getUserId();

                                if (oneSignalPlayerId != null) {
                                    // Create the notification content
                                    JSONObject json = new JSONObject();
                                    json.put("app_id", oneSignalID);  // Replace with your OneSignal app ID

                                    // Add title (headings) and content (contents)
                                    JSONObject headings = new JSONObject();
                                    headings.put("en", "Help-Konnect Available Resources");  // Set your desired title here
                                    json.put("headings", headings);

                                    JSONObject contents = new JSONObject();
                                    contents.put("en", "Try using these mental health resources available in our app.");
                                    json.put("contents", contents);
                                    json.put("include_player_ids", new JSONArray().put(oneSignalPlayerId));

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                            Request.Method.POST,
                                            "https://onesignal.com/api/v1/notifications",
                                            json,
                                            response -> Log.d(jTAG, "Notification sent successfully: " + response.toString()),
                                            error -> Log.e(jTAG, "Failed to send notification", error)) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> headers = new HashMap<>();
                                            headers.put("Content-Type", "application/json");
                                            headers.put("Authorization", "Basic " + oneSignalKey);
                                            return headers;
                                        }
                                    };

                                    // Add the request to the queue
                                    Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
                                } else {
                                    Log.e(rTAG, "Invalid OneSignal player ID.");
                                }

                            } catch (JSONException e) {
                                Log.e(rTAG, "Error creating notification request", e);
                            }
                        } else if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(rTAG, "User has accessed ResourcesFragment 3 or more times today. No reminder needed.");
                        } else {
                            Log.e(rTAG, "Failed to query user activity: " + task.getException());
                        }
                    });
        }
    }

}
