package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide

import java.util.List;

public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    private List<Professional> professionals;

    public static class Professional {
        private String image; // Image URL
        private String name;
        private String profession;
        private String introduction;
        private String userId;
        private float rate;

        public Professional(String image, String name, String profession, String introduction, String userId, float rate) {
            this.image = image;
            this.name = name;
            this.profession = profession;
            this.introduction = introduction;
            this.userId = userId;
            this.rate = rate;
        }

        public String getImage() { return image; }
        public String getName() { return name; }
        public String getProfession() { return profession; }
        public String getIntroduction() { return introduction; }
        public String getUserId() { return userId; }
        public float getRate() { return rate; }
    }

    public class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        public ImageView therapistImage;
        public TextView therapistName;
        public TextView profession;
        public TextView introduction;

        public ProfessionalViewHolder(View itemView) {
            super(itemView);
            therapistImage = itemView.findViewById(R.id.therapistImage);
            therapistName = itemView.findViewById(R.id.therapistName);
            profession = itemView.findViewById(R.id.profession);
            introduction = itemView.findViewById(R.id.introduction);
            // No click listener is set here
        }
    }

    public ProfessionalAdapter(List<Professional> professionals) {
        this.professionals = professionals;
    }

    @NonNull
    @Override
    public ProfessionalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_professionals, parent, false);
        return new ProfessionalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessionalViewHolder holder, int position) {
        Professional professional = professionals.get(position);

        Glide.with(holder.itemView.getContext())
                .load(professional.getImage())
                .into(holder.therapistImage);

        holder.therapistName.setText(professional.getName());
        holder.profession.setText(professional.getProfession());
        holder.introduction.setText(professional.getIntroduction());
    }

    @Override
    public int getItemCount() {
        return professionals.size();
    }
}
