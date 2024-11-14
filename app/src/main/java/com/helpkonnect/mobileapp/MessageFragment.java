package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;

public class MessageFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserList> userList;
    private List<UserList> filteredUserList;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressBar loaderMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messaging, container, false);

        loaderMessage = rootView.findViewById(R.id.LoadUserMessages);
        recyclerView = rootView.findViewById(R.id.user_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
        userAdapter = new UserAdapter(filteredUserList, user -> {
            openChatWithUser(user);
        });
        recyclerView.setAdapter(userAdapter);

        // Set up the search view
        SearchView searchUser = rootView.findViewById(R.id.searchUser);
        searchUser.setOnClickListener( v -> {
            searchUser.setIconified(false);
            searchUser.requestFocus();
        });
        searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        fetchUsersFromFirebase();

        return rootView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchUsersFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loaderMessage.setVisibility(View.VISIBLE);
        db.collection("credentials")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getString("userId");
                        String username = document.getString("username");
                        String facilityName = document.getString("facilityName");
                        String imageUrl = document.getString("imageUrl");

                        if (!userId.equals(getCurrentUserId())) {
                            String displayName = username != null ? username : facilityName;
                            UserList user = new UserList(userId, displayName, imageUrl);
                            userList.add(user);
                            filteredUserList.add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                } else {
                    Log.w("MessageFragment", "Error getting documents.", task.getException());
                }
                loaderMessage.setVisibility(View.GONE);
            });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserList user : userList) {
                if (user.getDisplayName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredUserList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private String getCurrentUserId() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        assert currentUser != null;
        return currentUser.getUid();
    }

    private void openChatWithUser(UserList user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("userId", user.getUserId());
        startActivity(intent);
    }
}
