package com.helpkonnect.mobileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ManageResourceAdapter extends RecyclerView.Adapter<ManageResourceAdapter.ManageResourceViewHolder> {

    private List<ResourceModel> resources;

    public ManageResourceAdapter(List<ResourceModel> resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public ManageResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manageresource, parent, false);
        return new ManageResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageResourceViewHolder holder, int position) {
        ResourceModel resource = resources.get(position);
        holder.titleTextView.setText(resource.getTitle());
        holder.descriptionTextView.setText(resource.getDescription());
        holder.imageView.setImageResource(resource.getImageResId());
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    public static class ManageResourceViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView imageView;

        public ManageResourceViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.ResourceTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.ResourceDescriptionTextView);
            imageView = itemView.findViewById(R.id.ResourceImageView);
        }
    }
}
