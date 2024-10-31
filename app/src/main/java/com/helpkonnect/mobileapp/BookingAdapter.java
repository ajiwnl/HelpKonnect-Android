package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<BookingModel> bookingList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(BookingModel booking);
    }

    public BookingAdapter(List<BookingModel> bookingList, OnItemClickListener onItemClickListener) {
        this.bookingList = bookingList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);
        holder.bookingTitle.setText(booking.getBookingTitle());
        holder.bookingDate.setText(booking.getBookingDate());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView bookingTitle;
        TextView bookingDate;

        public BookingViewHolder(View itemView) {
            super(itemView);
            bookingTitle = itemView.findViewById(R.id.BookingTitleTextView);
            bookingDate = itemView.findViewById(R.id.BookingDateTextView);
        }
    }
}


