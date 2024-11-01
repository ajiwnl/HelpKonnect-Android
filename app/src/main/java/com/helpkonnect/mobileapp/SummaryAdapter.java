/*
package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {
    private List<EmotionSummary> emotionSummaryList;

    public SummaryAdapter(List<EmotionSummary> emotionSummaryList) {
        this.emotionSummaryList = emotionSummaryList;
    }

    @Override
    public SummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emotion_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SummaryViewHolder holder, int position) {
        EmotionSummary summary = emotionSummaryList.get(position);
        holder.dayTextView.setText(summary.getDay());
        StringBuilder emotionText = new StringBuilder();
        for (Map.Entry<String, Float> entry : summary.getEmotions().entrySet()) {
            emotionText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        holder.emotionTextView.setText(emotionText.toString());
    }

    @Override
    public int getItemCount() {
        return emotionSummaryList.size();
    }

    class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView emotionTextView;

        SummaryViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.DayTextView);
            emotionTextView = itemView.findViewById(R.id.EmotionTextView);
        }
    }
}
*/
