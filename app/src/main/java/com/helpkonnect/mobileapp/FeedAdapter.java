package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FeedItem> feedItems;

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public ImageView eventImage;
        public TextView eventName;
        public TextView eventDescription;
        public TextView eventDate;
        public TextView eventTime;
        public TextView eventVenue;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.eventName);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            eventVenue = itemView.findViewById(R.id.eventVenue);
        }
    }

    public static class CommunityPostViewHolder extends RecyclerView.ViewHolder {
        public ImageView userProfileImage;
        public TextView userPostName;
        public TextView userPostDescription;
        public TextView userPostDate;

        public CommunityPostViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userprofileimage);
            userPostName = itemView.findViewById(R.id.userpostname);
            userPostDescription = itemView.findViewById(R.id.userpostdescription);
            userPostDate = itemView.findViewById(R.id.userpostdate);
        }
    }

    public FeedAdapter(List<FeedItem> feedItems) {
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == FeedItem.TYPE_EVENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_event, parent, false);
            return new EventViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_communitypost, parent, false);
            return new CommunityPostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedItem feedItem = feedItems.get(position);
        if (feedItem.getType() == FeedItem.TYPE_EVENT) {
            Event event = feedItem.getEvent();
            EventViewHolder eventHolder = (EventViewHolder) holder;
            eventHolder.eventName.setText(event.getName());
            eventHolder.eventDescription.setText(event.getDescription());
            eventHolder.eventDate.setText(event.getDate());
            eventHolder.eventTime.setText(event.getTimeStart() + " - " + event.getTimeEnd());
            eventHolder.eventVenue.setText(event.getVenue());
            Glide.with(eventHolder.itemView.getContext())
                    .load(event.getImageUrl())
                    .into(eventHolder.eventImage);
        } else {
            CommunityListAdapter.CommunityPost communityPost = feedItem.getCommunityPost();
            CommunityPostViewHolder communityHolder = (CommunityPostViewHolder) holder;
            communityHolder.userPostName.setText(communityPost.getUserPostName());
            communityHolder.userPostDescription.setText(communityPost.getUserPostDescription());
            communityHolder.userPostDate.setText(communityPost.getUserPostDate());
            Glide.with(communityHolder.itemView.getContext())
                    .load(communityPost.getUserProfileImageUrl())
                    .into(communityHolder.userProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return feedItems.get(position).getType();
    }
}