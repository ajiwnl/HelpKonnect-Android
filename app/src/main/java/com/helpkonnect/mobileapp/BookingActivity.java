package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private RecyclerView bookingListView;
    private TextView bookingErrorTextView, recentSwitcher;
    private ProgressBar loadingIndicator;
    private List<BookingModel> bookingList = new ArrayList<>();
    private BookingAdapter bookingAdapter;
    private boolean isAscending = true;
    private ImageView sortOrderIcon;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        fetchBookingHistory();
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

    private void fetchBookingHistory() {
        loadingIndicator.setVisibility(View.VISIBLE);
        bookingErrorTextView.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        bookingList.clear();
                        List<Task<DocumentSnapshot>> credentialTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String professionalId = document.getString("professionalId");
                            Timestamp timestamp = document.getTimestamp("createdAt");
                            String createdAt = "";

                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                // Format the date as a string
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                createdAt = dateFormat.format(date);
                            }
                            BookingModel booking = new BookingModel(
                                    document.getString("facilityName"),
                                    createdAt,
                                    "",
                                    document.getString("bookingDate"),
                                    document.getString("sessionDuration"),
                                    String.valueOf(document.getLong("amount"))
                            );

                            bookingList.add(booking);

                            // Fetch professional's name
                            Task<DocumentSnapshot> credentialTask = db.collection("credentials")
                                    .document(professionalId)
                                    .get();
                            credentialTasks.add(credentialTask);
                        }

                        // Handle professional name fetch results
                        Tasks.whenAllComplete(credentialTasks)
                                .addOnCompleteListener(credentialsTask -> {
                                    for (int i = 0; i < credentialTasks.size(); i++) {
                                        Task<DocumentSnapshot> credentialTask = credentialTasks.get(i);
                                        if (credentialTask.isSuccessful() && credentialTask.getResult() != null) {
                                            DocumentSnapshot credentialDoc = credentialTask.getResult();
                                            String firstName = credentialDoc.getString("firstName");
                                            String lastName = credentialDoc.getString("lastName");
                                            BookingModel booking = bookingList.get(i);

                                            // Update booking details with professional's name
                                            booking.setBookingDetails(firstName + " " + lastName);
                                        }
                                    }

                                    if (bookingList.isEmpty()) {
                                        bookingErrorTextView.setText("No booking history available.");
                                        bookingErrorTextView.setVisibility(View.VISIBLE);
                                    } else {
                                        bookingErrorTextView.setVisibility(View.GONE);
                                        toggleSortOrder(); // Ensure initial sort order is applied
                                    }
                                    bookingAdapter.notifyDataSetChanged();
                                });
                    } else {
                        Log.e("BookingActivity", "Error fetching bookings: ", task.getException());
                        bookingErrorTextView.setText("Failed to load booking history.");
                        bookingErrorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

}
