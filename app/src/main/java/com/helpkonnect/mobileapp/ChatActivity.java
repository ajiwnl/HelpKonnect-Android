package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        currentUserId = currentUser.getUid();

        chatClient = ChatClient.instance();

        String selectedUserId = getIntent().getStringExtra("userId");

        String[] sortedUserIds = {currentUserId, selectedUserId};
        Arrays.sort(sortedUserIds);

        String channelId = String.format("messaging:%s_%s", sortedUserIds[0], sortedUserIds[1]);

        createOrGetChannel(sortedUserIds[0], sortedUserIds[1], channelId);


        if (savedInstanceState == null) {
            MessageListFragment fragment = MessageListFragment.newInstance( channelId , builder -> {
                builder.showHeader(true);
                return Unit.INSTANCE;
            });
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.channel_fragment_container, fragment)
                    .commit();
        }
    }

    private void createOrGetChannel(String currentUserId, String selectedUserId, String channelId) {
        List<String> members = new LinkedList<>();
        members.add(currentUserId);
        members.add(selectedUserId);

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Private Chat");

        chatClient.createChannel("messaging",  channelId, members, extraData)
                .enqueue(result -> {
                    if (result.isSuccess()) {
                        Log.d("ChatActivity", "Channel created or retrieved successfully.");
                    } else {
                        Log.e("ChatActivity", "Error creating/retrieving the channel: ");
                    }
                });
    }
}
