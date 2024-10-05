package com.helpkonnect.mobileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResourcesSearchSuggestionAdapter extends RecyclerView.Adapter<ResourcesSearchSuggestionAdapter.ViewHolder> {

    private List<String> suggestions;
    private Context context;

    public ResourcesSearchSuggestionAdapter(Context context, List<String> suggestions) {
        this.context = context;
        this.suggestions = suggestions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_searchsuggestions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.button.setText(suggestion);
        holder.button.setOnClickListener(v -> {
            ((SelfCareMaterialsActivity) context).autoFillSuggested(suggestion);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.suggestionButton);
        }
    }
}
