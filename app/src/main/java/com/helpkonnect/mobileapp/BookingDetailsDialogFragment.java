package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BookingDetailsDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DATE = "date";
    private static final String ARG_DETAILS = "details";
    private static final String ARG_START_TIME = "startTime";
    private static final String ARG_DURATION = "duration";
    private static final String ARG_PRICE = "price";

    public static BookingDetailsDialogFragment newInstance(String title, String date, String details,
                                                     String startTime, String duration, String price) {
        BookingDetailsDialogFragment fragment = new BookingDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DATE, date);
        args.putString(ARG_DETAILS, details);
        args.putString(ARG_START_TIME, startTime);
        args.putString(ARG_DURATION, duration);
        args.putString(ARG_PRICE, price);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookingdetailsdialog, container, false);

        TextView titleTextView = view.findViewById(R.id.BookingTitleTextView);
        TextView dateTextView = view.findViewById(R.id.BookingDateTextView);
        TextView detailsTextView = view.findViewById(R.id.BookingDetailsTextView);
        TextView startTimeTextView = view.findViewById(R.id.BookingStartTimeTextView);
        TextView durationTextView = view.findViewById(R.id.BookingDurationTextView);
        TextView priceTextView = view.findViewById(R.id.BookingPriceTextView);
        ImageView dismissButton = view.findViewById(R.id.DetailsDismissButton);

        if (getArguments() != null) {
            titleTextView.setText(getArguments().getString(ARG_TITLE));
            dateTextView.setText(getArguments().getString(ARG_DATE));
            detailsTextView.setText(getArguments().getString(ARG_DETAILS));
            startTimeTextView.setText(getArguments().getString(ARG_START_TIME));
            durationTextView.setText(getArguments().getString(ARG_DURATION) + "Hours");
            priceTextView.setText("Php" + getArguments().getString(ARG_PRICE));
        }

        dismissButton.setOnClickListener(v -> dismiss());

        return view;
    }
}


