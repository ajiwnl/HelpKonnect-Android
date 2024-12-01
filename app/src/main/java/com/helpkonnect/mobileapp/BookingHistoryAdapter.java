package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder> {

    private List<BookingHistoryModel> bookingList;

    public BookingHistoryAdapter(List<BookingHistoryModel> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookinghistory, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingHistoryModel booking = bookingList.get(position);
        holder.facilityNameTextView.setText(booking.getFacilityName());
        holder.amountTextView.setText("Amount: " + booking.getAmount());
        holder.bookingDateTextView.setText("Date: " + booking.getBookingDate());
        holder.sessionDurationTextView.setText("Duration: " + booking.getSessionDuration() + " hours");
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateData(List<BookingHistoryModel> newBookings) {
        this.bookingList = newBookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView facilityNameTextView, amountTextView, bookingDateTextView, sessionDurationTextView;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityNameTextView = itemView.findViewById(R.id.facilityNameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            bookingDateTextView = itemView.findViewById(R.id.bookingDateTextView);
            sessionDurationTextView = itemView.findViewById(R.id.sessionDurationTextView);
        }
    }
}
