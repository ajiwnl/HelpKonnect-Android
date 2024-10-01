package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<CommunityListAdapter.CommunityPost> posts = new ArrayList<>();
    private CommunityListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.communitypostrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CommunityListAdapter(posts, post ->
                Toast.makeText(requireContext(), "Clicked on " + post.getUserPostName(), Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(adapter);

        fetchCommunityPosts();

        return rootView;
    }

    private void fetchCommunityPosts() {
        CollectionReference communityRef = db.collection("community");

        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                posts.clear(); // Clear the list to prevent duplication
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String caption = document.getString("caption");
                    int heart = document.getLong("heart").intValue();

                    // Handle imageUrls properly to avoid null pointer exceptions
                    List<String> imageUrls = (List<String>) document.get("imageUrls");
                    if (imageUrls == null) {
                        imageUrls = new ArrayList<>(); // Initialize as empty list if it's null
                    }

                    String time = document.getTimestamp("time").toDate().toString(); // Convert to readable format
                    String userId = document.getString("userId");
                    String userProfile = document.getString("userProfile");
                    String username = document.getString("username");

                    // Add the post to the list
                    posts.add(new CommunityListAdapter.CommunityPost(
                            userProfile, // URL of the user profile image
                            username,   // Username
                            caption,    // Post caption
                            imageUrls,  // List of image URLs
                            heart,      // Number of likes (hearts)
                            time,       // Timestamp of the post
                            "256 Comments" // Placeholder for comments (you can replace this with actual comments if needed)
                    ));
                }
                adapter.notifyDataSetChanged(); // Notify the adapter of data changes
            } else {
                Log.w("CommunityFragment", "Error getting documents.", task.getException());
            }
        });
    }
}
