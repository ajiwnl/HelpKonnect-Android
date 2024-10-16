package com.helpkonnect.mobileapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityListAdapter extends RecyclerView.Adapter<CommunityListAdapter.CommunityViewHolder> {

    private List<CommunityPost> posts;
    private OnItemClickListener onItemClick;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public static class CommunityPost implements Serializable {
        private String userProfileImageUrl;
        private String userPostName;
        private String userPostDescription;
        private List<String> imageUrls;
        private int userPostLikes;
        private String userPostDate;

        private String postId;

        public CommunityPost(String userProfileImageUrl, String userPostName, String userPostDescription,
                             List<String> imageUrls, int userPostLikes, String userPostDate, String postId) {
            this.userProfileImageUrl = userProfileImageUrl;
            this.userPostName = userPostName;
            this.userPostDescription = userPostDescription;
            this.imageUrls = imageUrls;
            this.userPostLikes = userPostLikes;
            this.userPostDate = userPostDate;
            this.postId = postId;
        }

        public String getUserProfileImageUrl() { return userProfileImageUrl; }
        public String getUserPostName() { return userPostName; }
        public String getUserPostDescription() { return userPostDescription; }
        // Getters
        public List<String> getImageUrls() { return imageUrls; }
        public int getUserPostLikes() { return userPostLikes; }
        public String getUserPostDate() { return userPostDate; }

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
        public ImageView heartIcon; // Add this line

        public CommunityViewHolder(View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userprofileimage);
            userPostName = itemView.findViewById(R.id.userpostname);
            userPostDescription = itemView.findViewById(R.id.userpostdescription);
            imageContainer = itemView.findViewById(R.id.imageContainer);
            userPostLikes = itemView.findViewById(R.id.userpostlikes);
            userPostDate = itemView.findViewById(R.id.userpostdate);
            postComment = itemView.findViewById(R.id.postComment);
            heartIcon = itemView.findViewById(R.id.heartIcon); // Add this line

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick.onItemClick(posts.get(position));
                }
            });

            heartIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onHeartClick(posts.get(position), heartIcon, userPostLikes);
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
        holder.userPostDate.setText(post.getUserPostDate());

        // Load the current heart count from Firestore
        db.collection("community").document(post.getPostId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int likesCount = documentSnapshot.getLong("heart").intValue();
                    holder.userPostLikes.setText(String.valueOf(likesCount));
                }
            })
            .addOnFailureListener(e -> Log.w("CommunityFragment", "Error loading heart count", e));

        // Check if the current user has liked the post
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("likes")
            .whereEqualTo("postId", post.getPostId())
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    holder.heartIcon.setImageResource(R.drawable.hearticonfilled); // Liked icon
                } else {
                    holder.heartIcon.setImageResource(R.drawable.hearticon); // Unliked icon
                }
            });

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

    private void onHeartClick(CommunityListAdapter.CommunityPost post, ImageView heartIcon, TextView userPostLikes) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String postId = post.getPostId();
        CollectionReference likesRef = db.collection("likes");
        CollectionReference communityRef = db.collection("community"); // Ensure this is the correct collection

        likesRef.whereEqualTo("postId", postId).whereEqualTo("userId", userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // User has already liked the post, so remove the like
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                likesRef.document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            heartIcon.setImageResource(R.drawable.hearticon); // Update to unliked icon
                                            int likesCount = Integer.parseInt(userPostLikes.getText().toString()) - 1;
                                            userPostLikes.setText(String.valueOf(likesCount));

                                            // Update the heart count in the community document
                                            communityRef.document(postId).update("heart", likesCount)
                                                    .addOnFailureListener(e -> Log.w("CommunityFragment", "Error updating heart count", e));
                                        })
                                        .addOnFailureListener(e -> Log.w("CommunityFragment", "Error removing like", e));
                            }
                        } else {
                            // User has not liked the post, so add a like
                            Map<String, Object> likeData = new HashMap<>();
                            likeData.put("postId", postId);
                            likeData.put("userId", userId);

                            likesRef.add(likeData)
                                    .addOnSuccessListener(documentReference -> {
                                        heartIcon.setImageResource(R.drawable.hearticonfilled); // Update to liked icon
                                        int likesCount = Integer.parseInt(userPostLikes.getText().toString()) + 1;
                                        userPostLikes.setText(String.valueOf(likesCount));

                                        // Update the heart count in the community document
                                        communityRef.document(postId).update("heart", likesCount)
                                                .addOnFailureListener(e -> Log.w("CommunityFragment", "Error updating heart count", e));
                                    })
                                    .addOnFailureListener(e -> Log.w("CommunityFragment", "Error adding like", e));
                        }
                    } else {
                        Log.w("CommunityFragment", "Error checking likes", task.getException());
                    }
                });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }
}
