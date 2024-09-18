package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private TextView currentDateTextView;
    private View loaderView;
    private ImageView createJournalButton;
    private RecyclerView journalCollections;
    private List<JournalListAdapter.Journal> journalList;
    private JournalListAdapter adapter;
    private FirebaseFirestore db;

    private final Date DateToday = new Date();
    private final SimpleDateFormat DateTodayFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
    private final String DateTodayString = DateTodayFormat.format(DateToday);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_journal, container, false);
        loaderView = inflater.inflate(R.layout.initial_loader, container, false);

        db = FirebaseFirestore.getInstance();

        journalList = new ArrayList<>();

        //remove this after testing
        //List<JournalListAdapter.Journal> journalList = new ArrayList<>();
        //journalList.add(new JournalListAdapter.Journal("R.drawable.edittextusericon", "Journal Title 1", "SubTitle 1","2024-09-08", "This is a preview of the journal entry 1."));
        //journalList.add(new JournalListAdapter.Journal("R.drawable.edittextusericon", "Journal Title 2", "SubTitle 2","2024-09-07", "This is a preview of the journal entry 2."));
        //journalList.add(new JournalListAdapter.Journal("R.drawable.edittextusericon", "Journal Title 3", "SubTitle 3","2024-09-06", "This is a preview of the journal entry 3."));
        //journalList.add(new JournalListAdapter.Journal("R.drawable.edittextusericon", "Journal Title 4","SubTitle 4" ,"2024-09-05", "This is a preview of the journal entry 4."));

        adapter = new JournalListAdapter(journalList, journal -> {
            Intent intent = new Intent(requireContext(), CreateJournalActivity.class);

            // Pass the journal details for editing
            intent.putExtra("isNewJournal", false);  // Set to false to indicate this is an edit
            intent.putExtra("journalTitle", journal.getTitle());
            intent.putExtra("journalSubtitle", journal.getSubtitle());
            intent.putExtra("journalDate", journal.getDate());
            intent.putExtra("journalNotes", journal.getNotes());
            intent.putExtra("journalImageUrl", journal.getImageUrl());

            startActivity(intent);
        });


        journalCollections = rootView.findViewById(R.id.journalcollectionrecyclerview);
        journalCollections.setLayoutManager(new LinearLayoutManager(requireContext()));
        journalCollections.setAdapter(adapter);

        currentDateTextView = rootView.findViewById(R.id.currentdate);
        currentDateTextView.setText(DateTodayString);

        createJournalButton = rootView.findViewById(R.id.createjournalbutton);
        createJournalButton.setOnClickListener(v -> {
            Intent toCreateJournal = new Intent(requireContext(), CreateJournalActivity.class);
            toCreateJournal.putExtra("isNewJournal", true);
            startActivity(toCreateJournal);
        });

        fetchJournals();

        return rootView;
    }

    private void fetchJournals() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        showLoader(true);
        db.collection("journals")
                .whereEqualTo("userId", userId)
                .orderBy("dateCreated", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        journalList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String subtitle = document.getString("subtitle");
                            String date = document.getString("dateCreated");
                            String notes = document.getString("notes");
                            String imageUrl = document.getString("imageUrl");
                            String preview = (notes != null && notes.length() > 45) ? notes.substring(0, 45) + "..." : notes;


                            JournalListAdapter.Journal journal = new JournalListAdapter.Journal(imageUrl, title, subtitle, date, preview);
                            journalList.add(journal);
                        }
                        showLoader(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("JournalFragment", "Error getting documents: " + task.getException().getMessage());
                        Toast.makeText(requireContext(), "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoader(boolean show) {
        TextView loadingText = loaderView.findViewById(R.id.loadingText); // Get the TextView

        if (show) {
            getActivity().addContentView(loaderView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            if (loaderView.getParent() != null) {
                ((ViewGroup) loaderView.getParent()).removeView(loaderView);
            }
        }
    }
}
