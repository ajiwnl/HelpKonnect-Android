package com.helpkonnect.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private Context context;
    private List<Resource> resourceList;

    public ResourceAdapter(Context context, List<Resource> resourceList) {
        this.context = context;
        this.resourceList = resourceList;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resourceList.get(position);

        holder.resourceName.setText(resource.getName() != null ? resource.getName() : "Unnamed Resource");
        holder.resourceDescription.setText(resource.getDescription() != null ? resource.getDescription() : "No Description Available");

        Glide.with(context)
                .load(resource.getImageURL())
                .placeholder(io.getstream.chat.android.ui.R.drawable.stream_ui_icon_picture_placeholder)
                .error(io.getstream.chat.android.compose.R.drawable.stream_compose_ic_error)
                .into(holder.resourceImage);

        if (resource.getTime() != null) {
            Date date = resource.getTime().toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String formattedTime = dateFormat.format(date);
            holder.resourceTime.setText(formattedTime);
        } else {
            holder.resourceTime.setText("No Date Available");
        }

        holder.itemView.setOnClickListener(v -> {
            String fileURL = resource.getFileURL();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileURL));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        TextView resourceName, resourceDescription, resourceTime;
        ImageView resourceImage;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            resourceName = itemView.findViewById(R.id.resourceName);
            resourceDescription = itemView.findViewById(R.id.resourceDescription);
            resourceImage = itemView.findViewById(R.id.resourceImage);
            resourceTime = itemView.findViewById(R.id.resourceTime);
        }
    }
}

