package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

//For Journal Date
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private TextView currentDateTextView;
    private ImageView createJournalButton;
    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_journal, container, false);

        // List of journals
        List<JournalListAdapter.Journal> journalList = new ArrayList<>();
        journalList.add(new JournalListAdapter.Journal(R.drawable.edittextusericon, "Journal Title 1", "2024-09-08", "This is a preview of the journal entry 1."));
        journalList.add(new JournalListAdapter.Journal(R.drawable.edittextusericon, "Journal Title 2", "2024-09-07", "This is a preview of the journal entry 2."));
        journalList.add(new JournalListAdapter.Journal(R.drawable.edittextusericon, "Journal Title 3", "2024-09-06", "This is a preview of the journal entry 3."));
        journalList.add(new JournalListAdapter.Journal(R.drawable.edittextusericon, "Journal Title 4", "2024-09-05", "This is a preview of the journal entry 4."));

        // Setting up RecyclerView
        RecyclerView journalCollections = rootView.findViewById(R.id.journalcollectionrecyclerview);
        journalCollections.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter setup
        JournalListAdapter adapter = new JournalListAdapter(journalList, journal ->
                Toast.makeText(requireContext(), "Clicked: " + journal.getTitle(), Toast.LENGTH_SHORT).show()
        );

        journalCollections.setAdapter(adapter);

        //Display Current Date to TextView
        currentDateTextView = rootView.findViewById(R.id.currentdate);
        currentDateTextView.setText(DateTodayString);

        //Create Journal
        createJournalButton = rootView.findViewById(R.id.createjournalbutton);
        createJournalButton.setOnClickListener( v -> {
            Intent toCreateJournal = new Intent(requireContext(), CreateJournalActivity.class);
            startActivity(toCreateJournal);
        });

        return rootView;
    }
}
