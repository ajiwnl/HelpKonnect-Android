package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private RecyclerView bookingRecyclerView;
    private BookingHistoryAdapter bookingAdapter;
    private FirebaseFirestore db;
    private String professionalId;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        ImageView backBtn = findViewById(R.id.backBtn);
        bookingRecyclerView = findViewById(R.id.bookingRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);

        backBtn.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        professionalId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupRecyclerView();
        fetchBookingHistory();
    }

    private void setupRecyclerView() {
        bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingHistoryAdapter(new ArrayList<>());
        bookingRecyclerView.setAdapter(bookingAdapter);
    }

    private void fetchBookingHistory() {
        db.collection("bookings")
                .whereEqualTo("professionalId", professionalId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<BookingHistoryModel> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BookingHistoryModel booking = document.toObject(BookingHistoryModel.class);
                            bookings.add(booking);
                        }

                        if (bookings.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                            bookingRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyTextView.setVisibility(View.GONE);
                            bookingRecyclerView.setVisibility(View.VISIBLE);
                            bookingAdapter.updateData(bookings);
                        }
                    } else {
                        Log.e("BookingHistory", "Error fetching data", task.getException());
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                });
    }
}
