package com.helpkonnect.mobileapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class BookingReminderWorker extends Worker {

    private static final String TAG = "BookingReminderWorker";
    private static final String ONE_KEY_URL = "https://helpkonnect.vercel.app/api/onesignalKey";

    private String oneSignalKey = null;
    private String oneSignalID = null;

    public BookingReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        fetchOneSignalKeys();
        sendBookingReminderNotification();
        return Result.success();
    }

    private void fetchOneSignalKeys() {
        // Fetch the API keys using GET request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ONE_KEY_URL,
                response -> {
                    Log.d(TAG, "API Key Response: " + response); // Log the response
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        oneSignalID = jsonResponse.getString("onesignalID");
                        oneSignalKey = jsonResponse.getString("onesignalKey");

                        Log.d(TAG, "Fetched oneSignalID: " + oneSignalID);
                        Log.d(TAG, "Fetched oneSignalKey: " + oneSignalKey);

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Error fetching API keys: " + error.toString())
        );
        RequestQueue requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void sendBookingReminderNotification() {
        Log.d("BookingReminder", "Setting up booking reminder notification with OneSignal...");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String bookingDateStr = document.getString("bookingDate");
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                                    Date bookingDate = format.parse(bookingDateStr);

                                    long currentTimeMillis = System.currentTimeMillis();
                                    long timeDifference = bookingDate.getTime() - currentTimeMillis;

                                    // Check for a 30-minute notification window
                                    if (timeDifference > 0 && timeDifference <= 1800000) {
                                        sendOneSignalNotification(bookingDate);
                                    }
                                } catch (Exception e) {
                                    Log.e("BookingReminder", "Error parsing booking date/time", e);
                                }
                            }
                        } else {
                            Log.e("BookingReminder", "Failed to fetch bookings: ", task.getException());
                        }
                    });
        }
    }

    private void sendOneSignalNotification(Date bookingDate) {
        String oneSignalPlayerId = OneSignal.getDeviceState().getUserId();

        if (oneSignalPlayerId != null) {
            try {
                // Construct the notification payload
                JSONObject json = new JSONObject();
                Log.d("BookingReminder", "Sending notification with App ID: " + oneSignalID);
                json.put("app_id", oneSignalID);  // Ensure this is not null or empty


                JSONObject headings = new JSONObject();
                headings.put("en", "Booking Reminder");

                String bookingTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(bookingDate);
                JSONObject contents = new JSONObject();
                contents.put("en", "You have a booking at " + bookingTime + ". Be prepared!");

                json.put("headings", headings);
                json.put("contents", contents);
                json.put("include_player_ids", new JSONArray().put(oneSignalPlayerId));

                Log.d(TAG, "Sending notification: " + json.toString()); // Log the JSON request

                // Send the notification using a POST request
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        "https://onesignal.com/api/v1/notifications",
                        json,
                        response -> Log.d("BookingReminder", "Notification sent successfully: " + response.toString()),
                        error -> {
                            Log.e("BookingReminder", "Failed to send notification: " + error.getMessage());
                            if (error.networkResponse != null) {
                                Log.e("BookingReminder", "Response Code: " + error.networkResponse.statusCode);
                                Log.e("BookingReminder", "Error Body: " + new String(error.networkResponse.data));
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Basic " + oneSignalKey); // OneSignal Key
                        return headers;
                    }
                };

                // Add the request to the queue
                Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);

            } catch (JSONException e) {
                Log.e("BookingReminder", "Error creating notification request", e);
            }
        } else {
            Log.e("BookingReminder", "Invalid OneSignal player ID.");
        }
    }
}