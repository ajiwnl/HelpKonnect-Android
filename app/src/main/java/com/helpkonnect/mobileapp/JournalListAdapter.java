package com.helpkonnect.mobileapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JournalListAdapter extends RecyclerView.Adapter<JournalListAdapter.JournalViewHolder> {

    private final List<Journal> journals;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(Journal journal);
    }

    public static class Journal {
        public final int imageResId;
        public final String title;
        public final String date;
        public final String preview;

        public Journal(int imageResId, String title, String date, String preview) {
            this.imageResId = imageResId;
            this.title = title;
            this.date = date;
            this.preview = preview;
        }

        public String getTitle() {
            return title;
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClick.onItemClick(journals.get(position));
                    }
                }
            });
        }

        public void bind(Journal journal) {
            journalImage.setImageResource(journal.imageResId);
            journalTitle.setText(journal.title);
            journalDate.setText(journal.date);
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
}
