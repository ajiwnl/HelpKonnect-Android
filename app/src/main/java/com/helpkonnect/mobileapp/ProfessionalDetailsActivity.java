package com.helpkonnect.mobileapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfessionalDetailsActivity extends AppCompatActivity {

    private ImageView professionalImage;
    private TextView professionalName, associatedFacility;
    private EditText startTimeEditText, sessionDurationEditText, availableDateEditText;
    private ImageButton backButton;
    private Button requestBookButton;
    private String professionalId, userEmail, userUsername, userId, facility;
    private float rate, amount, subtotal;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    private static final String BASE_URL = "https://helpkonnect.vercel.app/api/createCheckout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_details);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        professionalImage = findViewById(R.id.ProfessionalImage);
        professionalName = findViewById(R.id.ProfessionalName);
        associatedFacility = findViewById(R.id.AssociatedFacility);
        availableDateEditText = findViewById(R.id.AvailableDateEditText);
        startTimeEditText = findViewById(R.id.StartTimeEditText);
        backButton = findViewById(R.id.ProfessionalBackButton);
        requestBookButton = findViewById(R.id.RequestBookButton);
        sessionDurationEditText = findViewById(R.id.SessionDurationEditText);

        backButton.setOnClickListener(v -> finish());

        if (getIntent().getExtras() != null) {
            String imageUrl = getIntent().getStringExtra("imageUrl");
            String name = getIntent().getStringExtra("name");
            facility = getIntent().getStringExtra("facilityName");
            professionalId = getIntent().getStringExtra("userId");
            rate = getIntent().getFloatExtra("rate", 0.0f);
            Glide.with(this)
                    .load(imageUrl) 
                    .into(professionalImage); 

            professionalName.setText(name);
            associatedFacility.setText("Associated with " + facility);
        }

        availableDateEditText.setOnClickListener(v -> showDatePickerDialog());
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            fetchUserCredentials(userId);
        }

        requestBookButton.setOnClickListener(v -> {
            String amountText = sessionDurationEditText.getText().toString();
            float amount;

            try {
                amount = Float.parseFloat(amountText);
                subtotal = amount * rate;

                createCheckoutSession(subtotal);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void fetchUserCredentials(String userId) {
        db.collection("credentials")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userEmail = document.getString("email");
                            userUsername = document.getString("username");
                        }
                    } else {
                        Log.e("UserCredentials", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            availableDateEditText.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void saveBookingDetails(String userId, String professionalId, float amount, String sessionDuration) {
        Map<String, Object> booking = new HashMap<>();
        booking.put("bookingId", UUID.randomUUID().toString());
        booking.put("userId", userId);
        booking.put("professionalId", professionalId);
        booking.put("amount", amount);
        booking.put("sessionDuration", sessionDuration);
        booking.put("bookingDate", availableDateEditText.getText().toString());
        booking.put("createdAt", FieldValue.serverTimestamp());
        booking.put("facilityName", facility);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Booking", "Booking saved with ID: " + documentReference.getId());
                    Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Booking", "Error saving booking: ", e);
                    Toast.makeText(this, "Error saving booking", Toast.LENGTH_SHORT).show();
                });
    }

    private void createCheckoutSession(float subtotal) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("amount", subtotal);
            requestBody.put("username", userUsername);
            requestBody.put("email", userEmail);
            requestBody.put("phone", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String checkoutUrl = response.getJSONObject("data").getJSONObject("attributes").getString("checkout_url");
                            Log.d("CheckoutSession", "Checkout URL: " + checkoutUrl);

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl));
                            startActivity(intent);

                            saveBookingDetails(userId, professionalId, subtotal, sessionDurationEditText.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CheckoutSession", "Request failed: " + error.getMessage());
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {

            String timeFormat = selectedHour < 12 ? "AM" : "PM";
            int displayHour = selectedHour % 12;
            if (displayHour == 0) displayHour = 12;

            String time = String.format("%02d:%02d %s", displayHour, selectedMinute, timeFormat);
            startTimeEditText.setText(time);
        }, hour, minute, false);

        timePickerDialog.show();
    }
}
