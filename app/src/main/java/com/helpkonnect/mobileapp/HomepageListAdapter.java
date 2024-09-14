package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomepageListAdapter extends RecyclerView.Adapter<HomepageListAdapter.ViewHolder> {

    public static class Item {
        private final String title;
        private final int imageResId;

        public Item(String title, int imageResId) {
            this.title = title;
            this.imageResId = imageResId;
        }

        public String getTitle() {
            return title;
        }

        public int getImageResId() {
            return imageResId;
        }
    }

    private final List<Item> items;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public HomepageListAdapter(List<Item> items, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView imagetitle;
        public final ImageView imageviewholder;

        public ViewHolder(View itemView) {
            super(itemView);
            imagetitle = itemView.findViewById(R.id.imagetitle);
            imageviewholder = itemView.findViewById(R.id.imageviewholder);

            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(items.get(getAdapterPosition())));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_homepage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.imagetitle.setText(item.getTitle());
        holder.imageviewholder.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
