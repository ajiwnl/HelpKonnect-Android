package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {

    private List<Facility> facilities;
    private OnItemClickListener onItemClick;



    // Define the Facility class
    public static class Facility {
        private String image;
        private String title;
        private String location;
        private float rating;
        private String priceRange;

        // Constructor
        public Facility(String image, String title, String location, float rating, String priceRange) {
            this.image = image;
            this.title = title;
            this.location = location;
            this.rating = rating;
            this.priceRange = priceRange;
        }

        // Getters
        public String getImage() { return image; }
        public String getTitle() { return title; }
        public String getLocation() { return location; }
        public float getRating() { return rating; }
        public String getPriceRange() { return priceRange; }
    }

    // Define the interface for click handling
    public interface OnItemClickListener {
        void onItemClick(Facility facility);
    }

    // Constructor
    public FacilityAdapter(List<Facility> facilities, OnItemClickListener onItemClick) {
        this.facilities = facilities;
        this.onItemClick = onItemClick;
    }

    // FacilityViewHolder class
    public class FacilityViewHolder extends RecyclerView.ViewHolder {
        public ImageView facilityImage;
        public TextView facilityTitle;
        public TextView facilityLocation;
        public TextView facilityRating;
        public TextView facilityPrice;

        public FacilityViewHolder(View itemView) {
            super(itemView);
            facilityImage = itemView.findViewById(R.id.facilityimageviewholder);
            facilityTitle = itemView.findViewById(R.id.facilityTitle);
            facilityLocation = itemView.findViewById(R.id.facilitylocation);
            facilityRating = itemView.findViewById(R.id.facilityrating);
            facilityPrice = itemView.findViewById(R.id.facilityprice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick.onItemClick(facilities.get(position));
                }
            });
        }
    }

    @Override
    public FacilityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_facilities, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FacilityViewHolder holder, int position) {
        Facility facility = facilities.get(position);
        Glide.with(holder.itemView.getContext())
                .load(facility.getImage())
                .into(holder.facilityImage);
        holder.facilityTitle.setText(facility.getTitle());
        holder.facilityLocation.setText(facility.getLocation());
        holder.facilityRating.setText(String.valueOf(facility.getRating()));
        holder.facilityPrice.setText(facility.getPriceRange());
    }

    @Override
    public int getItemCount() {
        return facilities.size();
    }

    public void updateList(List<Facility> newFacilities) {
        facilities.clear();
        facilities.addAll(newFacilities);
        notifyDataSetChanged();
    }
}
