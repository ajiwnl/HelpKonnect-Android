package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Make sure to add Glide dependency for image loading

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Event> events;
    private String currentUserId;
    private FirebaseAuth mAuth;


    public EventAdapter(List<Event> events) {
        this.events = events;
        this.mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            this.currentUserId = currentUser.getUid();
        }
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_event, parent, false);
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

        Glide.with(holder.itemView.getContext())
                .load(event.getImageUrl())
                .placeholder(R.drawable.default_icon)
                .into(holder.eventImage);

        checkParticipation(holder.participateBtn, event);

        holder.participateBtn.setOnClickListener(view -> {
            toggleParticipation(holder.participateBtn, event);
        });
    }

    private void checkParticipation(Button participateBtn, Event event) {
        CollectionReference participatedRef = db.collection("participated");

        participatedRef
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("eventName", event.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        participateBtn.setText("Participated");
                        participateBtn.setBackgroundColor(participateBtn.getContext().getResources().getColor(R.color.participatedGray));
                    } else {
                        participateBtn.setText("Participate");
                        participateBtn.setBackgroundColor(participateBtn.getContext().getResources().getColor(R.color.participateDefault));
                    }
                });
    }


    private void toggleParticipation(Button participateBtn, Event event) {
        CollectionReference participatedRef = db.collection("participated");

        participatedRef
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("eventName", event.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> participationData = new HashMap<>();
                        participationData.put("userId", currentUserId);
                        participationData.put("facilityName", event.getFacilityName());
                        participationData.put("eventName", event.getName());

                        participatedRef.add(participationData)
                                .addOnSuccessListener(documentReference -> {
                                    participateBtn.setText("Participated");
                                })
                                .addOnFailureListener(e -> {
                                });
                    } else {
                        // User has already participated; remove participation
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        participatedRef.document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    participateBtn.setText("Participate");
                                })
                                .addOnFailureListener(e -> {
                                });
                    }
                });
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
        ImageView eventImage;
        Button participateBtn;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            nameTextView = itemView.findViewById(R.id.eventName);
            descriptionTextView = itemView.findViewById(R.id.eventDescription);
            dateTextView = itemView.findViewById(R.id.eventDate);
            timeTextView = itemView.findViewById(R.id.eventTime);
            venueTextView = itemView.findViewById(R.id.eventVenue);
            participateBtn = itemView.findViewById(R.id.participateBtn);
        }
    }
}