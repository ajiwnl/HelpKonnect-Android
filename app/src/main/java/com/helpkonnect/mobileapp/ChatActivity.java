package com.helpkonnect.mobileapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.ui.feature.messages.MessageListFragment;
import kotlin.Unit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ChatClient chatClient;
    private String currentUserId;

    private TextView usernameTextView;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserId = currentUser.getUid();
        chatClient = ChatClient.instance();

        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            finish();
        });

        db = FirebaseFirestore.getInstance();

        usernameTextView = findViewById(R.id.usernameTextView);
        profileImageView = findViewById(R.id.profileImageView);

        String selectedUserId = getIntent().getStringExtra("userId");
        fetchUserDetails(selectedUserId);

        String[] sortedUserIds = {currentUserId, selectedUserId};
        Arrays.sort(sortedUserIds);

        String channelId = String.format("messaging:%s_%s", sortedUserIds[0], sortedUserIds[1]);

        createOrGetChannel(sortedUserIds[0], sortedUserIds[1], channelId);

        if (savedInstanceState == null) {
            MessageListFragment fragment = MessageListFragment.newInstance(channelId, builder -> {
                builder.showHeader(false);
                return Unit.INSTANCE;
            });
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.channel_fragment_container, fragment)
                    .commit();
        }
    }

    @SuppressLint("SetTextI18n")
    private void fetchUserDetails(String selectedUserId) {
        db.collection("credentials").document(selectedUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            String facilityName = document.getString("facilityName");
                            String imageUrl = document.getString("imageUrl");

                            if (username != null && !username.isEmpty()) {
                                usernameTextView.setText(username);
                            } else if (facilityName != null && !facilityName.isEmpty()) {
                                usernameTextView.setText(facilityName);
                            } else {
                                usernameTextView.setText("No Name Available");
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .circleCrop()
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.userprofileicon);
                            }
                        } else {
                            Log.d("ChatActivity", "Document does not exist.");
                        }
                    } else {
                        Log.e("ChatActivity", "Error fetching user details: ", task.getException());
                    }
                });
    }


    private void createOrGetChannel(String currentUserId, String selectedUserId, String channelId) {
        List<String> members = new LinkedList<>();
        members.add(currentUserId);
        members.add(selectedUserId);

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Private Chat");

        chatClient.createChannel("messaging", channelId, members, extraData)
                .enqueue(result -> {
                    if (result.isSuccess()) {
                        Log.d("ChatActivity", "Channel created or retrieved successfully.");
                    } else {
                        Log.e("ChatActivity", "Error creating/retrieving the channel: ");
                    }
                });
    }
}
