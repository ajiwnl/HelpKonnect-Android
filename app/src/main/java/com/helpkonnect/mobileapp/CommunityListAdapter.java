package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.List;

public class CommunityListAdapter extends RecyclerView.Adapter<CommunityListAdapter.CommunityViewHolder> {

    private List<CommunityPost> posts;
    private OnItemClickListener onItemClick;

    public static class CommunityPost implements Serializable {
        private String userProfileImageUrl;
        private String userPostName;
        private String userPostDescription;
        private List<String> imageUrls;
        private int userPostLikes;
        private String userPostDate;
        private String postComment;

        private String postId;

        public CommunityPost(String userProfileImageUrl, String userPostName, String userPostDescription,
                             List<String> imageUrls, int userPostLikes, String userPostDate, String postComment, String postId) {
            this.userProfileImageUrl = userProfileImageUrl;
            this.userPostName = userPostName;
            this.userPostDescription = userPostDescription;
            this.imageUrls = imageUrls;
            this.userPostLikes = userPostLikes;
            this.userPostDate = userPostDate;
            this.postComment = postComment;
            this.postId = postId;
        }

        public String getUserProfileImageUrl() { return userProfileImageUrl; }
        public String getUserPostName() { return userPostName; }
        public String getUserPostDescription() { return userPostDescription; }
        // Getters
        public List<String> getImageUrls() { return imageUrls; }
        public int getUserPostLikes() { return userPostLikes; }
        public String getUserPostDate() { return userPostDate; }
        public String getPostComment() { return postComment; }

        public String getPostId() {return postId;}
    }

    public interface OnItemClickListener {
        void onItemClick(CommunityPost post);
    }


    public CommunityListAdapter(List<CommunityPost> posts, OnItemClickListener onItemClick) {
        this.posts = posts;
        this.onItemClick = onItemClick;
    }

    public class CommunityViewHolder extends RecyclerView.ViewHolder {
        public ImageView userProfileImage;
        public TextView userPostName;
        public TextView userPostDescription;
        public LinearLayout imageContainer;
        public TextView userPostLikes;
        public TextView userPostDate;
        public TextView postComment;

        public CommunityViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userprofileimage);
            userPostName = itemView.findViewById(R.id.userpostname);
            userPostDescription = itemView.findViewById(R.id.userpostdescription);
            imageContainer = itemView.findViewById(R.id.imageContainer);
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

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_communitypost, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommunityViewHolder holder, int position) {
        CommunityPost post = posts.get(position);

        holder.userPostName.setText(post.getUserPostName());
        holder.userPostDescription.setText(post.getUserPostDescription());
        holder.userPostLikes.setText(String.valueOf(post.getUserPostLikes()));
        holder.userPostDate.setText(post.getUserPostDate());
        holder.postComment.setText(post.getPostComment());

        Glide.with(holder.itemView.getContext())
                .load(post.getUserProfileImageUrl())
                .circleCrop()
                .into(holder.userProfileImage);

        holder.imageContainer.removeAllViews();

        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null) {
            holder.imageContainer.removeAllViews();

            for (String imageUrl : imageUrls) {
                if (!imageUrl.isEmpty()) {
                    ImageView imageView = new ImageView(holder.itemView.getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            holder.itemView.getContext().getResources().getDisplayMetrics().widthPixels,
                            400
                    );
                    layoutParams.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Glide.with(holder.itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.userprofileicon)
                            .into(imageView);

                    holder.imageContainer.addView(imageView);
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }
}
