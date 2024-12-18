package com.helpkonnect.mobileapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalListAdapter extends RecyclerView.Adapter<JournalListAdapter.JournalViewHolder> {

    private final List<Journal> journals;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Journal journal);
    }

    public static class Journal {
        public final String imageUrl;
        public final String title;
        public final String subtitle;
        public final Timestamp date;
        public final String preview;
        public final String documentId;
        public String fullNotes;
        private String translatedNotes;

        public Journal(String imageUrl, String title, String subtitle, Timestamp date, String preview, String documentId) {
            this.imageUrl = imageUrl;
            this.title = title;
            this.subtitle = subtitle;
            this.date = date;
            this.preview = preview;
            this.documentId = documentId;
            this.translatedNotes = translatedNotes;
        }

        public String getTranslatedNotes() {
            return translatedNotes;
        }

        public void setTranslatedNotes(String translatedNotes) {
            this.translatedNotes = translatedNotes;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public Timestamp getDate() {
            return date;
        }

        public String getNotes() {
            return preview;
        }

        public String getFullNotes() {
            return fullNotes;
        }

        public void setFullNotes(String fullNotes) {
            this.fullNotes = fullNotes;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getDocumentId() {
            return documentId;
        }
    }

    public static class JournalViewHolder extends RecyclerView.ViewHolder {
        private final ImageView journalImage;
        private final TextView journalTitle;
        private final TextView journalDate;
        private final TextView journalPreview;

        public JournalViewHolder(@NonNull View itemView, final OnItemClickListener onItemClick, final List<Journal> journals) {
            super(itemView);
            journalImage = itemView.findViewById(R.id.journalimageViewholder);
            journalTitle = itemView.findViewById(R.id.journaltitle);
            journalDate = itemView.findViewById(R.id.journaldate);
            journalPreview = itemView.findViewById(R.id.journalpreview);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Journal journal = journals.get(position);

                    // Start slide-in animation
                    Context context = itemView.getContext();
                    Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.journal_slide_right);
                    itemView.startAnimation(slideIn);

                    onItemClick.onItemClick(journal);
                }
            });
        }

        public void bind(Journal journal) {
            Picasso.get().load(journal.imageUrl).into(journalImage);
            journalTitle.setText(journal.title);
            String formattedDate = formatDate(journal.date);
            journalDate.setText(formattedDate);
            journalPreview.setText(journal.preview);
        }
    }

    public JournalListAdapter(List<Journal> journals, OnItemClickListener onItemClick) {
        this.journals = journals;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_journal, parent, false);
        return new JournalViewHolder(view, onItemClick, journals);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        Journal journal = journals.get(position);
        holder.bind(journal);
    }

    @Override
    public int getItemCount() {
        return journals.size();
    }

    private static String formatDate(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
        return ""; // Return empty if timestamp is null
    }
}