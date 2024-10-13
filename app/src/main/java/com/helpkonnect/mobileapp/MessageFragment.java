package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.ProgressBar;

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
        userAdapter = new UserAdapter(userList, user -> {
            openChatWithUser(user);
        });
        recyclerView.setAdapter(userAdapter);

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
                            userList.add(new UserList(userId, displayName, imageUrl));
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                } else {
                    Log.w("MessageFragment", "Error getting documents.", task.getException());
                }
                loaderMessage.setVisibility(View.GONE);
            });
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
