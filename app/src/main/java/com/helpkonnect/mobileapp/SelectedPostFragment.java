package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.List;

import android.util.Log;

public class SelectedPostFragment extends Fragment {

    private static final String ARG_POST = "post";

    private CommunityListAdapter.CommunityPost post;
    private FirebaseFirestore db;

    public static SelectedPostFragment newInstance(CommunityListAdapter.CommunityPost post) {
        SelectedPostFragment fragment = new SelectedPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, (Serializable) post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = (CommunityListAdapter.CommunityPost) getArguments().getSerializable(ARG_POST);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectedpost, container, false);

        ImageView userProfileImageView = rootView.findViewById(R.id.userprofileimage);
        TextView usernameTextView = rootView.findViewById(R.id.userpostname);
        TextView captionTextView = rootView.findViewById(R.id.userpostdescription);
        TextView heartTextView = rootView.findViewById(R.id.userpostlikes);
        TextView timeTextView = rootView.findViewById(R.id.userpostdate);
        LinearLayout imageContainer = rootView.findViewById(R.id.imageContainer);
        LinearLayout commentsContainer = rootView.findViewById(R.id.commentsContainer);

        Glide.with(this)
            .load(post.getUserProfileImageUrl())
            .circleCrop()
            .into(userProfileImageView);
        usernameTextView.setText(post.getUserPostName());
        captionTextView.setText(post.getUserPostDescription());
        heartTextView.setText(String.valueOf(post.getUserPostLikes()));
        timeTextView.setText(post.getUserPostDate());

        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                if (!imageUrl.isEmpty()) {
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            getResources().getDisplayMetrics().widthPixels,
                            600
                    );
                    layoutParams.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.userprofileicon)
                            .into(imageView);

                    imageContainer.addView(imageView);
                }
            }
        }

        loadComments(commentsContainer);

        return rootView;
    }

    private void loadComments(LinearLayout commentsContainer) {
        db.collection("comments")
                .whereEqualTo("postId", post.getPostId())
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String commentText = document.getString("comment");
                            String userId = document.getString("userId");

                            db.collection("credentials").document(userId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        String username = userDoc.getString("facilityName");
                                        String imageUrl = userDoc.getString("imageUrl");

                                        View commentView = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, commentsContainer, false);
                                        TextView commentTextView = commentView.findViewById(R.id.commentText);
                                        TextView usernameTextView = commentView.findViewById(R.id.username);
                                        ImageView userImageView = commentView.findViewById(R.id.userImage);

                                        commentTextView.setText(commentText);
                                        usernameTextView.setText(username);
                                        Glide.with(getContext())
                                            .load(imageUrl)
                                            .circleCrop()
                                            .into(userImageView);

                                        commentsContainer.addView(commentView);
                                    })
                                    .addOnFailureListener(e -> Log.e("SelectedPostFragment", "Error fetching user details", e));
                        }
                    } else {
                        Log.e("SelectedPostFragment", "Error getting comments: ", task.getException());
                    }
                });
    }
}
