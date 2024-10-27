package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Make sure to add Glide dependency for image loading
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.nameTextView.setText(event.getName());
        holder.descriptionTextView.setText(event.getDescription());
        holder.dateTextView.setText(event.getDate());
        holder.timeTextView.setText(event.getTimeStart() + " - " + event.getTimeEnd());
        holder.venueTextView.setText(event.getVenue());

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(event.getImageUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView venueTextView;
        ImageView imageView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.eventNameTextView);
            descriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
            dateTextView = itemView.findViewById(R.id.eventDateTextView);
            timeTextView = itemView.findViewById(R.id.eventTimeTextView);
            venueTextView = itemView.findViewById(R.id.eventVenueTextView);
            imageView = itemView.findViewById(R.id.eventImageView);
        }
    }
}