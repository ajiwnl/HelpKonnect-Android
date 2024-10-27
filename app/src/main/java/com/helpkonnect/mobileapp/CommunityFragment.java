package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.SearchView;

import com.google.firebase.Timestamp; // Import this for Firestore timestamp handling
import java.util.Calendar; // Import this for date handling

public class CommunityFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<CommunityListAdapter.CommunityPost> posts = new ArrayList<>();
    private List<CommunityListAdapter.CommunityPost> allPosts = new ArrayList<>();
    private CommunityListAdapter adapter;
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> events;

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

        ImageView createPostImageView = rootView.findViewById(R.id.createpost);
        createPostImageView.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            startActivity(intent);
        });

        // Add SearchView functionality
        SearchView searchView = rootView.findViewById(R.id.searchCaption);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return true;
            }
        });

        eventsRecyclerView = rootView.findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        events = new ArrayList<>();
        eventAdapter = new EventAdapter(events);
        eventsRecyclerView.setAdapter(eventAdapter);

        fetchCommunityPosts();

        fetchEvents();

        return rootView;
    }

    private void fetchCommunityPosts() {
        CollectionReference communityRef = db.collection("community");

        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allPosts.clear();
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

                    CommunityListAdapter.CommunityPost post = new CommunityListAdapter.CommunityPost(
                            userProfile,
                            username,
                            caption,
                            imageUrls,
                            heart,
                            time,
                            postId
                    );
                    allPosts.add(post);
                    posts.add(post);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("CommunityFragment", "Error getting documents.", task.getException());
            }
        });
    }

    private void filterPosts(String query) {
        posts.clear();
        if (query.isEmpty()) {
            posts.addAll(allPosts);
        } else {
            for (CommunityListAdapter.CommunityPost post : allPosts) {
                if (post.getUserPostDescription().toLowerCase().contains(query.toLowerCase())) {
                    posts.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchEvents() {
        CollectionReference eventsRef = db.collection("events");

        // Get the current date
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date currentDate = calendar.getTime();

        Log.d("CommunityFragment", "Current Date: " + currentDate);

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                events.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("CommunityFragment", "Fetched Document ID: " + document.getId());
                    Log.d("CommunityFragment", "Fetched Data: " + document.getData());

                    String name = document.getString("name");
                    String description = document.getString("description");
                    String dateString = document.getString("date");
                    String timeStart = document.getString("timeStart");
                    String timeEnd = document.getString("timeEnd");
                    String venue = document.getString("venue");
                    String facilityName = document.getString("facilityName");
                    String imageUrl = document.getString("imageUrl");

                    Long accommodationCountLong = document.getLong("accommodationCount");
                    int accommodationCount = (accommodationCountLong != null) ? accommodationCountLong.intValue() : 0;

                    boolean done = document.getBoolean("done") != null && document.getBoolean("done");

                    // Parse the string date to a Date object
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date eventDate = null;
                    try {
                        eventDate = sdf.parse(dateString); // Parse the string date
                    } catch (ParseException e) {
                        Log.e("CommunityFragment", "Error parsing date: " + dateString, e);
                    }

                    if (eventDate != null && !eventDate.before(currentDate)) {
                        Event event = new Event(name, description, dateString, timeStart, timeEnd, venue, facilityName, imageUrl, accommodationCount, done);
                        events.add(event);
                    }
                }
                Log.d("CommunityFragment", "Total Events Fetched: " + events.size());
                eventAdapter.notifyDataSetChanged();
            } else {
                Log.w("CommunityFragment", "Error getting documents.", task.getException());
            }
        });
    }
}
