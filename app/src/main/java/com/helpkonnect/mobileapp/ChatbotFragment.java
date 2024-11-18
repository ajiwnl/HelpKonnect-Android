package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ChatbotFragment extends Fragment {

    private static final String TAG = "ChatbotFragment";
    private RecyclerView messageDisplayList;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageList;
    private EditText messageInput;
    private Button sendButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatbot, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        messageDisplayList = rootView.findViewById(R.id.messageDisplayList);
        messageDisplayList.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        messageDisplayList.setAdapter(messageAdapter);

        fetchMessages();

        messageInput = rootView.findViewById(R.id.messageInput);
        sendButton = rootView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });

        return rootView;
    }

    private void sendMessage(String message) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "unknownUser";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("message", message);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception: ", e);
            return;
        }

        long timestamp = System.currentTimeMillis();
        messageList.add(new Message(userId, message, true, timestamp));
        saveMessageToFirebase(userId, message, null); // Save only user message

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                "https://hk-recommender.vercel.app/recommend", jsonObject,
                response -> {
                    try {
                        String responseMessage = response.getString("response");
                        Log.d(TAG, "API Response: " + responseMessage);

                        // Only add the bot response if it's not null
                        if (responseMessage != null && !responseMessage.isEmpty()) {
                            messageList.add(new Message(userId, responseMessage, false, System.currentTimeMillis()));
                            saveMessageToFirebase(userId, null, responseMessage);
                        }

                        messageAdapter.notifyDataSetChanged();
                        messageDisplayList.scrollToPosition(messageList.size() - 1);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing API response: ", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error: ", error);
                });

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);
    }

    private void fetchMessages() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "unknownUser";

        messageList.clear();

        db.collection("messages")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String message = document.getString("message");
                            String response = document.getString("response");
                            long timestamp = document.getLong("timestamp");

                            if (message != null) {
                                messageList.add(new Message(userId, message, true, timestamp));
                            }

                            if (response != null) {
                                messageList.add(new Message(userId, response, false, timestamp));
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        messageDisplayList.scrollToPosition(messageList.size() - 1);
                    } else {
                        Log.e(TAG, "Error getting messages: ", task.getException());
                    }
                });
    }

    private void saveMessageToFirebase(String userId, String message, String response) {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("userId", userId);
        if (message != null) {
            messageData.put("message", message);
        }
        if (response != null) {
            messageData.put("response", response);
        }
        messageData.put("timestamp", timestamp);

        db.collection("messages").add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message saved successfully: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving message to Firestore: ", e);
                });
    }
}
