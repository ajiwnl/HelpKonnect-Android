package com.helpkonnect.testpush;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommunicationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_communication, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.communitypostrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create list of posts
        List<CommunityListAdapter.CommunityPost> posts = new ArrayList<>();
        posts.add(new CommunityListAdapter.CommunityPost(
                R.drawable.edittextusericon,
                "John Doe",
                "This is the first post hello world",
                R.drawable.editusericon,
                27,
                "2 hrs ago",
                "256 Comments"
        ));
        posts.add(new CommunityListAdapter.CommunityPost(
                R.drawable.edittextusericon,
                "Micheal Johnson",
                "This is the second post hello world",
                R.drawable.editusericon,
                25,
                "3 hrs ago",
                "20 Comments"
        ));
        posts.add(new CommunityListAdapter.CommunityPost(
                R.drawable.edittextusericon,
                "Mark Denver",
                "This is the third post hello world",
                R.drawable.editusericon,
                5,
                "4 hrs ago",
                "22 Comments"
        ));

        // Set adapter with click listener
        CommunityListAdapter adapter = new CommunityListAdapter(posts, post ->
                Toast.makeText(requireContext(), "Clicked on " + post.getUserPostName(), Toast.LENGTH_SHORT).show()
        );

        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
