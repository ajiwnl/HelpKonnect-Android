package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectedPostFragment extends Fragment {

    private static final String ARG_POST = "post";

    private CommunityListAdapter.CommunityPost post;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration commentsListener;
    private RequestQueue requestQueue;
    private String filterKey;
    private String filterHost;
    private ImageView heartIcon;
    private TextView heartTextView;
    private Button commentButton;

    public static SelectedPostFragment newInstance(CommunityListAdapter.CommunityPost post) {
        SelectedPostFragment fragment = new SelectedPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST, post);
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
        mAuth = FirebaseAuth.getInstance();

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Fetch API keys from Vercel
        fetchApiKeys();
    }

    private void fetchApiKeys() {
        String url = "https://helpkonnect.vercel.app/api/filterKey";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        filterKey = jsonObject.getString("filterKey");
                        filterHost = jsonObject.getString("filterHost");
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Failed to parse API keys", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to fetch API keys", Toast.LENGTH_SHORT).show());

        requestQueue.add(stringRequest);
    }

    private void filterText(String text, String userId, FilterCallback callback) {
        String url = "https://community-purgomalum.p.rapidapi.com/json?text=" + Uri.encode(text);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean containsProfanity = response.getString("result").contains("***");
                        if (containsProfanity) {
                            saveFlaggedComment(text, userId);
                        }
                        callback.onResult(containsProfanity);
                    } catch (JSONException e) {
                        callback.onFailure(e);
                    }
                },
                error -> callback.onFailure(error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                if (filterKey != null && filterHost != null) {
                    headers.put("X-RapidAPI-Key", filterKey);
                    headers.put("X-RapidAPI-Host", filterHost);
                } else {
                    Toast.makeText(getContext(), "API keys not available", Toast.LENGTH_SHORT).show();
                }
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    interface FilterCallback {
        void onResult(boolean containsProfanity);
        void onFailure(Exception e);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectedpost, container, false);

        // Initialize UI elements
        heartIcon = rootView.findViewById(R.id.heartIcon);
        heartTextView = rootView.findViewById(R.id.userpostlikes);
        TextView userPostName = rootView.findViewById(R.id.userpostname);
        TextView userPostDescription = rootView.findViewById(R.id.userpostdescription);
        TextView userPostDate = rootView.findViewById(R.id.userpostdate);
        ImageView userProfileImage = rootView.findViewById(R.id.userprofileimage);
        LinearLayout imageContainer = rootView.findViewById(R.id.imageContainer);

        // Set data to UI elements
        if (post != null) {
            userPostName.setText(post.getUserPostName());
            userPostDescription.setText(post.getUserPostDescription());
            userPostDate.setText(post.getUserPostDate());

            // Load the current heart count from Firestore
            db.collection("community").document(post.getPostId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int likesCount = documentSnapshot.getLong("heart").intValue();
                        heartTextView.setText(String.valueOf(likesCount));
                    }
                })
                .addOnFailureListener(e -> Log.w("SelectedPostFragment", "Error loading heart count", e));

            Glide.with(this)
                .load(post.getUserProfileImageUrl())
                .circleCrop()
                .into(userProfileImage);

            // Load images into the container
            List<String> imageUrls = post.getImageUrls();
            if (imageUrls != null) {
                imageContainer.removeAllViews();
                for (String imageUrl : imageUrls) {
                    if (!imageUrl.isEmpty()) {
                        ImageView imageView = new ImageView(getContext());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                600
                        );
                        layoutParams.setMargins(8, 8, 8, 8);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        Glide.with(this)
                            .load(imageUrl)
                            .into(imageView);

                        imageContainer.addView(imageView);
                    }
                }
            }
        }

        // Initialize commentsContainer
        LinearLayout commentsContainer = rootView.findViewById(R.id.commentsContainer);
        checkIfUserLikedPost();

        heartIcon.setOnClickListener(v -> toggleHeartReaction());

        EditText postComment = rootView.findViewById(R.id.commentText);
        commentButton = rootView.findViewById(R.id.commentButton);
        commentButton.setOnClickListener(v -> {
            String commentText = postComment.getText().toString();
            Log.d("SelectedPostFragment", "Comment Button Clicked: " + commentText);
            postComment(commentText);
        });

        loadComments(commentsContainer);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }

    private void postComment(String comment) {
        String userId = mAuth.getCurrentUser().getUid();

        if (comment.isEmpty()) {
            Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        filterText(comment, userId, new FilterCallback() {
            @Override
            public void onResult(boolean containsProfanity) {
                if (containsProfanity) {
                    Toast.makeText(getContext(), "Comment contains inappropriate content.", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("comment", comment);
                    commentData.put("postId", post.getPostId());
                    commentData.put("time", new java.util.Date());
                    commentData.put("userId", userId);

                    db.collection("comments").add(commentData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                                EditText postComment = getView().findViewById(R.id.commentText);
                                postComment.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to filter text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void loadComments(LinearLayout commentsContainer) {
        commentsListener = db.collection("comments")
                .whereEqualTo("postId", post.getPostId())
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("SelectedPostFragment", "Error listening for comments: ", e);
                        return;
                    }

                    commentsContainer.removeAllViews(); // Clear existing comments

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String commentText = document.getString("comment");
                        String userId = document.getString("userId");

                        db.collection("credentials").document(userId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String username;
                                    if (userDoc.getString("facilityName") != null) {
                                        username = userDoc.getString("facilityName");
                                    } else {
                                        username = userDoc.getString("username");
                                    }

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
                                .addOnFailureListener(ex -> Log.e("SelectedPostFragment", "Error fetching user details", ex));
                    }
                });
    }

    private void saveFlaggedComment(String comment, String userId) {
        Map<String, Object> flaggedCommentData = new HashMap<>();
        flaggedCommentData.put("comment", comment);
        flaggedCommentData.put("time", new Date());
        flaggedCommentData.put("userId", userId);

        db.collection("flaggedAccounts").add(flaggedCommentData)
            .addOnSuccessListener(documentReference -> {
                Log.d("SelectedPostFragment", "Flagged comment saved for moderation.");
            })
            .addOnFailureListener(e -> {
                Log.e("SelectedPostFragment", "Failed to save flagged comment: " + e.getMessage());
            });
    }

    private void checkIfUserLikedPost() {
        String userId = mAuth.getCurrentUser().getUid();
        String postId = post.getPostId();
        CollectionReference likesRef = db.collection("likes");

        likesRef.whereEqualTo("postId", postId).whereEqualTo("userId", userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    heartIcon.setImageResource(R.drawable.hearticonfilled); // User has liked the post
                } else {
                    heartIcon.setImageResource(R.drawable.hearticon); // User has not liked the post
                }
            });
    }

    private void toggleHeartReaction() {
        String userId = mAuth.getCurrentUser().getUid();
        String postId = post.getPostId();
        CollectionReference likesRef = db.collection("likes");
        CollectionReference communityRef = db.collection("community"); // Assuming your posts are stored in a collection named "community"

        likesRef.whereEqualTo("postId", postId).whereEqualTo("userId", userId).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        // User has already liked the post, so remove the like
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            likesRef.document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    heartIcon.setImageResource(R.drawable.hearticon); // Update to unliked icon
                                    int likesCount = Integer.parseInt(heartTextView.getText().toString()) - 1;
                                    heartTextView.setText(String.valueOf(likesCount));

                                    // Update the heart count in the community document
                                    communityRef.document(postId).update("heart", likesCount)
                                        .addOnFailureListener(e -> Log.w("SelectedPostFragment", "Error updating heart count", e));
                                })
                                .addOnFailureListener(e -> Log.w("SelectedPostFragment", "Error removing like", e));
                        }
                    } else {
                        // User has not liked the post, so add a like
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("postId", postId);
                        likeData.put("userId", userId);

                        likesRef.add(likeData)
                            .addOnSuccessListener(documentReference -> {
                                heartIcon.setImageResource(R.drawable.hearticonfilled); // Update to liked icon
                                int likesCount = Integer.parseInt(heartTextView.getText().toString()) + 1;
                                heartTextView.setText(String.valueOf(likesCount));

                                // Update the heart count in the community document
                                communityRef.document(postId).update("heart", likesCount)
                                    .addOnFailureListener(e -> Log.w("SelectedPostFragment", "Error updating heart count", e));
                            })
                            .addOnFailureListener(e -> Log.w("SelectedPostFragment", "Error adding like", e));
                    }
                } else {
                    Log.w("SelectedPostFragment", "Error checking likes", task.getException());
                }
            });
    }

}