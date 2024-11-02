package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    private RecyclerView bookingListView;
    private TextView bookingErrorTextView, recentSwitcher;
    private ProgressBar loadingIndicator;
    private List<BookingModel> bookingList = new ArrayList<>();
    private BookingAdapter bookingAdapter;
    private boolean isAscending = true;
    private ImageView sortOrderIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize UI components
        ImageView backButton = findViewById(R.id.BookingBackButton);
        bookingListView = findViewById(R.id.BookingListRecyclerView);
        bookingErrorTextView = findViewById(R.id.BookingErrorTextView);
        loadingIndicator = findViewById(R.id.LoadingIndicator);
        recentSwitcher = findViewById(R.id.RecentSwitcher);
        sortOrderIcon = findViewById(R.id.SortOrderIcon);

        backButton.setOnClickListener(v -> finish());
        recentSwitcher.setOnClickListener(v -> toggleSortOrder());

        bookingAdapter = new BookingAdapter(bookingList, booking -> showBookingDetailsDialog(booking));
        bookingListView.setLayoutManager(new LinearLayoutManager(this));
        bookingListView.setAdapter(bookingAdapter);

        populateBookingHistory();
    }

    private void toggleSortOrder() {
        if (isAscending) {
            Collections.sort(bookingList, (b1, b2) -> b2.getBookingDate().compareTo(b1.getBookingDate()));
            sortOrderIcon.setImageResource(R.drawable.ic_down); // Change icon to down
        } else {
            Collections.sort(bookingList, (b1, b2) -> b1.getBookingDate().compareTo(b2.getBookingDate()));
            sortOrderIcon.setImageResource(R.drawable.ic_up);
        }
        isAscending = !isAscending;
        bookingAdapter.notifyDataSetChanged();
    }

    private void showBookingDetailsDialog(BookingModel booking) {
        BookingDetailsDialogFragment dialog = BookingDetailsDialogFragment.newInstance(
                booking.getBookingTitle(),
                booking.getBookingDate(),
                booking.getBookingDetails(),
                booking.getBookingStartTime(),
                booking.getBookingDuration(),
                booking.getBookingPrice()
        );
        dialog.show(getSupportFragmentManager(), "BookingDetailsDialogFragment");
    }


    private void populateBookingHistory() {
        loadingIndicator.setVisibility(View.VISIBLE);
        bookingErrorTextView.setVisibility(View.GONE);

        // Simulate data loading (add booking entries here)
        bookingList.add(new BookingModel(
                "Dr. John Smith",
                "2024-10-10",
                "Planning to relieve stress on work",
                "15:00",
                "2",
                "3000"
        ));

        bookingList.add(new BookingModel(
                "Dr. Mike Wazowski",
                "2024-09-15",
                "Coping with anxiety help",
                "08:30",
                "4",
                "5000"
        ));


        loadingIndicator.setVisibility(View.GONE);

        if (bookingList.isEmpty()) {
            bookingErrorTextView.setText("No booking history available.");
            bookingErrorTextView.setVisibility(View.VISIBLE);
        } else {
            bookingErrorTextView.setVisibility(View.GONE);
            bookingAdapter.notifyDataSetChanged();
        }
    }
}
