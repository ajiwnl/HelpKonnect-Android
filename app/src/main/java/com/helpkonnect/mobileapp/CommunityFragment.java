package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;

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

        adapter = new CommunityListAdapter(posts, post -> {
            SelectedPostFragment selectedPostFragment = SelectedPostFragment.newInstance(post);

            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedPostFragment)
                .addToBackStack(null)
                .commit();
        });
        recyclerView.setAdapter(adapter);

        // Set OnClickListener for the create post ImageView
        ImageView createPostImageView = rootView.findViewById(R.id.createpost);
        createPostImageView.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            startActivity(intent);
        });

        fetchCommunityPosts();

        return rootView;
    }

    private void fetchCommunityPosts() {
        CollectionReference communityRef = db.collection("community");

        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                posts.clear();
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

                    posts.add(new CommunityListAdapter.CommunityPost(
                            userProfile,
                            username,
                            caption,
                            imageUrls,
                            heart,
                            time,
                            "256 Comments",
                            postId
                    ));
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("CommunityFragment", "Error getting documents.", task.getException());
            }
        });
    }
}
