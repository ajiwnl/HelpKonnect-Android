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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
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

    private void filterText(String text, FilterCallback callback) {
        String url = "https://community-purgomalum.p.rapidapi.com/json?text=" + Uri.encode(text);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean containsProfanity = response.getString("result").contains("***");
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

        ImageView userProfileImageView = rootView.findViewById(R.id.userprofileimage);
        TextView usernameTextView = rootView.findViewById(R.id.userpostname);
        TextView captionTextView = rootView.findViewById(R.id.userpostdescription);
        TextView heartTextView = rootView.findViewById(R.id.userpostlikes);
        TextView timeTextView = rootView.findViewById(R.id.userpostdate);
        LinearLayout imageContainer = rootView.findViewById(R.id.imageContainer);
        LinearLayout commentsContainer = rootView.findViewById(R.id.commentsContainer);

        EditText commentText = rootView.findViewById(R.id.commentText);
        Button commentButton = rootView.findViewById(R.id.commentButton);

        commentButton.setOnClickListener(v -> {
            String comment = commentText.getText().toString().trim();
            if (!comment.isEmpty()) {
                postComment(comment);
                commentText.setText(""); // Clear the input field
            } else {
                Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }

    private void postComment(String comment) {
        filterText(comment, new FilterCallback() {
            @Override
            public void onResult(boolean containsProfanity) {
                if (containsProfanity) {
                    Toast.makeText(getContext(), "Comment contains inappropriate content.", Toast.LENGTH_SHORT).show();
                } else {
                    String userId = mAuth.getCurrentUser().getUid();
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("comment", comment);
                    commentData.put("postId", post.getPostId());
                    commentData.put("time", new java.util.Date());
                    commentData.put("userId", userId);

                    db.collection("comments").add(commentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh comments display here
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

}
