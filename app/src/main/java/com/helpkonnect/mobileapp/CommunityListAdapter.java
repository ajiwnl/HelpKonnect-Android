package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommunityListAdapter extends RecyclerView.Adapter<CommunityListAdapter.CommunityViewHolder> {

    private List<CommunityPost> posts;
    private OnItemClickListener onItemClick;

    // Define the CommunityPost class
    public static class CommunityPost {
        private int userProfileImageResId;
        private String userPostName;
        private String userPostDescription;
        private int userPostImageResId;
        private int userPostLikes;
        private String userPostDate;
        private String postComment;

        // Constructor
        public CommunityPost(int userProfileImageResId, String userPostName, String userPostDescription,
                             int userPostImageResId, int userPostLikes, String userPostDate, String postComment) {
            this.userProfileImageResId = userProfileImageResId;
            this.userPostName = userPostName;
            this.userPostDescription = userPostDescription;
            this.userPostImageResId = userPostImageResId;
            this.userPostLikes = userPostLikes;
            this.userPostDate = userPostDate;
            this.postComment = postComment;
        }

        // Getters
        public int getUserProfileImageResId() { return userProfileImageResId; }
        public String getUserPostName() { return userPostName; }
        public String getUserPostDescription() { return userPostDescription; }
        public int getUserPostImageResId() { return userPostImageResId; }
        public int getUserPostLikes() { return userPostLikes; }
        public String getUserPostDate() { return userPostDate; }
        public String getPostComment() { return postComment; }
    }

    // Define the interface for click handling
    public interface OnItemClickListener {
        void onItemClick(CommunityPost post);
    }

    // Constructor
    public CommunityListAdapter(List<CommunityPost> posts, OnItemClickListener onItemClick) {
        this.posts = posts;
        this.onItemClick = onItemClick;
    }

    // CommunityViewHolder class
    public class CommunityViewHolder extends RecyclerView.ViewHolder {
        public ImageView userProfileImage;
        public TextView userPostName;
        public TextView userPostDescription;
        public ImageView userPostImage;
        public TextView userPostLikes;
        public TextView userPostDate;
        public TextView postComment;

        public CommunityViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userprofileimage);
            userPostName = itemView.findViewById(R.id.userpostname);
            userPostDescription = itemView.findViewById(R.id.userpostdescription);
            userPostImage = itemView.findViewById(R.id.userpostimage);
            userPostLikes = itemView.findViewById(R.id.userpostlikes);
            userPostDate = itemView.findViewById(R.id.userpostdate);
            postComment = itemView.findViewById(R.id.postComment);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick.onItemClick(posts.get(position));
                }
            });
        }
    }

    @Override
    public CommunityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_communitypost, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommunityViewHolder holder, int position) {
        CommunityPost post = posts.get(position);
        holder.userProfileImage.setImageResource(post.getUserProfileImageResId());
        holder.userPostName.setText(post.getUserPostName());
        holder.userPostDescription.setText(post.getUserPostDescription());
        holder.userPostImage.setImageResource(post.getUserPostImageResId());
        holder.userPostLikes.setText(String.valueOf(post.getUserPostLikes()));
        holder.userPostDate.setText(post.getUserPostDate());
        holder.postComment.setText(post.getPostComment());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
