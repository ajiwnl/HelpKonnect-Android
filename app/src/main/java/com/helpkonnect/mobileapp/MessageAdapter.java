package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;
        private TextView responseTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            responseTextView = itemView.findViewById(R.id.responseTextView);
        }

        public void bind(Message message) {
            if (message.isUserMessage()) {
                messageTextView.setText(message.getMessage());
                messageTextView.setVisibility(View.VISIBLE);
                responseTextView.setVisibility(View.GONE);
            } else {
                responseTextView.setText(message.getMessage());
                responseTextView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);
            }
        }
    }
}
