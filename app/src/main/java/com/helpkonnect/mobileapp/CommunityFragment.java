package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<FeedItem> feedItems = new ArrayList<>();
    private FeedAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.communitypostrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FeedAdapter(feedItems);
        recyclerView.setAdapter(adapter);

        ImageView createPostImageView = rootView.findViewById(R.id.createpost);
        createPostImageView.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            startActivity(intent);
        });

        fetchEvents();
        fetchCommunityPosts();

        return rootView;
    }

    private void fetchEvents() {
        CollectionReference eventsRef = db.collection("events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                LocalDate currentDate = LocalDate.now();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    String description = document.getString("description");
                    String date = document.getString("date");
                    String timeStart = document.getString("timeStart");
                    String timeEnd = document.getString("timeEnd");
                    String venue = document.getString("venue");
                    String accommodation = document.getString("accommodation");
                    String facilityName = document.getString("facilityName");
                    String imageUrl = document.getString("imageUrl");

                    LocalDate eventDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    if (!eventDate.isBefore(currentDate)) {
                        feedItems.add(new FeedItem(new Event(name, description, date, timeStart, timeEnd, venue, accommodation, facilityName, imageUrl)));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("CommunityFragment", "Error getting events.", task.getException());
            }
        });
    }

    private void fetchCommunityPosts() {
        CollectionReference communityRef = db.collection("community");

        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String caption = document.getString("caption");
                    int heart = document.getLong("heart").intValue();
                    List<String> imageUrls = (List<String>) document.get("imageUrls");
                    if (imageUrls == null) {
                        imageUrls = new ArrayList<>();
                    }
                    String time = document.getTimestamp("time").toDate().toString();
                    String userId = document.getString("userId");
                    String userProfile = document.getString("userProfile");
                    String username = document.getString("username");
                    String postId = document.getId();

                    feedItems.add(new FeedItem(new CommunityListAdapter.CommunityPost(
                            userProfile,
                            username,
                            caption,
                            imageUrls,
                            heart,
                            time,
                            postId
                    )));
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("CommunityFragment", "Error getting documents.", task.getException());
            }
        });
    }
}
