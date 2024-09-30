package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfessionalAdapter extends RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder> {

    private List<Professional> professionals;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Professional professional);
    }

    public static class Professional {
        private int image;
        private String name;
        private String profession;
        private String introduction;

        public Professional(int image, String name, String profession, String introduction) {
            this.image = image;
            this.name = name;
            this.profession = profession;
            this.introduction = introduction;
        }

        public int getImage() { return image; }
        public String getName() { return name; }
        public String getProfession() { return profession; }
        public String getIntroduction() { return introduction; }
    }

    public class ProfessionalViewHolder extends RecyclerView.ViewHolder {
        public ImageView therapistImage;
        public TextView therapistName;
        public TextView profession;
        public TextView introduction;
        public ImageView arrowImage;

        public ProfessionalViewHolder(View itemView) {
            super(itemView);
            therapistImage = itemView.findViewById(R.id.therapistImage);
            therapistName = itemView.findViewById(R.id.therapistName);
            profession = itemView.findViewById(R.id.profession);
            introduction = itemView.findViewById(R.id.introduction);
            arrowImage = itemView.findViewById(R.id.arrowImage);

            // Set click listener for the entire item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(professionals.get(position));
                }
            });
        }
    }

    public ProfessionalAdapter(List<Professional> professionals, OnItemClickListener listener) {
        this.professionals = professionals;
        this.onItemClickListener = listener;
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
        holder.therapistImage.setImageResource(professional.getImage());
        holder.therapistName.setText(professional.getName());
        holder.profession.setText(professional.getProfession());
        holder.introduction.setText(professional.getIntroduction());
    }

    @Override
    public int getItemCount() {
        return professionals.size();
    }
}
