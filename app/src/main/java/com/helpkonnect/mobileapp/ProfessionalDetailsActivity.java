package com.helpkonnect.mobileapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ProfessionalDetailsActivity extends AppCompatActivity {

    private ImageView professionalImage;
    private TextView professionalName;
    private TextView associatedFacility;
    private EditText availableDateEditText;
    private EditText startTimeEditText;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_details);

        professionalImage = findViewById(R.id.ProfessionalImage);
        professionalName = findViewById(R.id.ProfessionalName);
        associatedFacility = findViewById(R.id.AssociatedFacility);
        availableDateEditText = findViewById(R.id.AvailableDateEditText);
        startTimeEditText = findViewById(R.id.StartTimeEditText);
        backButton = findViewById(R.id.ProfessionalBackButton);

        backButton.setOnClickListener(v -> finish());


        // Set up the professional details
        if (getIntent().getExtras() != null) {
            int imageResId = getIntent().getIntExtra("image", 0);
            String name = getIntent().getStringExtra("name");
            String facility = getIntent().getStringExtra("facility");
            professionalImage.setImageResource(imageResId);
            professionalName.setText(name);
            associatedFacility.setText("Associated with " + facility);
        }

        // Set up listeners for the EditText fields
        availableDateEditText.setOnClickListener(v -> showDatePickerDialog());
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog());
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
