package com.helpkonnect.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView responseTextView;
        private Button facilityButton;
        private String facilityName;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            responseTextView = itemView.findViewById(R.id.responseTextView);
            facilityButton = itemView.findViewById(R.id.facilityButton);
        }

        public void bind(Message message) {
            if (message.isUserMessage()) {
                messageTextView.setText(message.getMessage());
                messageTextView.setVisibility(View.VISIBLE);
                responseTextView.setVisibility(View.GONE);
                facilityButton.setVisibility(View.GONE);
            } else {
                responseTextView.setText(message.getMessage());
                responseTextView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);

                facilityName = getFacilityName(message.getMessage());
                if (facilityName != null) {
                    facilityButton.setText("Open " + facilityName);
                    facilityButton.setVisibility(View.VISIBLE);
                    facilityButton.setOnClickListener(v -> {
                        getFacilityDetails(facilityName, itemView.getContext());
                    });
                } else {
                    facilityButton.setVisibility(View.GONE);
                }
            }
        }

        public void getFacilityDetails(String facilityName, Context context) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("credentials")
                    .whereEqualTo("facilityName", facilityName)
                    .whereEqualTo("role", "facility")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                            String facilityImage = document.getString("imageUrl");
                            String facilityDescription = document.getString("facilityDescription");
                            String email = document.getString("email");
                            String facilityLocation = document.getString("facilityLocation");
                            String userId = document.getString("userId");

                            db.collection("feedback")
                                    .whereEqualTo("facilityId", userId)
                                    .get()
                                    .addOnSuccessListener(feedbackSnapshots -> {
                                        double averageRating = 0;
                                        List<Double> ratings = new ArrayList<>();
                                        for (DocumentSnapshot feedbackDoc : feedbackSnapshots) {
                                            Double rating = feedbackDoc.getDouble("rating");
                                            if (rating != null) {
                                                ratings.add(rating);
                                            }
                                        }
                                        if (!ratings.isEmpty()) {
                                            averageRating = ratings.stream()
                                                    .mapToDouble(Double::doubleValue)
                                                    .average()
                                                    .orElse(0);
                                        }

                                        Intent intent = new Intent(context, FacilityDetailsActivity.class);
                                        intent.putExtra("imageUrl", facilityImage);
                                        intent.putExtra("name", facilityName);
                                        intent.putExtra("location", facilityLocation);
                                        intent.putExtra("rating", (float) averageRating);
                                        intent.putExtra("userId", userId);
                                        intent.putExtra("description", facilityDescription);
                                        intent.putExtra("email", email);

                                        context.startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error fetching ratings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "Facility not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error fetching facility details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        public String getFacilityName(String message) {
            if (message.contains("Recommended Facility:")) {
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (line.startsWith("Recommended Facility:")) {
                        return line.replace("Recommended Facility:", "").trim();
                    }
                }
            }
            return null;
        }
    }
}
