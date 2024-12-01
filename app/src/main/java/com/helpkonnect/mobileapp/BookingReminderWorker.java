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
        fetchOneSignalKeys(new OnKeysFetchedCallback() {
            @Override
            public void onKeysFetched() {
                sendBookingReminderNotification();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to fetch OneSignal keys: " + error);
            }
        });
        return Result.success();
    }

    private void fetchOneSignalKeys(OnKeysFetchedCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ONE_KEY_URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        oneSignalID = jsonResponse.getString("onesignalID");
                        oneSignalKey = jsonResponse.getString("onesignalKey");

                        Log.d(TAG, "Fetched oneSignalID: " + oneSignalID);
                        Log.d(TAG, "Fetched oneSignalKey: " + oneSignalKey);
                        callback.onKeysFetched();
                    } catch (JSONException e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> callback.onError(error.toString())
        );
        Volley.newRequestQueue(this.getApplicationContext()).add(stringRequest);
    }

    interface OnKeysFetchedCallback {
        void onKeysFetched();
        void onError(String error);
    }


    private void sendBookingReminderNotification() {
        if (oneSignalID == null || oneSignalID.isEmpty()) {
            Log.e("BookingReminder", "OneSignal app ID is null or empty. Cannot send notification.");
            return;
        }

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
                                String facilityName = document.getString("facilityName");
                                String professionalId = document.getString("professionalId"); // Fetch professionalId

                                try {
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                                    Date bookingDate = format.parse(bookingDateStr);

                                    long currentTimeMillis = System.currentTimeMillis();
                                    long timeDifference = bookingDate.getTime() - currentTimeMillis;

                                    // Check for a 30-minute notification window
                                    if (timeDifference > 0 && timeDifference <= 1800000) {
                                        // Notify the user
                                        sendOneSignalNotification(bookingDate, facilityName, OneSignal.getDeviceState().getUserId());

                                        // Fetch and notify the professional
                                        fetchProfessionalPlayerId(professionalId, professionalPlayerId -> {
                                            sendOneSignalNotification(bookingDate, facilityName, professionalPlayerId);
                                        });
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

    private void fetchProfessionalPlayerId(String professionalId, OnProfessionalPlayerIdFetchedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userId", professionalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String playerId = document.getString("playerId");
                            if (playerId != null && !playerId.isEmpty()) {
                                callback.onPlayerIdFetched(playerId);
                            } else {
                                Log.e("BookingReminder", "Professional Player ID not found.");
                            }
                        }
                    } else {
                        Log.e("BookingReminder", "Failed to fetch professional Player ID: ", task.getException());
                    }
                });
    }

    interface OnProfessionalPlayerIdFetchedCallback {
        void onPlayerIdFetched(String playerId);
    }

    private void sendOneSignalNotification(Date bookingDate, String facilityName, String playerId) {
        if (playerId != null && !playerId.isEmpty()) {
            try {
                JSONObject json = new JSONObject();
                json.put("app_id", oneSignalID);

                JSONObject headings = new JSONObject();
                headings.put("en", "Upcoming Booking Reminder");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String bookingDateFormatted = dateFormat.format(bookingDate);
                String bookingTimeFormatted = timeFormat.format(bookingDate);

                String facilityText = (facilityName != null && !facilityName.isEmpty()) ? facilityName : "your facility";

                JSONObject contents = new JSONObject();
                contents.put("en", "You have a booking at " + facilityText + " on " + bookingDateFormatted + " at " + bookingTimeFormatted + ".");

                json.put("headings", headings);
                json.put("contents", contents);
                json.put("include_player_ids", new JSONArray().put(playerId));

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        "https://onesignal.com/api/v1/notifications",
                        json,
                        response -> Log.d("BookingReminder", "Notification sent successfully: " + response.toString()),
                        error -> Log.e("BookingReminder", "Failed to send notification: ", error)
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Basic " + oneSignalKey);
                        return headers;
                    }
                };

                Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);

            } catch (JSONException e) {
                Log.e("BookingReminder", "Error creating notification request", e);
            }
        } else {
            Log.e("BookingReminder", "Invalid Player ID.");
        }
    }

}