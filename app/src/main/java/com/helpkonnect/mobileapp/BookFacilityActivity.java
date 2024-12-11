package com.helpkonnect.mobileapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BookFacilityActivity extends AppCompatActivity {
    private ImageView facilityImage;
    private TextView facilityName, facilityDescription;
    private EditText startTimeEditText, sessionDurationEditText, availableDateEditText;
    private ImageButton backButton;
    private Button requestBookButton;
    private String description, userId, facility;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_facility);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        facilityImage = findViewById(R.id.facilityImage);
        facilityName = findViewById(R.id.facilityName);
        facilityDescription = findViewById(R.id.facilityDescription);
        availableDateEditText = findViewById(R.id.AvailableDateEditText);
        startTimeEditText = findViewById(R.id.StartTimeEditText);
        backButton = findViewById(R.id.BookingBackButton);
        requestBookButton = findViewById(R.id.RequestBookButton);
        sessionDurationEditText = findViewById(R.id.SessionDurationEditText);

        backButton.setOnClickListener(v -> finish());

        if (getIntent().getExtras() != null) {
            String imageUrl = getIntent().getStringExtra("imageUrl");
            facility = getIntent().getStringExtra("name");
            description = getIntent().getStringExtra("description");
            Glide.with(this)
                    .load(imageUrl)
                    .into(facilityImage);

            facilityName.setText(facility);
            facilityDescription.setText(description);
        }

        availableDateEditText.setOnClickListener(v -> showDatePickerDialog());
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        requestBookButton.setOnClickListener(v -> {
            String amountText = sessionDurationEditText.getText().toString();
            String selectedDate = availableDateEditText.getText().toString();
            String selectedTime = startTimeEditText.getText().toString();

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            try {
                Date selectedDateTime = dateFormat.parse(selectedDate + " " + selectedTime);

                Calendar calendar = Calendar.getInstance();
                Date currentDateTime = calendar.getTime();

                if (selectedDateTime != null && selectedDateTime.before(currentDateTime)) {
                    Toast.makeText(this, "Cannot book for a past date or time", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveBookingDetails(userId, sessionDurationEditText.getText().toString());

            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
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

    private void saveBookingDetails(String userId, String sessionDuration) {
        String selectedDate = availableDateEditText.getText().toString();
        String selectedTime = startTimeEditText.getText().toString();

        String bookingDateTime = selectedDate + " " + selectedTime;

        Map<String, Object> booking = new HashMap<>();
        booking.put("bookingId", UUID.randomUUID().toString());
        booking.put("userId", userId);
        booking.put("professionalId", "");
        booking.put("amount", 0);
        booking.put("sessionDuration", sessionDuration);
        booking.put("bookingDate", bookingDateTime);
        booking.put("createdAt", FieldValue.serverTimestamp());
        booking.put("status", false);
        booking.put("facilityName", facility);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Booking", "Booking saved with ID: " + documentReference.getId());
                    Toast.makeText(this, "Successfully Booked! Please Wait For The Facility To Allocate Your Booking Schedule", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Booking", "Error saving booking: ", e);
                });
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